package com.voidplayer.music.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.LocalPlayerConnection
import com.voidplayer.music.constants.StudioCompAttackKey
import com.voidplayer.music.constants.StudioCompKneeKey
import com.voidplayer.music.constants.StudioCompReleaseKey
import com.voidplayer.music.constants.StudioCompRatioKey
import com.voidplayer.music.constants.StudioCompThresholdKey
import com.voidplayer.music.constants.StudioCompressorEnabledKey
import com.voidplayer.music.constants.StudioActiveEqPresetKey
import com.voidplayer.music.constants.StudioActiveOverallPresetKey
import com.voidplayer.music.constants.StudioActiveCompPresetKey
import com.voidplayer.music.constants.StudioActiveReverbPresetKey
import com.voidplayer.music.constants.StudioActiveOverallPresetKey
import com.voidplayer.music.constants.StudioActiveCompPresetKey
import com.voidplayer.music.constants.StudioActiveReverbPresetKey
import com.voidplayer.music.constants.StudioEqBandGainsKey
import com.voidplayer.music.constants.StudioEqOnKey
import com.voidplayer.music.constants.StudioEnabledKey
import com.voidplayer.music.constants.StudioGainDbKey
import com.voidplayer.music.constants.StudioOutputModeKey
import com.voidplayer.music.constants.StudioPanKey
import com.voidplayer.music.constants.StudioReverbDampKey
import com.voidplayer.music.constants.StudioReverbEnabledKey
import com.voidplayer.music.constants.StudioReverbSizeKey
import com.voidplayer.music.constants.StudioReverbWetKey
import com.voidplayer.music.eq.data.FilterType
import com.voidplayer.music.eq.data.ParametricEQBand
import com.voidplayer.music.studio.COMPRESSOR_PRESETS
import com.voidplayer.music.studio.CompressorSettings
import com.voidplayer.music.studio.OVERALL_PRESETS
import com.voidplayer.music.studio.OverallPreset
import com.voidplayer.music.studio.REVERB_PRESETS
import com.voidplayer.music.studio.ReverbSettings
import com.voidplayer.music.studio.STUDIO_EQ_BAND_FREQUENCIES
import com.voidplayer.music.studio.STUDIO_EQ_PRESETS
import com.voidplayer.music.studio.StudioEqPreset
import com.voidplayer.music.ui.screens.studio.StudioViewModel
import com.voidplayer.music.studio.audio.StudioAudioProcessor
import com.voidplayer.music.ui.theme.SpaceMono
import com.voidplayer.music.ui.theme.VoidColors
import com.voidplayer.music.ui.theme.VoidRadius
import com.voidplayer.music.utils.rememberEnumPreference
import com.voidplayer.music.utils.rememberPreference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxHeight

/**
 * VOID Studio -- real-time audio DSP surface. Master enable switch, live
 * spectrum + waveform visualizer, Gain, Output Mode, Stereo Pan, an Overall
 * Preset picker that bundles EQ + compressor + reverb, a Compressor section
 * (threshold/ratio/attack/release/knee + live GR meter), a Reverb section
 * (wet/size/damp + character presets), and a 16-band EQ preset picker.
 * Section styling (monospace uppercase headers, thin sliders, pill toggle
 * buttons) is ported 1:1 from the web app's .studio-section / .studio-slider
 * / .studio-fx-btn rules.
 */
