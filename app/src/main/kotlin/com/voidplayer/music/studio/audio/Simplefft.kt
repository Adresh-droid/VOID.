package com.voidplayer.music.studio.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Minimal in-place radix-2 Cooley-Tukey FFT, sized for the Studio spectrum
 * visualizer only (not a general DSP utility). Input length must be a
 * power of two -- [magnitudes] handles the resampling from whatever the
 * visualizer tap gives it down to a fixed bin count for drawing.
 */
object SimpleFft {

    /** Computes magnitude spectrum for [samples] (real-valued), returns [binCount] bins. */
    fun magnitudes(samples: FloatArray, binCount: Int): FloatArray {
        val n = nextPowerOfTwo(samples.size)
        val real = DoubleArray(n)
        val imag = DoubleArray(n)

        // Hann window to reduce spectral leakage from the ring buffer's
        // arbitrary start point (there's no periodicity guarantee).
        for (i in samples.indices) {
            val window = 0.5 - 0.5 * cos(2.0 * PI * i / (samples.size - 1).coerceAtLeast(1))
            real[i] = samples[i] * window
        }

        fft(real, imag)

        val usableBins = n / 2
        val raw = DoubleArray(usableBins) { i -> sqrt(real[i] * real[i] + imag[i] * imag[i]) }

        // Resample raw bins down to binCount by simple block-averaging with
        // a log-ish grouping (bass gets finer resolution, treble coarser --
        // matches how the HTML's spectrum bars are grouped visually).
        val out = FloatArray(binCount)
        for (b in 0 until binCount) {
            val t0 = b.toDouble() / binCount
            val t1 = (b + 1).toDouble() / binCount
            val start = (usableBins * (t0 * t0)).toInt().coerceIn(0, usableBins - 1)
            val end = (usableBins * (t1 * t1)).toInt().coerceIn(start + 1, usableBins)
            var sum = 0.0
            for (i in start until end) sum += raw[i]
            out[b] = (sum / (end - start)).toFloat()
        }
        return out
    }

    private fun nextPowerOfTwo(n: Int): Int {
        var p = 1
        while (p < n) p = p shl 1
        return p
    }

    private fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        if (n <= 1) return

        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                val tr = real[i]; real[i] = real[j]; real[j] = tr
                val ti = imag[i]; imag[i] = imag[j]; imag[j] = ti
            }
        }

        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wReal = cos(ang)
            val wImag = sin(ang)
            var i = 0
            while (i < n) {
                var curReal = 1.0
                var curImag = 0.0
                for (k in 0 until len / 2) {
                    val evenReal = real[i + k]
                    val evenImag = imag[i + k]
                    val oddReal = real[i + k + len / 2] * curReal - imag[i + k + len / 2] * curImag
                    val oddImag = real[i + k + len / 2] * curImag + imag[i + k + len / 2] * curReal

                    real[i + k] = evenReal + oddReal
                    imag[i + k] = evenImag + oddImag
                    real[i + k + len / 2] = evenReal - oddReal
                    imag[i + k + len / 2] = evenImag - oddImag

                    val nextReal = curReal * wReal - curImag * wImag
                    val nextImag = curReal * wImag + curImag * wReal
                    curReal = nextReal
                    curImag = nextImag
                }
                i += len
            }
            len = len shl 1
        }
    }
}

