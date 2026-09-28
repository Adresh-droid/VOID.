package com.voidplayer.music.studio

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.voidplayer.music.studio.audio.CompressorAudioProcessor
import com.voidplayer.music.studio.audio.ReverbAudioProcessor
import com.voidplayer.music.studio.audio.StudioAudioProcessor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds references to whichever Studio audio processors are currently
 * attached to the live ExoPlayer instance, and re-applies the last known
 * settings to each whenever the player (and therefore the processors) is
 * recreated -- same pending-state pattern EqualizerService uses, since
 * MusicService can tear down and rebuild its ExoPlayer (e.g. on audio
 * format changes).
 */
@Singleton
class StudioService @Inject constructor() {

    @OptIn(UnstableApi::class)
    private var processor: StudioAudioProcessor? = null
    @OptIn(UnstableApi::class)
    private var compressor: CompressorAudioProcessor? = null
    @OptIn(UnstableApi::class)
    private var reverb: ReverbAudioProcessor? = null

    @Volatile private var pendingSettings: StudioSettings = StudioSettings()
    @Volatile private var pendingEnabled: Boolean = false
    @Volatile private var pendingCompressorSettings: CompressorSettings = CompressorSettings()
    @Volatile private var pendingCompressorEnabled: Boolean = false
    @Volatile private var pendingReverbSettings: ReverbSettings = ReverbSettings()
    @Volatile private var pendingReverbEnabled: Boolean = false

    companion object {
        private const val TAG = "StudioService"
    }

    // ── Gain / Pan / Output Mode ────────────────────────────────────────

    @OptIn(UnstableApi::class)
    fun attachProcessor(newProcessor: StudioAudioProcessor) {
        processor = newProcessor
        applyToProcessor(newProcessor, pendingSettings, pendingEnabled)
        Timber.tag(TAG).d("StudioAudioProcessor attached")
    }

    fun setEnabled(enabled: Boolean) {
        pendingEnabled = enabled
        processor?.setEnabled(enabled)
        compressor?.setEnabled(if (enabled) pendingCompressorEnabled else false)
        reverb?.setEnabled(if (enabled) pendingReverbEnabled else false)
    }

    fun updateSettings(settings: StudioSettings) {
        pendingSettings = settings
        processor?.let { applyToProcessor(it, settings, pendingEnabled) }
    }

    @OptIn(UnstableApi::class)
    private fun applyToProcessor(proc: StudioAudioProcessor, settings: StudioSettings, enabled: Boolean) {
        proc.setEnabled(enabled)
        proc.setGainDb(settings.gainDb)
        proc.setPan(settings.pan)
        proc.setOutputMode(settings.outputMode)
    }

    /** Reads the latest raw samples for the visualizer; returns 0 if no processor is attached. */
    @OptIn(UnstableApi::class)
    fun readVisualizerSnapshot(destination: FloatArray): Int {
        return processor?.readVisualizerSnapshot(destination) ?: 0
    }

    fun isAttached(): Boolean = processor != null

    // ── Compressor ───────────────────────────────────────────────────────

    @OptIn(UnstableApi::class)
    fun attachCompressor(newCompressor: CompressorAudioProcessor) {
        compressor = newCompressor
        applyToCompressor(newCompressor, pendingCompressorSettings, pendingCompressorEnabled)
        Timber.tag(TAG).d("CompressorAudioProcessor attached")
    }

    fun setCompressorEnabled(enabled: Boolean) {
        pendingCompressorEnabled = enabled
        compressor?.setEnabled(if (pendingEnabled) enabled else false)
    }

    fun updateCompressorSettings(settings: CompressorSettings) {
        pendingCompressorSettings = settings
        compressor?.let { applyToCompressor(it, settings, pendingCompressorEnabled) }
    }

    @OptIn(UnstableApi::class)
    private fun applyToCompressor(proc: CompressorAudioProcessor, settings: CompressorSettings, enabled: Boolean) {
        proc.setEnabled(if (pendingEnabled) enabled else false)
        proc.setParams(
            thresholdDb = settings.thresholdDb,
            ratio = settings.ratio,
            attackSeconds = settings.attackSeconds,
            releaseSeconds = settings.releaseSeconds,
            kneeDb = settings.kneeDb,
        )
    }

    /** Current gain-reduction in dB (<=0) for the compressor's UI meter, 0 if not attached. */
    @OptIn(UnstableApi::class)
    fun readCompressorGainReductionDb(): Double = compressor?.readGainReductionDb() ?: 0.0

    // ── Reverb ───────────────────────────────────────────────────────────

    @OptIn(UnstableApi::class)
    fun attachReverb(newReverb: ReverbAudioProcessor) {
        reverb = newReverb
        applyToReverb(newReverb, pendingReverbSettings, pendingReverbEnabled)
        Timber.tag(TAG).d("ReverbAudioProcessor attached")
    }

    fun setReverbEnabled(enabled: Boolean) {
        pendingReverbEnabled = enabled
        reverb?.setEnabled(if (pendingEnabled) enabled else false)
    }

    fun updateReverbSettings(settings: ReverbSettings) {
        pendingReverbSettings = settings
        reverb?.let { applyToReverb(it, settings, pendingReverbEnabled) }
    }

    @OptIn(UnstableApi::class)
    private fun applyToReverb(proc: ReverbAudioProcessor, settings: ReverbSettings, enabled: Boolean) {
        proc.setEnabled(if (pendingEnabled) enabled else false)
        proc.setParams(wet = settings.wet, roomSize = settings.size, damping = settings.damp)
    }
}

/**
 * Snapshot of every VOID Studio "front stage" control -- gain, stereo pan,
 * output-mode routing.
 */
data class StudioSettings(
    val gainDb: Double = 0.0,
    val pan: Double = 0.0,
    val outputMode: StudioAudioProcessor.OutputMode = StudioAudioProcessor.OutputMode.STEREO,
)

/** Compressor section settings -- mirrors the HTML's COMP_PRESETS parameter shape. */
data class CompressorSettings(
    val thresholdDb: Double = -24.0,
    val ratio: Double = 4.0,
    val attackSeconds: Double = 0.003,
    val releaseSeconds: Double = 0.25,
    val kneeDb: Double = 30.0,
)

/** Reverb section settings -- mirrors the HTML's REVERB_PRESETS parameter shape. */
data class ReverbSettings(
    val wet: Double = 0.3,
    val size: Double = 2.0,
    val damp: Double = 0.5,
)