@Composable
fun StudioScreen(
    navController: NavController,
    viewModel: StudioViewModel = hiltViewModel(),
) {
    val (studioEnabled, onStudioEnabledChange) = rememberPreference(StudioEnabledKey, false)
    val (gainDb, onGainDbChange) = rememberPreference(StudioGainDbKey, 0f)
    val (pan, onPanChange) = rememberPreference(StudioPanKey, 0f)
    val (outputMode, onOutputModeChange) = rememberEnumPreference(
        StudioOutputModeKey,
        StudioAudioProcessor.OutputMode.STEREO
    )

    val (compEnabled, onCompEnabledChange) = rememberPreference(StudioCompressorEnabledKey, false)
    val (compThresh, onCompThreshChange) = rememberPreference(StudioCompThresholdKey, -24f)
    val (compRatio, onCompRatioChange) = rememberPreference(StudioCompRatioKey, 4f)
    val (compAttack, onCompAttackChange) = rememberPreference(StudioCompAttackKey, 0.003f)
    val (compRelease, onCompReleaseChange) = rememberPreference(StudioCompReleaseKey, 0.25f)
    val (compKnee, onCompKneeChange) = rememberPreference(StudioCompKneeKey, 30f)

    val (reverbEnabled, onReverbEnabledChange) = rememberPreference(StudioReverbEnabledKey, false)
    val (reverbWet, onReverbWetChange) = rememberPreference(StudioReverbWetKey, 0.3f)
    val (reverbSize, onReverbSizeChange) = rememberPreference(StudioReverbSizeKey, 2f)
    val (reverbDamp, onReverbDampChange) = rememberPreference(StudioReverbDampKey, 0.5f)

    val (activeOverallPresetPref, onActiveOverallPresetChange) = rememberPreference(StudioActiveOverallPresetKey, "")
    val activeOverallPreset = activeOverallPresetPref.ifEmpty { null }

    val (activeCompPresetPref, onActiveCompPresetChange) = rememberPreference(StudioActiveCompPresetKey, "")
    val activeCompPreset = activeCompPresetPref.ifEmpty { null }

    val (activeReverbPresetPref, onActiveReverbPresetChange) = rememberPreference(StudioActiveReverbPresetKey, "")
    val activeReverbPreset = activeReverbPresetPref.ifEmpty { null }

    // EQ on/off, active preset key, and per-band gains are persisted (like
    // gain/pan/compressor/reverb above) rather than held in plain remember{}
    // state. The live audio processor keeps whatever profile was last
    // applied regardless of composition, so without this the *visual*
    // editor was resetting to flat/off on every tab switch while the actual
    // audio effect stayed applied underneath -- confusing/misleading UI.
    var eqOn by rememberPreference(StudioEqOnKey, false)
    val (activeEqPresetPref, onActiveEqPresetChange) = rememberPreference(StudioActiveEqPresetKey, "")
    val activeEqPreset = activeEqPresetPref.ifEmpty { null }
    val (eqBandGainsCsv, onEqBandGainsCsvChange) = rememberPreference(StudioEqBandGainsKey, "")
    // Live per-band gains backing the graphical 16-band editor -- restored
    // from the persisted CSV if present (covers tab switches and process
    // restarts alike), otherwise initialized flat.
    val eqBandGains = remember {
        val restored = eqBandGainsCsv
            .split(",")
            .mapNotNull { it.trim().toFloatOrNull() }
            .takeIf { it.size == STUDIO_EQ_BAND_FREQUENCIES.size }
        mutableStateListOf(
            *(restored ?: List(STUDIO_EQ_BAND_FREQUENCIES.size) { 0f }).toTypedArray()
        )
    }

    fun persistEqBandGains() {
        onEqBandGainsCsvChange(eqBandGains.joinToString(","))
    }

    fun currentCompressorSettings() = CompressorSettings(
        thresholdDb = compThresh.toDouble(),
        ratio = compRatio.toDouble(),
        attackSeconds = compAttack.toDouble(),
        releaseSeconds = compRelease.toDouble(),
        kneeDb = compKnee.toDouble(),
    )

    fun currentReverbSettings() = ReverbSettings(
        wet = reverbWet.toDouble(),
        size = reverbSize.toDouble(),
        damp = reverbDamp.toDouble(),
    )

    fun applyCompPreset(preset: com.voidplayer.music.studio.CompressorPreset) {
        onCompThreshChange(preset.settings.thresholdDb.toFloat())
        onCompRatioChange(preset.settings.ratio.toFloat())
        onCompAttackChange(preset.settings.attackSeconds.toFloat())
        onCompReleaseChange(preset.settings.releaseSeconds.toFloat())
        onCompKneeChange(preset.settings.kneeDb.toFloat())
        viewModel.updateCompressorSettings(preset.settings)
        onActiveCompPresetChange(preset.key)
    }

    fun applyReverbPreset(preset: com.voidplayer.music.studio.ReverbPreset) {
        onReverbWetChange(preset.settings.wet.toFloat())
        onReverbSizeChange(preset.settings.size.toFloat())
        onReverbDampChange(preset.settings.damp.toFloat())
        viewModel.updateReverbSettings(preset.settings)
        onActiveReverbPresetChange(preset.key)
    }

    fun applyEqPreset(preset: StudioEqPreset) {
        viewModel.applyEqPreset(preset)
        onActiveEqPresetChange(preset.key)
        eqOn = true
        preset.eq.bands.forEachIndexed { i, band -> if (i < eqBandGains.size) eqBandGains[i] = band.gain.toFloat() }
        persistEqBandGains()
    }

    fun applyManualEqBand(index: Int, gain: Float) {
        eqBandGains[index] = gain
        onActiveEqPresetChange("")
        onActiveOverallPresetChange("")
        eqOn = true
        persistEqBandGains()
        val bands = STUDIO_EQ_BAND_FREQUENCIES.mapIndexed { i, freq ->
            ParametricEQBand(
                frequency = freq,
                gain = eqBandGains[i].toDouble(),
                q = 1.2,
                filterType = when (i) {
                    0 -> FilterType.LSC
                    STUDIO_EQ_BAND_FREQUENCIES.lastIndex -> FilterType.HSC
                    else -> FilterType.PK
                }
            )
        }
        viewModel.applyManualEqBands(bands)
    }

    fun applyOverallPreset(preset: OverallPreset) {
        // EQ
        STUDIO_EQ_PRESETS.find { it.label == preset.eqPresetName }?.let { applyEqPreset(it) }

        // Compressor
        onCompEnabledChange(preset.compressorOn)
        viewModel.setCompressorEnabled(preset.compressorOn)
        preset.compressorPresetKey?.let { key ->
            COMPRESSOR_PRESETS.find { it.key == key }?.let { applyCompPreset(it) }
        }

        // Reverb
        onReverbEnabledChange(preset.reverbOn)
        viewModel.setReverbEnabled(preset.reverbOn)
        preset.reverbPresetKey?.let { key ->
            REVERB_PRESETS.find { it.key == key }?.let { applyReverbPreset(it) }
        }

        onActiveOverallPresetChange(preset.key)
    }

    // Real playback state (not just Studio's own enabled switch) so the
    // spectrum/waveform can decay to flat on pause instead of freezing.
    val playerConnection = LocalPlayerConnection.current
    val isPlaying by (playerConnection?.isPlaying?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) })
    val isPlayingState = rememberUpdatedState(isPlaying)

    // Sync the live processors once on entry with whatever's persisted --
    // covers the case where Studio was enabled last session and the
    // process was killed/relaunched since (ExoPlayer/MusicService starts
    // fresh, so the processors need today's settings pushed to them).
    DisposableEffect(Unit) {
        viewModel.syncInitialSettings(
            studioEnabled = studioEnabled,
            gainDb = gainDb,
            pan = pan,
            outputMode = outputMode,
            compressorEnabled = compEnabled,
            compressorSettings = currentCompressorSettings(),
            reverbEnabled = reverbEnabled,
            reverbSettings = currentReverbSettings(),
        )
        // EQ isn't part of syncInitialSettings since it doesn't use
        // StudioService's push-settings model -- it goes through
        // EqualizerService, which already keeps whatever profile was last
        // applied live on the processor. Re-apply here anyway so a fresh
        // process (not just a tab switch) restores it too, matching the
        // restored eqBandGains/eqOn/activeEqPreset UI state above.
        if (eqOn) {
            val restoredBands = STUDIO_EQ_BAND_FREQUENCIES.mapIndexed { i, freq ->
                ParametricEQBand(
                    frequency = freq,
                    gain = eqBandGains.getOrElse(i) { 0f }.toDouble(),
                    q = 1.2,
                    filterType = when (i) {
                        0 -> FilterType.LSC
                        STUDIO_EQ_BAND_FREQUENCIES.lastIndex -> FilterType.HSC
                        else -> FilterType.PK
                    }
                )
            }
            viewModel.applyManualEqBands(restoredBands)
        }
        // Bars decay to flat on pause/stop instead of freezing -- the
        // ViewModel feeds silence into the FFT whenever isPlaying() is
        // false, since the audio-processor tap otherwise just holds
        // whatever PCM last passed through it.
        viewModel.startVisualizerPolling(isPlaying = { isPlayingState.value })
        onDispose { viewModel.stopVisualizerPolling() }
    }

    val spectrum by viewModel.spectrum.collectAsStateWithLifecycle()
    val waveform by viewModel.waveform.collectAsStateWithLifecycle()
    val gainReductionDb by viewModel.gainReductionDb.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
        item {
            StudioMasterToggle(
                enabled = studioEnabled,
                onToggle = {
                    onStudioEnabledChange(it)
                    viewModel.setEnabled(it)
                }
            )
        }

        item {
            StudioSection(title = "Overall Preset") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    OVERALL_PRESETS.forEach { preset ->
                        StudioPillButton(
                            text = preset.key,
                            selected = activeOverallPreset == preset.key,
                            onClick = { applyOverallPreset(preset) }
                        )
                    }
                }
            }
        }

        item {
            StudioSection(title = "Spectrum Analyzer", trailing = { LiveBadge() }) {
                SpectrumVisualizer(spectrum, isPlaying = isPlaying)
                Spacer(Modifier.height(6.dp))
                WaveformVisualizer(waveform)
            }
        }

        item {
            StudioSection(title = "Output Gain") {
                Text(
                    text = "%.1f dB".format(gainDb),
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SpaceMono),
                    color = VoidColors.Text,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                StudioSliderRow(
                    label = "GAIN",
                    value = gainDb,
                    valueRange = -20f..20f,
                    valueLabel = "%.1fdB".format(gainDb),
                    accentTrack = true,
                    onValueChange = {
                        val stepped = (it * 2).roundToInt() / 2f // 0.5 dB steps, matches HTML
                        onGainDbChange(stepped)
                        viewModel.updateGainDb(stepped)
                    }
                )
            }
        }

        item {
            StudioSection(title = "Output Mode") {
                OutputModeRow(
                    selected = outputMode,
                    onSelect = {
                        onOutputModeChange(it)
                        viewModel.updateOutputMode(it)
                    }
                )
            }
        }

        item {
            StudioSection(title = "Stereo Pan") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "L",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        color = VoidColors.Text3,
                    )
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        ThinSlider(
                            value = pan,
                            valueRange = -1f..1f,
                            accentTrack = false,
                            onValueChange = {
                                val stepped = (it * 100).roundToInt() / 100f
                                onPanChange(stepped)
                                viewModel.updatePan(stepped)
                            }
                        )
                    }
                    Text(
                        text = "R",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        color = VoidColors.Text3,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = panLabel(pan),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SpaceMono,
                        color = VoidColors.Text3,
                        modifier = Modifier.width(38.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    StudioPillButton(
                        text = "CENTER",
                        selected = false,
                        onClick = {
                            onPanChange(0f)
                            viewModel.updatePan(0f)
                        }
                    )
                }
            }
        }

        item {
            StudioSection(
                title = "Compressor",
                trailing = {
                    OnOffBadge(
                        on = compEnabled,
                        onToggle = {
                            onCompEnabledChange(it)
                            viewModel.setCompressorEnabled(it)
                        }
                    )
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    COMPRESSOR_PRESETS.forEach { preset ->
                        StudioPillButton(
                            text = preset.label,
                            selected = activeCompPreset == preset.key,
                            onClick = { applyCompPreset(preset) }
                        )
                    }
                }

                StudioMeterRow(label = "GR", valueDb = gainReductionDb)

                StudioSliderRow(
                    label = "THRESH",
                    value = compThresh,
                    valueRange = -60f..0f,
                    valueLabel = "%.0fdB".format(compThresh),
                    accentTrack = false,
                    onValueChange = {
                        val stepped = it.roundToInt().toFloat()
                        onCompThreshChange(stepped)
                        onActiveCompPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateCompressorSettings(
                            currentCompressorSettings().copy(thresholdDb = stepped.toDouble())
                        )
                    }
                )
                StudioSliderRow(
                    label = "RATIO",
                    value = compRatio,
                    valueRange = 1f..20f,
                    valueLabel = "%.1f:1".format(compRatio),
                    accentTrack = false,
                    onValueChange = {
                        val stepped = (it * 10).roundToInt() / 10f
                        onCompRatioChange(stepped)
                        onActiveCompPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateCompressorSettings(
                            currentCompressorSettings().copy(ratio = stepped.toDouble())
                        )
                    }
                )
                StudioSliderRow(
                    label = "ATTACK",
                    value = compAttack,
                    valueRange = 0.0005f..0.1f,
                    valueLabel = "%.0fms".format(compAttack * 1000),
                    accentTrack = false,
                    onValueChange = {
                        onCompAttackChange(it)
                        onActiveCompPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateCompressorSettings(
                            currentCompressorSettings().copy(attackSeconds = it.toDouble())
                        )
                    }
                )
                StudioSliderRow(
                    label = "RELEASE",
                    value = compRelease,
                    valueRange = 0.01f..1f,
                    valueLabel = "%.0fms".format(compRelease * 1000),
                    accentTrack = false,
                    onValueChange = {
                        onCompReleaseChange(it)
                        onActiveCompPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateCompressorSettings(
                            currentCompressorSettings().copy(releaseSeconds = it.toDouble())
                        )
                    }
                )
                StudioSliderRow(
                    label = "KNEE",
                    value = compKnee,
                    valueRange = 0f..40f,
                    valueLabel = "%.0fdB".format(compKnee),
                    accentTrack = false,
                    onValueChange = {
                        val stepped = it.roundToInt().toFloat()
                        onCompKneeChange(stepped)
                        onActiveCompPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateCompressorSettings(
                            currentCompressorSettings().copy(kneeDb = stepped.toDouble())
                        )
                    }
                )
            }
        }

        item {
            StudioSection(
                title = "Reverb / Space",
                trailing = {
                    OnOffBadge(
                        on = reverbEnabled,
                        onToggle = {
                            onReverbEnabledChange(it)
                            viewModel.setReverbEnabled(it)
                        }
                    )
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    REVERB_PRESETS.forEach { preset ->
                        StudioPillButton(
                            text = preset.label,
                            selected = activeReverbPreset == preset.key,
                            onClick = { applyReverbPreset(preset) }
                        )
                    }
                }

                StudioSliderRow(
                    label = "WET",
                    value = reverbWet,
                    valueRange = 0f..1f,
                    valueLabel = "${(reverbWet * 100).roundToInt()}%",
                    accentTrack = false,
                    onValueChange = {
                        onReverbWetChange(it)
                        onActiveReverbPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateReverbSettings(currentReverbSettings().copy(wet = it.toDouble()))
                    }
                )
                StudioSliderRow(
                    label = "SIZE",
                    value = reverbSize,
                    valueRange = 0.1f..8f,
                    valueLabel = "%.1fs".format(reverbSize),
                    accentTrack = false,
                    onValueChange = {
                        onReverbSizeChange(it)
                        onActiveReverbPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateReverbSettings(currentReverbSettings().copy(size = it.toDouble()))
                    }
                )
                StudioSliderRow(
                    label = "DAMP",
                    value = reverbDamp,
                    valueRange = 0f..1f,
                    valueLabel = "${(reverbDamp * 100).roundToInt()}%",
                    accentTrack = false,
                    onValueChange = {
                        onReverbDampChange(it)
                        onActiveReverbPresetChange("")
                        onActiveOverallPresetChange("")
                        viewModel.updateReverbSettings(currentReverbSettings().copy(damp = it.toDouble()))
                    }
                )
            }
        }

        item {
            StudioSection(
                title = "16-Band Equalizer",
                trailing = {
                    OnOffBadge(
                        on = eqOn,
                        onToggle = { on ->
                            if (on) {
                                val preset = activeEqPreset?.let { key -> STUDIO_EQ_PRESETS.find { it.key == key } }
                                    ?: STUDIO_EQ_PRESETS.first()
                                applyEqPreset(preset)
                            } else {
                                viewModel.disableEq()
                                eqOn = false
                            }
                        }
                    )
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    STUDIO_EQ_PRESETS.forEach { preset ->
                        StudioPillButton(
                            text = preset.label,
                            selected = activeEqPreset == preset.key,
                            onClick = { applyEqPreset(preset) }
                        )
                    }
                }

                EqBandGraph(
                    frequencies = STUDIO_EQ_BAND_FREQUENCIES,
                    gains = eqBandGains,
                    onBandChange = { index, gain -> applyManualEqBand(index, gain) }
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun panLabel(pan: Float): String = when {
    pan == 0f -> "C"
    pan < 0f -> "L%.0f".format(-pan * 100)
    else -> "R%.0f".format(pan * 100)
}

@Composable
private fun StudioMasterToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "VOID Studio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VoidColors.Text,
            )
            Text(
                text = if (enabled) "Processing active" else "Bypassed",
                style = MaterialTheme.typography.labelSmall,
                color = VoidColors.Text2,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}

@Composable
private fun StudioSection(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = Dp.Hairline, color = VoidColors.Border)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = SpaceMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = VoidColors.Text3,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        content()
    }
}

