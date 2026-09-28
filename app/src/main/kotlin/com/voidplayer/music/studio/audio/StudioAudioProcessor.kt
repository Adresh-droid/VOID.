package com.voidplayer.music.studio.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

/**
 * VOID Studio's "front stage" processor: gain, stereo pan, and output-mode
 * routing (stereo / mono / swapped / left-only / right-only), plus a live
 * PCM tap the Studio screen's spectrum/waveform visualizer reads from.
 *
 * This intentionally mirrors CustomEqualizerAudioProcessor's shape (same
 * ExoPlayer AudioProcessor contract, same 16-bit stereo assumption) so it
 * slots into the same DefaultAudioSink.DefaultAudioProcessorChain in
 * MusicService without needing a different processing model.
 *
 * Placed logically *before* the EQ in signal-flow terms (gain/pan/routing
 * as a "front of chain" stage), though physical position in the chain
 * array only matters relative to where visualizer taps should sample from
 * -- right after this stage is post-gain, pre-EQ, which is what the UI
 * meter should reflect since that's this screen's own section.
 */
@UnstableApi
class StudioAudioProcessor : AudioProcessor {

    enum class OutputMode {
        STEREO, MONO, SWAPPED, LEFT_ONLY, RIGHT_ONLY
    }

    private var sampleRate = 0
    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var isActive = false

    private var inputEnded = false
    private var outputBuffer: ByteBuffer = EMPTY_BUFFER

    // dB gain, matches the HTML's gain slider range (-24..+24 dB is typical
    // for a "gain" stage; VOID Studio's slider range is enforced in the UI
    // layer, this just applies whatever linear factor it's given).
    @Volatile private var gainLinear: Double = 1.0

    // Pan: -1.0 (full left) .. 0.0 (center) .. +1.0 (full right). Uses an
    // equal-power pan law so center stays perceptually at unity gain rather
    // than a plain linear crossfade, which sounds like it dips in the middle.
    @Volatile private var panLeftGain: Double = 1.0
    @Volatile private var panRightGain: Double = 1.0

    @Volatile private var outputMode: OutputMode = OutputMode.STEREO
    @Volatile private var enabled: Boolean = false

    // Live tap for the visualizer: a small ring buffer of the most recent
    // processed samples (post gain/pan/routing), downmixed to mono for
    // simplicity. The Studio screen polls this on a UI-driven timer rather
    // than this processor pushing to it, so no listener/callback plumbing
    // is needed across the playback-service/UI boundary.
    private val visualizerTap = VisualizerTap()

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    }

    @Synchronized
    fun setEnabled(value: Boolean) {
        enabled = value
    }

    @Synchronized
    fun setGainDb(db: Double) {
        gainLinear = Math.pow(10.0, db / 20.0)
    }

    /** pan in [-1, 1], 0 = center. */
    @Synchronized
    fun setPan(pan: Double) {
        val clamped = pan.coerceIn(-1.0, 1.0)
        // Equal-power law: angle sweeps 0..90deg as pan goes -1..1.
        val angle = (clamped + 1.0) * (Math.PI / 4.0) // 0..PI/2
        panLeftGain = Math.cos(angle)
        panRightGain = Math.sin(angle)
    }

    @Synchronized
    fun setOutputMode(mode: OutputMode) {
        outputMode = mode
    }

    /** Snapshot for the visualizer -- safe to call from the UI thread at any rate. */
    fun readVisualizerSnapshot(destination: FloatArray): Int {
        if (!enabled) return 0
        return visualizerTap.read(destination)
    }

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

        processBuffer(inputBuffer, outputBuffer)
        outputBuffer.flip()
    }

    private fun processBuffer(input: ByteBuffer, output: ByteBuffer) {
        val sampleCount = input.remaining() / 2
        val frames = if (channelCount == 2) sampleCount / 2 else sampleCount

        repeat(frames) {
            when (channelCount) {
                1 -> {
                    val sample = input.getShort().toDouble() / 32768.0
                    val gained = sample * gainLinear
                    // Mono input has nothing to pan/route between channels;
                    // gain is the only relevant stage.
                    val outSample = clampToShort(gained)
                    output.putShort(outSample)
                    visualizerTap.push(gained.toFloat())
                }
                2 -> {
                    val left = input.getShort().toDouble() / 32768.0
                    val right = input.getShort().toDouble() / 32768.0

                    var gainedLeft = left * gainLinear
                    var gainedRight = right * gainLinear

                    // Pan
                    val pannedLeft = gainedLeft * panLeftGain
                    val pannedRight = gainedRight * panRightGain

                    // Output mode routing (applied after pan, matching the
                    // HTML's signal order: pan feeds the output-mode stage).
                    val (finalLeft, finalRight) = when (outputMode) {
                        OutputMode.STEREO -> pannedLeft to pannedRight
                        OutputMode.MONO -> {
                            val mono = (pannedLeft + pannedRight) * 0.5
                            mono to mono
                        }
                        OutputMode.SWAPPED -> pannedRight to pannedLeft
                        OutputMode.LEFT_ONLY -> pannedLeft to pannedLeft
                        OutputMode.RIGHT_ONLY -> pannedRight to pannedRight
                    }

                    output.putShort(clampToShort(finalLeft))
                    output.putShort(clampToShort(finalRight))

                    visualizerTap.push(((finalLeft + finalRight) * 0.5).toFloat())
                }
                else -> {
                    repeat(channelCount) { output.putShort(input.getShort()) }
                }
            }
        }
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
    }

    override fun queueEndOfStream() {
        inputEnded = true
    }
}

/**
 * Small lock-free-ish ring buffer of recent mono samples for the
 * visualizer. Not sample-accurate/thread-perfect (playback thread writes,
 * UI thread reads without a lock) but a visualizer only needs "recent and
 * roughly right", not sample-exact synchronization -- a torn read just
 * looks like one slightly-off frame, which is imperceptible at 30-60fps.
 */
private class VisualizerTap(private val capacity: Int = 4096) {
    private val buffer = FloatArray(capacity)
    @Volatile private var writeIndex = 0

    fun push(sample: Float) {
        buffer[writeIndex % capacity] = sample
        writeIndex++
    }

    /** Copies up to destination.size most-recent samples, oldest-first. Returns count written. */
    fun read(destination: FloatArray): Int {
        val count = min(destination.size, capacity)
        val start = writeIndex
        for (i in 0 until count) {
            val idx = (start - count + i)
            val wrapped = ((idx % capacity) + capacity) % capacity
            destination[i] = buffer[wrapped]
        }
        return count
    }
}

