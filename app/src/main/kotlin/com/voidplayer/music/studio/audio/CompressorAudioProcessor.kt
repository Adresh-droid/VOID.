package com.voidplayer.music.studio.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max

/**
 * Real-time feed-forward dynamics compressor, ported from the web app's
 * DynamicsCompressorNode usage (threshold/ratio/attack/release/knee) since
 * Android has no equivalent built-in node reachable from an ExoPlayer
 * AudioProcessor chain -- this implements the same soft-knee envelope-
 * follower model by hand on raw 16-bit PCM.
 *
 * Same AudioProcessor shape as StudioAudioProcessor/CustomEqualizerAudioProcessor
 * so it drops into the same DefaultAudioProcessorChain array.
 */
@UnstableApi
class CompressorAudioProcessor : AudioProcessor {

    private var sampleRate = 0
    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var isActive = false

    private var inputEnded = false
    private var outputBuffer: ByteBuffer = EMPTY_BUFFER

    @Volatile private var enabled = false

    // dB thresh, ratio (x:1), attack/release in seconds, knee width in dB --
    // same parameter set as the HTML's DynamicsCompressorNode calls; VOID's
    // presets override these per-preset.
    @Volatile private var thresholdDb: Double = -24.0
    @Volatile private var ratio: Double = 4.0
    @Volatile private var attackSeconds: Double = 0.003
    @Volatile private var releaseSeconds: Double = 0.25
    @Volatile private var kneeDb: Double = 30.0
    @Volatile private var makeupGainLinear: Double = 1.0

    // Linked stereo envelope follower (dB domain) -- persists across
    // buffers since attack/release times span many process() calls.
    private var envelopeDb = 0.0

    // Live gain-reduction meter for the UI (dB, always <= 0, 0 = no reduction).
    @Volatile private var currentGainReductionDb: Double = 0.0

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    }

    @Synchronized
    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) {
            envelopeDb = 0.0
            currentGainReductionDb = 0.0
        }
    }

    @Synchronized
    fun setParams(
        thresholdDb: Double,
        ratio: Double,
        attackSeconds: Double,
        releaseSeconds: Double,
        kneeDb: Double,
        makeupGainDb: Double = 0.0,
    ) {
        this.thresholdDb = thresholdDb
        this.ratio = ratio.coerceAtLeast(1.0)
        this.attackSeconds = attackSeconds.coerceAtLeast(0.0001)
        this.releaseSeconds = releaseSeconds.coerceAtLeast(0.0001)
        this.kneeDb = kneeDb.coerceAtLeast(0.0)
        // Add automatic makeup gain if threshold is deep, compensating for overall volume drop
        val autoMakeup = if (thresholdDb < -5.0 && ratio > 1.5) (kotlin.math.abs(thresholdDb) * (1.0 - 1.0/ratio)) / 2.0 else 0.0
        this.makeupGainLinear = Math.pow(10.0, (makeupGainDb + autoMakeup) / 20.0)
    }

    /** Current gain reduction in dB (<= 0), for the UI's GR meter. */
    fun readGainReductionDb(): Double = currentGainReductionDb

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        encoding = inputAudioFormat.encoding

        if (encoding != C.ENCODING_PCM_16BIT || channelCount > 2) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }

        isActive = true
        return inputAudioFormat
    }

    override fun isActive(): Boolean = isActive

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        if (outputBuffer.capacity() < remaining) {
            outputBuffer = ByteBuffer.allocateDirect(remaining).order(ByteOrder.nativeOrder())
        } else {
            outputBuffer.clear()
        }

        if (!enabled || encoding != C.ENCODING_PCM_16BIT) {
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            return
        }

        process(inputBuffer, outputBuffer)
        outputBuffer.flip()
    }

    /**
     * Soft-knee gain computer: below (threshold - knee/2) -> unity;
     * above (threshold + knee/2) -> full ratio compression; in between ->
     * quadratic interpolation. Standard soft-knee compressor formula.
     */
    private fun gainComputerDb(inputDb: Double): Double {
        val kneeStart = thresholdDb - kneeDb / 2.0
        val kneeEnd = thresholdDb + kneeDb / 2.0
        return when {
            inputDb <= kneeStart -> inputDb
            inputDb >= kneeEnd -> thresholdDb + (inputDb - thresholdDb) / ratio
            else -> {
                val delta = inputDb - kneeStart
                inputDb + ((1.0 / ratio - 1.0) * delta * delta) / (2.0 * kneeDb)
            }
        }
    }

    private fun process(input: ByteBuffer, output: ByteBuffer) {
        val sampleCount = input.remaining() / 2
        val frames = if (channelCount == 2) sampleCount / 2 else sampleCount

        val attackCoeff = 1.0 - exp(-1.0 / (sampleRate * attackSeconds))
        val releaseCoeff = 1.0 - exp(-1.0 / (sampleRate * releaseSeconds))

        var peakReductionDb = 0.0

        repeat(frames) {
            when (channelCount) {
                1 -> {
                    val sample = input.getShort().toDouble() / 32768.0
                    val absSample = abs(sample)
                    val inputDb = if (absSample > 0.0) 20.0 * log10(absSample) else -100.0
                    val targetDb = gainComputerDb(inputDb)
                    val diffDb = targetDb - inputDb
                    val coeff = if (diffDb < envelopeDb) attackCoeff else releaseCoeff
                    envelopeDb += coeff * (diffDb - envelopeDb)
                    val gainLinear = Math.pow(10.0, envelopeDb / 20.0) * makeupGainLinear
                    output.putShort(clampToShort(sample * gainLinear))
                    if (envelopeDb < peakReductionDb) peakReductionDb = envelopeDb
                }
                2 -> {
                    val left = input.getShort().toDouble() / 32768.0
                    val right = input.getShort().toDouble() / 32768.0

                    // Linked stereo detection: both channels share one
                    // envelope derived from the louder channel, so panned
                    // content doesn't cause image shift under compression.
                    val peakAbs = max(abs(left), abs(right))
                    val inputDb = if (peakAbs > 0.0) 20.0 * log10(peakAbs) else -100.0
                    val targetDb = gainComputerDb(inputDb)
                    val diffDb = targetDb - inputDb

                    val coeff = if (diffDb < envelopeDb) attackCoeff else releaseCoeff
                    envelopeDb += coeff * (diffDb - envelopeDb)

                    val gainLinear = Math.pow(10.0, envelopeDb / 20.0) * makeupGainLinear

                    output.putShort(clampToShort(left * gainLinear))
                    output.putShort(clampToShort(right * gainLinear))

                    if (envelopeDb < peakReductionDb) peakReductionDb = envelopeDb
                }
                else -> {
                    repeat(channelCount) { output.putShort(input.getShort()) }
                }
            }
        }

        currentGainReductionDb = peakReductionDb
    }

    private fun clampToShort(value: Double): Short =
        (value * 32768.0).coerceIn(-32768.0, 32767.0).toInt().toShort()

    override fun getOutput(): ByteBuffer {
        val buffer = outputBuffer
        outputBuffer = EMPTY_BUFFER
        return buffer
    }

    override fun isEnded(): Boolean = inputEnded && outputBuffer.remaining() == 0

    @Deprecated("Deprecated in Java")
    override fun flush() {
        outputBuffer = EMPTY_BUFFER
        inputEnded = false
    }

    override fun reset() {
        @Suppress("DEPRECATION")
        flush()
        sampleRate = 0
        channelCount = 0
        encoding = C.ENCODING_INVALID
        isActive = false
        envelopeDb = 0.0
    }

    override fun queueEndOfStream() {
        inputEnded = true
    }
}