/** Small "LIVE" badge for the spectrum analyzer header, matches .studio-badge. */
@Composable
private fun LiveBadge() {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .border(width = 1.dp, color = accent.copy(alpha = 0.3f), shape = RoundedCornerShape(3.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 8.sp,
            color = accent,
        )
    }
}

/** ON/OFF section toggle badge, mirrors the compressor/reverb/EQ section headers in the HTML. */
@Composable
private fun OnOffBadge(on: Boolean, onToggle: (Boolean) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = if (on) accent else VoidColors.Border2, shape = RoundedCornerShape(4.dp))
            .background(if (on) accent.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onToggle(!on) }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (on) "ON" else "OFF",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            fontSize = 9.sp,
            color = if (on) accent else VoidColors.Text3,
        )
    }
}

/** Gain-reduction (or other dB) meter row: label + thin fill track, matches .studio-meter-row. */
@Composable
private fun StudioMeterRow(label: String, valueDb: Double, maxDb: Double = 24.0) {
    val accent = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            fontWeight = FontWeight.Bold,
            color = VoidColors.Text3,
            modifier = Modifier.width(44.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(VoidColors.Bg4)
        ) {
            val fraction = (abs(valueDb) / maxDb).coerceIn(0.0, 1.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
        }
        Text(
            text = "%.1fdB".format(valueDb),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            color = VoidColors.Text3,
            modifier = Modifier.width(46.dp)
        )
    }
}

