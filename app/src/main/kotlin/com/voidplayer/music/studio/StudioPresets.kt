package com.voidplayer.music.studio

/**
 * Built-in preset tables, ported 1:1 from the web app's COMP_PRESETS /
 * REVERB_PRESETS / OVERALL_PRESETS objects. Values are exact matches --
 * these are the numbers a VOID user coming from the web app will expect.
 */

data class CompressorPreset(val key: String, val label: String, val settings: CompressorSettings)

val COMPRESSOR_PRESETS: List<CompressorPreset> = listOf(
    CompressorPreset("gentle", "Gentle", CompressorSettings(thresholdDb = -24.0, ratio = 2.0, attackSeconds = 0.03, releaseSeconds = 0.25, kneeDb = 10.0)),
    CompressorPreset("vocal", "Vocal", CompressorSettings(thresholdDb = -18.0, ratio = 3.0, attackSeconds = 0.015, releaseSeconds = 0.15, kneeDb = 6.0)),
    CompressorPreset("punch", "Punch", CompressorSettings(thresholdDb = -20.0, ratio = 4.0, attackSeconds = 0.005, releaseSeconds = 0.10, kneeDb = 4.0)),
    CompressorPreset("brickwall", "Brickwall", CompressorSettings(thresholdDb = -12.0, ratio = 16.0, attackSeconds = 0.001, releaseSeconds = 0.05, kneeDb = 0.0)),
    CompressorPreset("master", "Master", CompressorSettings(thresholdDb = -6.0, ratio = 2.0, attackSeconds = 0.05, releaseSeconds = 0.30, kneeDb = 12.0)),
    CompressorPreset("drums", "Drums", CompressorSettings(thresholdDb = -15.0, ratio = 6.0, attackSeconds = 0.002, releaseSeconds = 0.08, kneeDb = 2.0)),
)

data class ReverbPreset(val key: String, val label: String, val settings: ReverbSettings)

val REVERB_PRESETS: List<ReverbPreset> = listOf(
    ReverbPreset("room", "Room", ReverbSettings(size = 0.8, damp = 0.6, wet = 0.25)),
    ReverbPreset("hall", "Hall", ReverbSettings(size = 2.5, damp = 0.3, wet = 0.40)),
    ReverbPreset("cave", "Cave", ReverbSettings(size = 4.0, damp = 0.1, wet = 0.50)),
    ReverbPreset("spring", "Spring", ReverbSettings(size = 0.5, damp = 0.8, wet = 0.30)),
    ReverbPreset("plate", "Plate", ReverbSettings(size = 1.2, damp = 0.5, wet = 0.35)),
    ReverbPreset("shimmer", "Shimmer", ReverbSettings(size = 3.5, damp = 0.05, wet = 0.45)),
)

/**
 * An "overall" preset bundles an EQ preset name (matched against
 * ParametricEQParser's built-in profiles / EQProfileRepository), a
 * compressor preset key (nullable = off), and a reverb preset key
 * (nullable = off) -- exact same shape as the HTML's OVERALL_PRESETS.
 */
data class OverallPreset(
    val key: String,
    val eqPresetName: String,
    val compressorPresetKey: String?,
    val reverbPresetKey: String?,
    val compressorOn: Boolean,
    val reverbOn: Boolean,
)

val OVERALL_PRESETS: List<OverallPreset> = listOf(
    OverallPreset("Flat", "Flat", null, null, compressorOn = false, reverbOn = false),
    OverallPreset("Warm", "Lounge", "gentle", "room", compressorOn = true, reverbOn = true),
    OverallPreset("Vocals", "Pop", "vocal", "plate", compressorOn = true, reverbOn = true),
    OverallPreset("Bass Boost", "Bass+", "punch", null, compressorOn = true, reverbOn = false),
    OverallPreset("Lo-Fi", "Lounge", "gentle", "room", compressorOn = true, reverbOn = true),
    OverallPreset("Concert", "Classical", "master", "hall", compressorOn = true, reverbOn = true),
    OverallPreset("Podcast", "Rock", "vocal", null, compressorOn = true, reverbOn = false),
    OverallPreset("Cave", "Flat", "gentle", "cave", compressorOn = true, reverbOn = true),
    OverallPreset("Mastered", "VOID", "brickwall", null, compressorOn = true, reverbOn = false),
)
