package com.voidplayer.music.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.R
import com.voidplayer.music.constants.AudioNormalizationKey
import com.voidplayer.music.constants.AutoDownloadOnLikeKey
import com.voidplayer.music.constants.CrossfadeDurationKey
import com.voidplayer.music.constants.CrossfadeEnabledKey
import com.voidplayer.music.constants.CrossfadeGaplessKey
import com.voidplayer.music.constants.HistoryDuration
import com.voidplayer.music.constants.SkipSilenceInstantKey
import com.voidplayer.music.constants.SkipSilenceKey
import com.voidplayer.music.constants.EnableExportAsMp3Key
import com.voidplayer.music.constants.PreloadNextSongEnabledKey
import com.voidplayer.music.constants.PreloadNextSongLimitKey
import com.voidplayer.music.constants.PreloadLyricsEnabledKey

import com.voidplayer.music.ui.component.DefaultDialog
import com.voidplayer.music.ui.component.IconButton
import com.voidplayer.music.ui.component.Material3SettingsGroup
import com.voidplayer.music.ui.component.Material3SettingsItem
import com.voidplayer.music.ui.utils.backToMain
import com.voidplayer.music.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    highlightKey: String? = null) {
    val scrollState = androidx.compose.foundation.rememberScrollState()

    val (crossfadeEnabled, onCrossfadeEnabledChange) = rememberPreference(
        CrossfadeEnabledKey,
        defaultValue = false
    )
    val (crossfadeDuration, onCrossfadeDurationChange) = rememberPreference(
        CrossfadeDurationKey,
        defaultValue = 5f
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false
    )
    val (skipSilenceInstant, onSkipSilenceInstantChange) = rememberPreference(
        SkipSilenceInstantKey,
        defaultValue = false
    )
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        AudioNormalizationKey,
        defaultValue = true
    )

    val (preloadNextSongEnabled, onPreloadNextSongEnabledChange) = rememberPreference(
        key = PreloadNextSongEnabledKey,
        defaultValue = true
    )

    val (preloadNextSongLimit, onPreloadNextSongLimitChange) = rememberPreference(
        key = PreloadNextSongLimitKey,
        defaultValue = 10
    )

    val (preloadLyricsEnabled, onPreloadLyricsEnabledChange) = rememberPreference(
        key = PreloadLyricsEnabledKey,
        defaultValue = true
    )

    val (enableExportAsMp3, onEnableExportAsMp3Change) = rememberPreference(
        key = EnableExportAsMp3Key,
        defaultValue = false
    )


    val (autoDownloadOnLike, onAutoDownloadOnLikeChange) = rememberPreference(
        AutoDownloadOnLikeKey,
        defaultValue = false
    )

    val (historyDuration, onHistoryDurationChange) = rememberPreference(
        HistoryDuration,
        defaultValue = 1f
    )

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        var showCrossfadeBetaDialog by remember { mutableStateOf(false) }
        var showVideoQualityDialog by remember { mutableStateOf(false) }
        val (videoQualityStr, setVideoQualityStr) = rememberPreference(com.voidplayer.music.constants.VideoQualityKey, "360p")

        if (showVideoQualityDialog) {
            DefaultDialog(
                onDismiss = { showVideoQualityDialog = false },
                title = { Text("Background Video Quality") },
                buttons = {
                    TextButton(onClick = { showVideoQualityDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                val options = listOf(
                    "1080p" to "1080p (High Data Usage)",
                    "720p" to "720p (Balanced)",
                    "480p" to "480p (Standard Definition)",
                    "360p" to "360p / 240p (Data Saver)"
                )
                Column {
                    options.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    setVideoQualityStr(key)
                                    showVideoQualityDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = videoQualityStr == key,
                                onClick = null
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showCrossfadeBetaDialog) {
            DefaultDialog(
                onDismiss = { showCrossfadeBetaDialog = false },
                title = { Text(stringResource(R.string.crossfade_beta_title)) },
                buttons = {
                    TextButton(onClick = { showCrossfadeBetaDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        showCrossfadeBetaDialog = false
                        onCrossfadeEnabledChange(true)
                    }) {
                        Text(stringResource(R.string.enable))
                    }
                }
            ) {
                Text(stringResource(R.string.crossfade_beta_message))
            }
        }

        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )

        Material3SettingsGroup(scrollState = scrollState,
            title = stringResource(R.string.player),
            items = buildList {
                add(Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.crossfade)),
                    icon = painterResource(R.drawable.linear_scale),
                    title = { Text(stringResource(R.string.crossfade)) },
                    description = {
                        Text(stringResource(R.string.crossfade_desc))
                    },
                    showBadge = true,
                    trailingContent = {
                        Switch(
                            checked = crossfadeEnabled,
                            onCheckedChange = {
                                if (!crossfadeEnabled) {
                                    showCrossfadeBetaDialog = true
                                } else {
                                    onCrossfadeEnabledChange(false)
                                }
                            },
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (crossfadeEnabled) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = {
                        if (!crossfadeEnabled) {
                            showCrossfadeBetaDialog = true
                        } else {
                            onCrossfadeEnabledChange(false)
                        }
                    }
                ))
                if (crossfadeEnabled) {
                    add(Material3SettingsItem(
                        isHighlighted = (highlightKey == stringResource(R.string.crossfade_duration)),
                        icon = painterResource(R.drawable.timer),
                        title = { Text(stringResource(R.string.crossfade_duration)) },
                        description = {
                            Column {
                                Text(pluralStringResource(R.plurals.seconds, crossfadeDuration.toInt(), crossfadeDuration.toInt()))
                                Slider(
                                    value = crossfadeDuration,
                                    onValueChange = onCrossfadeDurationChange,
                                    valueRange = 1f..15f,
                                    steps = 14
                                )
                            }
                        }
                    ))
                }
                add(Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.history_duration)),
                    icon = painterResource(R.drawable.history),
                    title = { Text(stringResource(R.string.history_duration)) },
                    description = {
                        Slider(
                            value = historyDuration,
                            onValueChange = { onHistoryDurationChange(it.roundToInt().toFloat()) },
                            valueRange = 1f..100f,
                            steps = 9
                        )
                    },
                    trailingContent = {
                        Text(text = historyDuration.roundToInt().toString())
                    }
                ))
                add(Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.skip_silence)),
                    icon = painterResource(R.drawable.fast_forward),
                    title = { Text(stringResource(R.string.skip_silence)) },
                    description = { Text(stringResource(R.string.skip_silence_desc)) },
                    trailingContent = {
                        Switch(
                            checked = skipSilence,
                            onCheckedChange = onSkipSilenceChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (skipSilence) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSkipSilenceChange(!skipSilence) }
                ))
                add(Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.skip_silence_instant)),
                    icon = painterResource(R.drawable.skip_next),
                    title = { Text(stringResource(R.string.skip_silence_instant)) },
                    description = { Text(stringResource(R.string.skip_silence_instant_desc)) },
                    trailingContent = {
                        Switch(
                            checked = skipSilenceInstant,
                            onCheckedChange = { onSkipSilenceInstantChange(it) },
                            enabled = skipSilence,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (skipSilenceInstant) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { if (skipSilence) onSkipSilenceInstantChange(!skipSilenceInstant) }
                ))
                add(Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.audio_normalization)),
                    icon = painterResource(R.drawable.volume_up),
                    title = { Text(stringResource(R.string.audio_normalization)) },
                    trailingContent = {
                        Switch(
                            checked = audioNormalization,
                            onCheckedChange = onAudioNormalizationChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (audioNormalization) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onAudioNormalizationChange(!audioNormalization) }
                ))

                add(Material3SettingsItem(
                    isHighlighted = (highlightKey == "Preload Next Song"),
                    icon = painterResource(R.drawable.skip_next),
                    title = { Text("Preload Next Song") },
                    description = { Text("Cache the next song for gapless playback") },
                    trailingContent = {
                        Switch(
                            checked = preloadNextSongEnabled,
                            onCheckedChange = onPreloadNextSongEnabledChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (preloadNextSongEnabled) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onPreloadNextSongEnabledChange(!preloadNextSongEnabled) }
                ))

                if (preloadNextSongEnabled) {
                    add(Material3SettingsItem(
                        isHighlighted = (highlightKey == "Preload Limit"),
                        icon = painterResource(R.drawable.library_music),
                        title = { Text("Preload Limit") },
                        description = {
                            Slider(
                                value = preloadNextSongLimit.toFloat(),
                                onValueChange = { onPreloadNextSongLimitChange(it.roundToInt()) },
                                valueRange = 1f..10f,
                                steps = 9
                            )
                        },
                        trailingContent = {
                            Text(text = preloadNextSongLimit.toString())
                        }
                    ))

                    add(Material3SettingsItem(
                        isHighlighted = (highlightKey == "Preload Lyrics"),
                        icon = painterResource(R.drawable.queue_music),
                        title = { Text("Preload Lyrics") },
                        description = { Text("Also cache lyrics for the preloaded songs") },
                        trailingContent = {
                            Switch(
                                checked = preloadLyricsEnabled,
                                onCheckedChange = onPreloadLyricsEnabledChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (preloadLyricsEnabled) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onPreloadLyricsEnabledChange(!preloadLyricsEnabled) }
                    ))
                }
            }
        )

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(scrollState = scrollState,
            title = stringResource(R.string.queue),
            items = listOf(
                Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.auto_download_on_like)),
                    icon = painterResource(R.drawable.download),
                    title = { Text(stringResource(R.string.auto_download_on_like)) },
                    description = { Text(stringResource(R.string.auto_download_on_like_desc)) },
                    trailingContent = {
                        Switch(
                            checked = autoDownloadOnLike,
                            onCheckedChange = onAutoDownloadOnLikeChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (autoDownloadOnLike) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onAutoDownloadOnLikeChange(!autoDownloadOnLike) }
                ),
            )
        )

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(scrollState = scrollState,
            title = stringResource(R.string.misc),
            items = listOf(
                Material3SettingsItem(
                    isHighlighted = (highlightKey == stringResource(R.string.export_desc)),
                    icon = painterResource(R.drawable.file_export),
                    title = { Text(stringResource(R.string.export_desc)) },
                    description = { Text("Show 'Export as MP3' in menus") },
                    trailingContent = {
                        Switch(
                            checked = enableExportAsMp3,
                            onCheckedChange = onEnableExportAsMp3Change,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (enableExportAsMp3) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onEnableExportAsMp3Change(!enableExportAsMp3) }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.slow_motion_video),
                    title = { Text("Background Video Quality") },
                    description = {
                        val (videoQuality) = rememberPreference(com.voidplayer.music.constants.VideoQualityKey, "360p")
                        Text(
                            when (videoQuality) {
                                "1080p" -> "1080p (High Data Usage)"
                                "720p" -> "720p (Balanced)"
                                "480p" -> "480p (Standard Definition)"
                                else -> "360p / 240p (Data Saver)"
                            }
                        )
                    },
                    onClick = { showVideoQualityDialog = true }
                )
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.player_and_audio)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}