/**
 * Spectrum bars ported 1:1 from the web app's drawSpectrum(): bars grouped
 * and centered with a 2px gap, a bottom-to-top gradient tinted by the
 * accent color (brightening toward the top), a 1.5px white peak-hold
 * marker per bar, and a faint idle glow wash when nothing is playing.
 * Smoothing (_smooth) and peak decay (_peak/PEAK_DECAY) are reproduced
 * here with per-bar state that persists across recompositions.
 */
@Composable
private fun SpectrumVisualizer(bins: FloatArray, isPlaying: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val smoothed = remember { mutableStateListOf<Float>() }
    val peaks = remember { mutableStateListOf<Float>() }

    if (bins.isNotEmpty() && smoothed.size != bins.size) {
        smoothed.clear(); smoothed.addAll(FloatArray(bins.size).toTypedArray())
        peaks.clear(); peaks.addAll(FloatArray(bins.size).toTypedArray())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(VoidRadius.Small))
            .background(VoidColors.Bg2)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            if (bins.isEmpty() || smoothed.isEmpty()) return@Canvas
            val barCount = bins.size
            val maxVal = bins.maxOrNull()?.coerceAtLeast(0.0001f) ?: 1f

            for (i in bins.indices) {
                val target = (bins[i] / maxVal).coerceIn(0f, 1f)
                // Fast attack, slower decay -- matches the HTML's smoothing feel.
                smoothed[i] = if (target > smoothed[i]) target else smoothed[i] * 0.85f + target * 0.15f
                peaks[i] = if (smoothed[i] > peaks[i]) smoothed[i] else max(0f, peaks[i] - 0.015f)
            }

            val gap = 2f
            val barWidth = max(2f, (this.size.width - gap * (barCount - 1)) / barCount)
            val totalW = (barWidth + gap) * barCount - gap
            val xOff = (this.size.width - totalW) / 2f
            val h = this.size.height

            val gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(accent.red + 0.31f, accent.green + 0.31f, accent.blue + 0.31f, 1f).coerceChannels(),
                    Color(accent.red + 0.12f, accent.green + 0.12f, accent.blue + 0.12f, 1f).coerceChannels(),
                    accent,
                ),
                startY = 0f,
                endY = h,
            )

            for (i in 0 until barCount) {
                val x = xOff + i * (barWidth + gap)
                val barH = max(2f, smoothed[i] * (h - 4))
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, h - barH),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(1f, 1f)
                )
                if (peaks[i] > 0.04f) {
                    val peakY = h - max(2f, peaks[i] * (h - 4)) - 1f
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = Offset(x, peakY),
                        size = Size(barWidth, 1.5f),
                    )
                }
            }

            if (!isPlaying) {
                drawRect(color = accent.copy(alpha = 0.07f))
            }
        }
    }
}

