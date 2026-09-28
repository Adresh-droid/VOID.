package com.voidplayer.music.studio

import com.voidplayer.music.eq.data.FilterType
import com.voidplayer.music.eq.data.ParametricEQ
import com.voidplayer.music.eq.data.ParametricEQBand

/**
 * Built-in 16-band EQ presets, ported 1:1 from the web app's EQ_PRESETS
 * gain arrays and EQ_ALL_FREQS frequency list. The web app builds these as
 * BiquadFilterNodes: band 0 is a lowshelf, band 15 is a highshelf, the 14
 * bands between are peaking filters at Q=1.2 -- same shape as
 * CustomEqualizerAudioProcessor's ParametricEQBand model, so these convert
 * directly with no reinterpretation needed.
 */

private val EQ_16_FREQS = doubleArrayOf(
    32.0, 64.0, 125.0, 250.0, 500.0, 800.0, 1000.0, 1600.0,
    2000.0, 3150.0, 4000.0, 6300.0, 8000.0, 10000.0, 12500.0, 16000.0
)

private fun bandsFromGains(gains: DoubleArray): List<ParametricEQBand> {
    require(gains.size == EQ_16_FREQS.size)
    return gains.mapIndexed { i, gain ->
        val type = when (i) {
            0 -> FilterType.LSC
            EQ_16_FREQS.lastIndex -> FilterType.HSC
            else -> FilterType.PK
        }
        ParametricEQBand(frequency = EQ_16_FREQS[i], gain = gain, q = 1.2, filterType = type)
    }
}

data class StudioEqPreset(val key: String, val label: String, val eq: ParametricEQ)

val STUDIO_EQ_PRESETS: List<StudioEqPreset> = listOf(
    StudioEqPreset("VOID", "VOID", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(9.5, 7.0, 4.0, 2.5, -2.5, -4.0, -2.5, 0.0, 1.0, -1.0, 1.5, 4.5, 4.0, 2.0, 3.0, 2.5)))),
    StudioEqPreset("Flat", "Flat", ParametricEQ(preamp = 0.0, bands = bandsFromGains(DoubleArray(16)))),
    StudioEqPreset("Bass+", "Bass+", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(6.0, 5.0, 4.0, 3.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)))),
    StudioEqPreset("Treble+", "Treble+", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 6.0, 6.0)))),
    StudioEqPreset("V-Shape", "V-Shape", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(5.0, 4.0, 2.0, 0.0, -1.0, -2.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 6.0)))),
    StudioEqPreset("Pop", "Pop", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(-1.0, 0.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0, 0.0, 0.0, 1.0, 2.0, 2.0, 3.0, 3.0, 2.0)))),
    StudioEqPreset("Rock", "Rock", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 4.0, 5.0, 5.0, 4.0)))),
    StudioEqPreset("Jazz", "Jazz", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(3.0, 2.0, 1.0, 0.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 2.0, 3.0, 3.0, 2.0)))),
    StudioEqPreset("Classical", "Classical", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(3.0, 3.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 1.0, 2.0, 3.0, 3.0, 2.0)))),
    StudioEqPreset("Hip-Hop", "Hip-Hop", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(5.0, 4.0, 3.0, 1.0, 0.0, -1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 2.0)))),
    StudioEqPreset("Electronic", "Electronic", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(4.0, 3.0, 1.0, 0.0, -1.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 4.0, 3.0, 2.0)))),
    StudioEqPreset("Lounge", "Lounge", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(3.0, 2.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 2.0, 3.0, 3.0, 2.0)))),
    StudioEqPreset("Acoustic", "Acoustic", ParametricEQ(preamp = 0.0, bands = bandsFromGains(doubleArrayOf(4.0, 3.0, 2.0, 1.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 3.0, 4.0, 4.0, 3.0)))),
)

/** The 16 band center frequencies, exposed for the manual-slider UI. */
val STUDIO_EQ_BAND_FREQUENCIES: List<Double> = EQ_16_FREQS.toList()
