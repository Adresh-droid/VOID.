package com.voidplayer.music.ui.screens.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.voidplayer.music.eq.EqualizerService
import com.voidplayer.music.eq.data.EQProfileRepository
import com.voidplayer.music.eq.data.SavedEQProfile
import com.voidplayer.music.studio.CompressorSettings
import com.voidplayer.music.studio.ReverbSettings
import com.voidplayer.music.studio.StudioService
import com.voidplayer.music.studio.StudioSettings
import com.voidplayer.music.studio.audio.StudioAudioProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudioViewModel @Inject constructor(
    private val studioService: StudioService,
    private val equalizerService: EqualizerService,
    private val eqProfileRepository: EQProfileRepository,
) : ViewModel() {

    // Raw mono samples for the visualizer, refreshed on a UI-driven poll
    // rather than pushed from the playback thread -- see StudioAudioProcessor's
    // VisualizerTap for why that's an intentional, safe trade-off here.
    private val _spectrum = MutableStateFlow(FloatArray(SPECTRUM_BINS))
    val spectrum: StateFlow<FloatArray> = _spectrum.asStateFlow()

    private val _waveform = MutableStateFlow(FloatArray(WAVEFORM_SAMPLES))
    val waveform: StateFlow<FloatArray> = _waveform.asStateFlow()

    private val _gainReductionDb = MutableStateFlow(0.0)
    val gainReductionDb: StateFlow<Double> = _gainReductionDb.asStateFlow()

    companion object {
        const val SPECTRUM_BINS = 48
        const val WAVEFORM_SAMPLES = 256
        private const val TAP_SIZE = 2048
        private const val POLL_INTERVAL_MS = 33L // ~30fps
    }

    private var polling = false

    fun startVisualizerPolling(isPlaying: () -> Boolean) {
        if (polling) return
        polling = true
        viewModelScope.launch {
            val tap = FloatArray(TAP_SIZE)
            val silence = FloatArray(TAP_SIZE)
            while (isActive && polling) {
                val count = if (isPlaying()) studioService.readVisualizerSnapshot(tap) else 0
                if (count > 0) {
                    _spectrum.value = com.voidplayer.music.studio.audio.SimpleFft.magnitudes(tap, SPECTRUM_BINS)
                    _waveform.value = downsampleForWaveform(tap, WAVEFORM_SAMPLES)
                } else {
                    // Paused, stopped, or bypassed: feed silence so visualizers decay to flat.
                    _spectrum.value = com.voidplayer.music.studio.audio.SimpleFft.magnitudes(silence, SPECTRUM_BINS)
                    _waveform.value = downsampleForWaveform(silence, WAVEFORM_SAMPLES)
                }
                _gainReductionDb.value = studioService.readCompressorGainReductionDb()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopVisualizerPolling() {
        polling = false
    }

    private fun downsampleForWaveform(source: FloatArray, targetSize: Int): FloatArray {
        if (source.size <= targetSize) return source.copyOf()
        val out = FloatArray(targetSize)
        val step = source.size.toFloat() / targetSize
        for (i in 0 until targetSize) {
            out[i] = source[(i * step).toInt().coerceIn(0, source.size - 1)]
        }
        return out
    }

    // ── Gain / Pan / Output Mode ────────────────────────────────────────

    fun setEnabled(enabled: Boolean) {
        studioService.setEnabled(enabled)
    }

    fun updateGainDb(gainDb: Float) {
        pushSettings(gainDb = gainDb.toDouble())
    }

    fun updatePan(pan: Float) {
        pushSettings(pan = pan.toDouble())
    }

    fun updateOutputMode(mode: StudioAudioProcessor.OutputMode) {
        pushSettings(outputMode = mode)
    }

    private var lastGainDb = 0.0
    private var lastPan = 0.0
    private var lastOutputMode = StudioAudioProcessor.OutputMode.STEREO

    private fun pushSettings(
        gainDb: Double = lastGainDb,
        pan: Double = lastPan,
        outputMode: StudioAudioProcessor.OutputMode = lastOutputMode,
    ) {
        lastGainDb = gainDb
        lastPan = pan
        lastOutputMode = outputMode
        studioService.updateSettings(StudioSettings(gainDb = gainDb, pan = pan, outputMode = outputMode))
    }

    // ── Compressor ───────────────────────────────────────────────────────

    fun setCompressorEnabled(enabled: Boolean) {
        studioService.setCompressorEnabled(enabled)
    }

    fun updateCompressorSettings(settings: CompressorSettings) {
        studioService.updateCompressorSettings(settings)
    }

    // ── Reverb ───────────────────────────────────────────────────────────

    fun setReverbEnabled(enabled: Boolean) {
        studioService.setReverbEnabled(enabled)
    }

    fun updateReverbSettings(settings: ReverbSettings) {
        studioService.updateReverbSettings(settings)
    }

    // ── 16-band EQ ───────────────────────────────────────────────────────

    /** Applies a built-in Studio EQ preset by wrapping it as a transient SavedEQProfile. */
    fun applyEqPreset(preset: com.voidplayer.music.studio.StudioEqPreset) {
        val profile = SavedEQProfile(
            id = "studio_preset_${preset.key}",
            name = preset.label,
            deviceModel = "",
            bands = preset.eq.bands,
            preamp = preset.eq.preamp,
            isCustom = false,
        )
        equalizerService.applyProfile(profile)
    }

    fun disableEq() {
        equalizerService.disable()
    }

    /** Applies a manual set of 16 band gains (from slider drags), preserving band frequencies/types. */
    fun applyManualEqBands(bands: List<com.voidplayer.music.eq.data.ParametricEQBand>) {
        val profile = SavedEQProfile(
            id = "studio_manual",
            name = "Manual",
            deviceModel = "",
            bands = bands,
            preamp = 0.0,
            isCustom = true,
        )
        equalizerService.applyProfile(profile)
    }

    // ── Sync on entry ────────────────────────────────────────────────────

    /** Called once on screen entry with whatever's persisted in DataStore, to sync the live processors. */
    fun syncInitialSettings(
        studioEnabled: Boolean,
        gainDb: Float,
        pan: Float,
        outputMode: StudioAudioProcessor.OutputMode,
        compressorEnabled: Boolean,
        compressorSettings: CompressorSettings,
        reverbEnabled: Boolean,
        reverbSettings: ReverbSettings,
    ) {
        studioService.setEnabled(studioEnabled)
        studioService.updateSettings(
            StudioSettings(gainDb = gainDb.toDouble(), pan = pan.toDouble(), outputMode = outputMode)
        )
        studioService.setCompressorEnabled(compressorEnabled)
        studioService.updateCompressorSettings(compressorSettings)
        studioService.setReverbEnabled(reverbEnabled)
        studioService.updateReverbSettings(reverbSettings)
    }
}