/** Clamps each channel to [0,1] after additive brightening, keeping alpha untouched. */
private fun Color.coerceChannels(): Color =
    Color(red.coerceIn(0f, 1f), green.coerceIn(0f, 1f), blue.coerceIn(0f, 1f), alpha)

@Composable
private fun WaveformVisualizer(samples: FloatArray) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
        if (samples.isEmpty()) return@Canvas
        val barCount = samples.size
        val gap = 1f
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val centerY = size.height / 2
        for (i in samples.indices) {
            val amplitude = samples[i].coerceIn(-1f, 1f)
            val barHeight = (abs(amplitude) * size.height).coerceAtLeast(1.5f)
            val x = i * (barWidth + gap)
            drawRoundRect(
                color = accent.copy(alpha = 0.35f),
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(0.5f, 0.5f)
            )
        }
    }
}

@Composable
private fun StudioSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    accentTrack: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            fontWeight = FontWeight.Bold,
            color = VoidColors.Text3,
            modifier = Modifier.width(44.dp)
        )
        Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            ThinSlider(
                value = value,
                valueRange = valueRange,
                accentTrack = accentTrack,
                onValueChange = onValueChange,
            )
        }
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            color = VoidColors.Text3,
            modifier = Modifier.width(46.dp)
        )
    }
}

