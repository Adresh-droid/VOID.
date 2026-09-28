package com.voidplayer.music.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.voidplayer.music.BuildConfig
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.R
import com.voidplayer.music.ui.component.IconButton
import com.voidplayer.music.ui.component.Material3SettingsGroup
import com.voidplayer.music.ui.component.Material3SettingsItem
import com.voidplayer.music.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        )

        Text(
            text = stringResource(R.string.about_void),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )

        Material3SettingsGroup(
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.info),
                    title = { Text(stringResource(R.string.app_version)) },
                    description = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.link),
                    title = { Text(stringResource(R.string.original_project_repo)) },
                    description = { Text("https://github.com/EchoMusicApp/Echo-Music") },
                    onClick = { uriHandler.openUri("https://github.com/EchoMusicApp/Echo-Music") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.link),
                    title = { Text(stringResource(R.string.void_source_repo)) },
                    description = { Text(stringResource(R.string.void_source_repo_placeholder)) }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.echo_attribution_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.echo_attribution_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Material3SettingsGroup(
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.license_void),
                    title = { Text(stringResource(R.string.open_source_licenses)) },
                    description = { Text(stringResource(R.string.license)) },
                    onClick = {
                        // In a real app this might show a list of all libraries.
                        // For now, we point to the main project license.
                        uriHandler.openUri("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Developer & Credits",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )

        Material3SettingsGroup(
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.person),
                    title = { Text("Lead Developer") },
                    description = { Text("Adresh") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.link),
                    title = { Text("Instagram") },
                    description = { Text("@_mtb_adresh_") },
                    onClick = { uriHandler.openUri("https://instagram.com/_mtb_adresh_") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.link),
                    title = { Text("GitHub") },
                    description = { Text("github.com/Adresh-droid") },
                    onClick = { uriHandler.openUri("https://github.com/Adresh-droid") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.share),
                    title = { Text("Email") },
                    description = { Text("i9830162750@gmail.com") },
                    onClick = { uriHandler.openUri("mailto:i9830162750@gmail.com") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.favorite),
                    title = { Text("Support the Project (UPI)") },
                    description = { Text("i9830162750@oksbi") },
                    onClick = { 
                        uriHandler.openUri("upi://pay?pa=i9830162750@oksbi&pn=Adresh")
                    }
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.copyright_notice),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(50.dp))
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)
            )
        )
    }

    TopAppBar(
        title = {
            androidx.compose.animation.AnimatedVisibility(
                visible = scrollState.value > 100,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Text(
                    text = stringResource(R.string.about_void),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
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
        },
        scrollBehavior = scrollBehavior
    )
}
