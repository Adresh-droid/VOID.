package com.voidplayer.music.studio.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlin.math.tanh

/**
 * Algorithmic reverb (Schroeder/Freeverb topology: parallel comb filters
 * feeding series allpass filters, per channel) -- used instead of a true
 * convolution reverb since that needs impulse-response sample assets this
 * project doesn't ship. Room/Hall/Cave/Spring/Plate/Shimmer character comes
 * from scaling comb-filter delay lengths by the preset's "size", same as
 * how the HTML's reverb presets vary size/damp/wet.
 *
 * Same AudioProcessor shape as the rest of the Studio chain.
 */
@UnstableApi
class ReverbAudioProcessor : AudioProcessor {

    private var sampleRate = 0
    private var channelCount = 0
    private var encoding = C.ENCODING_INVALID
    private var isActive = false

    private var inputEnded = false
    private var outputBuffer: ByteBuffer = EMPTY_BUFFER

    @Volatile private var enabled = false
    @Volatile private var wet: Double = 0.3
    @Volatile private var roomSize: Double = 2.0
    @Volatile private var damping: Double = 0.5

    private var combsL: Array<CombFilter>? = null
    private var combsR: Array<CombFilter>? = null
    private var allpassesL: Array<AllpassFilter>? = null
    private var allpassesR: Array<AllpassFilter>? = null

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
        private val COMB_TUNING = intArrayOf(1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617)
        private val ALLPASS_TUNING = intArrayOf(556, 441, 341, 225)
        private const val STEREO_SPREAD = 23
        private const val FIXED_GAIN = 0.015
    }

    @Synchronized
    fun setEnabled(value: Boolean) {
        enabled = value
    }

    @Synchronized
    fun setParams(wet: Double, roomSize: Double, damping: Double) {
        val sizeChanged = this.roomSize != roomSize
        this.wet = wet.coerceIn(0.0, 1.0)
        this.roomSize = roomSize.coerceIn(0.1, 8.0)
        this.damping = damping.coerceIn(0.0, 1.0)
        
        val feedback = (0.55 + (this.roomSize.coerceIn(0.1, 4.0) * 0.05)).coerceIn(0.40, 0.82)
        combsL?.forEach { 
            it.damping = this.damping
            it.feedback = feedback
        }
        combsR?.forEach { 
            it.damping = this.damping
            it.feedback = feedback
        }
        if (sizeChanged && sampleRate > 0 && combsL == null) {
            buildFilters()
        }
    }

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        encoding = inputAudioFormat.encoding

        if (encoding != C.ENCODING_PCM_16BIT || channelCount > 2) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }

        buildFilters()
        isActive = true
        return inputAudioFormat
    }

    private fun buildFilters() {
        val scale = (sampleRate / 44100.0) * (roomSize.coerceIn(0.1, 4.0) / 2.0)
        val feedback = (0.55 + (roomSize.coerceIn(0.1, 4.0) * 0.05)).coerceIn(0.40, 0.82)
        combsL = Array(COMB_TUNING.size) { i -> CombFilter((COMB_TUNING[i] * scale).roundToInt().coerceAtLeast(1), damping, feedback) }
        combsR = Array(COMB_TUNING.size) { i -> CombFilter(((COMB_TUNING[i] + STEREO_SPREAD) * scale).roundToInt().coerceAtLeast(1), damping, feedback) }
        allpassesL = Array(ALLPASS_TUNING.size) { i -> AllpassFilter((ALLPASS_TUNING[i] * scale).roundToInt().coerceAtLeast(1), 0.5) }
        allpassesR = Array(ALLPASS_TUNING.size) { i -> AllpassFilter(((ALLPASS_TUNING[i] + STEREO_SPREAD) * scale).roundToInt().coerceAtLeast(1), 0.5) }
    }

    override fun isActive(): Boolean = isActive

    private var buffer: ByteBuffer = EMPTY_BUFFER

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        if (buffer.capacity() < remaining) {
            buffer = ByteBuffer.allocateDirect(remaining).order(ByteOrder.nativeOrder())
        } else {
            buffer.clear()
        }

        if (!enabled || encoding != C.ENCODING_PCM_16BIT || combsL == null) {
            buffer.put(inputBuffer)
            buffer.flip()
            outputBuffer = buffer
            return
        }

        process(inputBuffer, buffer)
        buffer.flip()
        outputBuffer = buffer
    }

    override fun queueEndOfStream() {
        inputEnded = true
    }

    private fun process(input: ByteBuffer, output: ByteBuffer) {
        val cL = combsL ?: return
        val cR = combsR ?: return
        val aL = allpassesL ?: return
        val aR = allpassesR ?: return

        val sampleCount = input.remaining() / 2
        val frames = if (channelCount == 2) sampleCount / 2 else sampleCount

        val wetGain = wet.coerceIn(0.0, 1.0) * 3.5 // Boosted reverb wet gain so it's clearly audible
        val dryGain = (1.0 - wetGain * 0.1).coerceIn(0.5, 1.0) // Keep dry mix high so audio doesn't muffle

        repeat(frames) {
            when (channelCount) {
                1 -> {
                    val dry = input.getShort().toDouble() / 32768.0
                    val wetSample = reverbChannel(dry * FIXED_GAIN, cL, aL)
                    val mixed = dry * dryGain + wetSample * wetGain
                    output.putShort(clampToShort(mixed))
                }
                2 -> {
                    val dryL = input.getShort().toDouble() / 32768.0
                    val dryR = input.getShort().toDouble() / 32768.0
                    val wetL = reverbChannel(dryL * FIXED_GAIN, cL, aL)
                    val wetR = reverbChannel(dryR * FIXED_GAIN, cR, aR)
                    output.putShort(clampToShort(dryL * dryGain + wetL * wetGain))
                    output.putShort(clampToShort(dryR * dryGain + wetR * wetGain))
                }
                else -> {
                    repeat(channelCount) { output.putShort(input.getShort()) }
                }
            }
        }
    }

    private fun reverbChannel(input: Double, combs: Array<CombFilter>, allpasses: Array<AllpassFilter>): Double {
        var sum = 0.0
        for (comb in combs) sum += comb.process(input)
        var out = sum
        for (allpass in allpasses) out = allpass.process(out)
        return out
    }

    private fun clampToShort(value: Double): Short {
        val limited = if (value > 0.85) {
            0.85 + 0.15 * tanh((value - 0.85) / 0.15)
        } else if (value < -0.85) {
            -0.85 + 0.15 * tanh((value + 0.85) / 0.15)
        } else {
            value
        }
        return (limited * 32767.0).coerceIn(-32768.0, 32767.0).toInt().toShort()
    }

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
        combsL = null
        combsR = null
        allpassesL = null
        allpassesR = null
    }
}

private class CombFilter(delaySamples: Int, var damping: Double, var feedback: Double = 0.84) {
    private val buffer = DoubleArray(delaySamples)
    private var index = 0
    private var filterState = 0.0

    fun process(input: Double): Double {
        val output = buffer[index]
        filterState = output * (1.0 - damping) + filterState * damping
        buffer[index] = input + filterState * feedback
        index = (index + 1) % buffer.size
        return output
    }
}

private class AllpassFilter(delaySamples: Int, private val feedback: Double = 0.5) {
    private val buffer = DoubleArray(delaySamples)
    private var index = 0

    fun process(input: Double): Double {
        val bufOut = buffer[index]
        val output = -input + bufOut
        buffer[index] = input + bufOut * feedback
        index = (index + 1) % buffer.size
        return output
    }
}