/** Formats an EQ band's center frequency the way the HTML's EQ_ALL_LABELS does ("1k", "12.5k"). */
private fun formatEqFreqLabel(freq: Double): String = when {
    freq < 1000.0 -> freq.roundToInt().toString()
    freq % 1000.0 == 0.0 -> "${(freq / 1000.0).roundToInt()}k"
    else -> "${"%.1f".format(freq / 1000.0)}k"
}

/**
 * Graphical 16-band EQ editor, ported 1:1 from the web app's .eq-bars /
 * .eq-band layout: dB readout on top, a vertical fader in the middle with a
 * thin center (0dB) reference line, frequency label on the bottom. Compact
 * 120dp height matches the HTML's ".studio-section .eq-bars" override, 1px
 * gaps between bands, plain thumb-only track (no filled/accent segment --
 * the native <input type=range> the HTML uses doesn't fill either).
 */
@Composable
private fun EqBandGraph(
    frequencies: List<Double>,
    gains: List<Float>,
    onBandChange: (index: Int, gain: Float) -> Unit,
) {
    val range = -12f..12f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(VoidRadius.Small))
            .background(VoidColors.Bg2)
            .border(width = Dp.Hairline, color = VoidColors.Border, shape = RoundedCornerShape(VoidRadius.Small))
            .padding(top = 10.dp, bottom = 6.dp, start = 6.dp, end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        frequencies.forEachIndexed { i, freq ->
            val gain = gains.getOrElse(i) { 0f }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "%+.1f".format(gain).let { if (gain == 0f) "0" else it },
                    fontFamily = SpaceMono,
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    color = VoidColors.Text3,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    VerticalBandFader(
                        value = gain,
                        valueRange = range,
                        onValueChange = { onBandChange(i, it) }
                    )
                }
                Text(
                    text = formatEqFreqLabel(freq),
                    fontFamily = SpaceMono,
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    color = VoidColors.Text3,
                )
            }
        }
    }
}

