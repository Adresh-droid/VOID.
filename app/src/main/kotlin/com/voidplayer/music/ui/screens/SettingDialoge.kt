package com.voidplayer.music.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.voidplayer.music.innertube.utils.parseCookieString

import com.voidplayer.music.R
import com.voidplayer.music.constants.AccountEmailKey
import com.voidplayer.music.constants.InnerTubeCookieKey
import com.voidplayer.music.constants.UseLoginForBrowse
import com.voidplayer.music.constants.YtmSyncKey
import com.voidplayer.music.ui.component.Material3SettingsGroup
import com.voidplayer.music.ui.component.Material3SettingsItem
import com.voidplayer.music.utils.rememberPreference
import com.voidplayer.music.viewmodels.HomeViewModel
import androidx.compose.ui.layout.ContentScale

@Composable
fun SettingDialoge(
    onDismissRequest: () -> Unit,
    onNavigate: (String) -> Unit,
    homeViewModel: HomeViewModel,
    showHistoryButton: Boolean = true
) {
    val (innerTubeCookie, _) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        innerTubeCookie.isNotEmpty() && "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val (accountEmail, _) = rememberPreference(AccountEmailKey, "")
    val accountName by homeViewModel.accountName.collectAsState()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()

    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)
    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, true)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val primaryColor = MaterialTheme.colorScheme.onSurface

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.size(24.dp))

                    Text(
                        text = "VOID",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Close",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Account Group
                Material3SettingsGroup(
                    title = "Account",
                    compact = true,
                    items = listOf(
                        Material3SettingsItem(
                            title = { Text(if (isLoggedIn) accountName else "Anonymous") },
                            description = { Text(if (isLoggedIn) accountEmail.ifEmpty { "Logged In" } else "Not Logged In") },
                            icon = painterResource(R.drawable.account),
                            trailingContent = if (isLoggedIn && !accountImageUrl.isNullOrBlank()) {
                                {
                                    AsyncImage(
                                        model = accountImageUrl,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            } else null,
                            onClick = { if (isLoggedIn) onNavigate("settings/account") else onNavigate("login") }
                        )
                    )
                )

                if (isLoggedIn) {
                    Material3SettingsGroup(
                        title = "Preferences",
                        compact = true,
                        items = listOf(
                            Material3SettingsItem(
                                title = { Text("Use Account for Browsing") },
                                icon = painterResource(R.drawable.add_circle),
                                trailingContent = {
                                    Switch(
                                        checked = useLoginForBrowse,
                                        onCheckedChange = {
                                            com.voidplayer.music.innertube.YouTube.useLoginForBrowse = it
                                            onUseLoginForBrowseChange(it)
                                        },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                },
                                onClick = {
                                    val newVal = !useLoginForBrowse
                                    com.voidplayer.music.innertube.YouTube.useLoginForBrowse = newVal
                                    onUseLoginForBrowseChange(newVal)
                                }
                            ),
                            Material3SettingsItem(
                                title = { Text("YouTube Music Sync") },
                                icon = painterResource(R.drawable.cached),
                                trailingContent = {
                                    Switch(
                                        checked = ytmSync,
                                        onCheckedChange = onYtmSyncChange,
                                        modifier = Modifier.scale(0.8f)
                                    )
                                },
                                onClick = { onYtmSyncChange(!ytmSync) }
                            )
                        )
                    )
                }

                // Quick Access Group
                // History, Refresh (Stats) and Profile (Listen Together) used to
                // live as separate icons in the top app bar. They now live here
                // so the top bar only exposes a single Settings entry point,
                // while remaining just as reachable as before.
                Material3SettingsGroup(
                    title = "Quick Access",
                    compact = true,
                    items = buildList {
                        if (showHistoryButton) {
                            add(
                                Material3SettingsItem(
                                    title = { Text(androidx.compose.ui.res.stringResource(R.string.history)) },
                                    icon = painterResource(R.drawable.music_history),
                                    onClick = { onNavigate("history") }
                                )
                            )
                        }
                        add(
                            Material3SettingsItem(
                                title = { Text(androidx.compose.ui.res.stringResource(R.string.stats)) },
                                icon = painterResource(R.drawable.stats),
                                onClick = { onNavigate("stats") }
                            )
                        )
                    }
                )

                Material3SettingsGroup(
                    title = "App",
                    compact = true,
                    items = listOf(
                        Material3SettingsItem(
                            title = { Text("Settings") },
                            icon = painterResource(R.drawable.settings),
                            onClick = { onNavigate("settings") }
                        ),
                        Material3SettingsItem(
                            title = { Text(androidx.compose.ui.res.stringResource(R.string.about_void)) },
                            icon = painterResource(R.drawable.info),
                            onClick = { onNavigate("settings/about") }
                        )
                    )
                )

            }
        }
    }
}