/**
 * Single vertical fader for one EQ band -- 3px track, 12px round accent
 * thumb, 1px center (0dB) reference line across the full band width. No
 * fill between center and thumb, matching the native range input's plain
 * track (.eq-band-wrap input[type=range]::-webkit-slider-runnable-track).
 */
@Composable
private fun VerticalBandFader(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    var heightPx by remember { mutableFloatStateOf(0f) }

    fun fractionFromY(y: Float): Float {
        if (heightPx <= 0f) return 0.5f
        // Inverted: top of track = max gain, bottom = min gain.
        return (1f - (y / heightPx)).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(20.dp)
            .pointerInput(valueRange) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        val fraction = fractionFromY(change.position.y)
                        onValueChange(valueRange.start + fraction * (valueRange.endInclusive - valueRange.start))
                    }
                )
            }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val fraction = fractionFromY(offset.y)
                    onValueChange(valueRange.start + fraction * (valueRange.endInclusive - valueRange.start))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxHeight().width(20.dp)) {
            heightPx = size.height
            val trackX = size.width / 2
            val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            val thumbY = size.height * (1f - fraction)
            val centerY = size.height * (1f - (0f - valueRange.start) / (valueRange.endInclusive - valueRange.start))

            // Plain track top-to-bottom -- matches .eq-band-wrap's
            // ::-webkit-slider-runnable-track (no accent fill segment).
            drawLine(
                color = VoidColors.Bg4,
                start = Offset(trackX, 0f),
                end = Offset(trackX, size.height),
                strokeWidth = 3f,
                cap = StrokeCap.Round,
            )
            // 0dB center reference line spans the full band width, matches .eq-center-line.
            drawLine(
                color = VoidColors.Border2,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 1f,
            )
            drawCircle(
                color = accent,
                radius = 6f,
                center = Offset(trackX, thumbY),
            )
        }
    }
}

/**
 * Thin 3dp-track slider with a small round thumb, matching .studio-slider
 * exactly (default Material3 Slider has a much thicker track/large thumb
 * that doesn't match this design at all).
 */
@Composable
private fun ThinSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentTrack: Boolean,
    onValueChange: (Float) -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    var widthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .pointerInput(valueRange) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        if (widthPx > 0f) {
                            val fraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                            val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                        }
                    }
                )
            }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    if (widthPx > 0f) {
                        val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(20.dp)) {
            widthPx = size.width
            val trackY = size.height / 2
            val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            val thumbX = fraction * size.width

            drawLine(
                color = VoidColors.Bg4,
                start = Offset(0f, trackY),
                end = Offset(size.width, trackY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            if (accentTrack) {
                drawLine(
                    color = accent,
                    start = Offset(0f, trackY),
                    end = Offset(thumbX, trackY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            drawCircle(
                color = VoidColors.Text,
                radius = 6.dp.toPx(),
                center = Offset(thumbX, trackY),
            )
        }
    }
}

@Composable
private fun OutputModeRow(
    selected: StudioAudioProcessor.OutputMode,
    onSelect: (StudioAudioProcessor.OutputMode) -> Unit,
) {
    val modes = listOf(
        StudioAudioProcessor.OutputMode.STEREO to "STEREO",
        StudioAudioProcessor.OutputMode.MONO to "MONO",
        StudioAudioProcessor.OutputMode.SWAPPED to "SWAPPED",
        StudioAudioProcessor.OutputMode.LEFT_ONLY to "LEFT ONLY",
        StudioAudioProcessor.OutputMode.RIGHT_ONLY to "RIGHT ONLY",
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        modes.forEach { (mode, label) ->
            StudioPillButton(
                text = label,
                selected = selected == mode,
                onClick = { onSelect(mode) }
            )
        }
    }
}

@Composable
private fun StudioPillButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (selected) accent else VoidColors.Border2,
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (selected) accent.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = SpaceMono,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            fontSize = 9.sp,
            color = if (selected) accent else VoidColors.Text3,
        )
    }
}
