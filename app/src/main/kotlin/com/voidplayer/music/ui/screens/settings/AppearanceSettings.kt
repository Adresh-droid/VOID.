package com.voidplayer.music.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.R
import com.voidplayer.music.constants.DynamicThemeKey
import com.voidplayer.music.constants.SelectedThemeColorKey
import com.voidplayer.music.ui.component.IconButton
import com.voidplayer.music.ui.component.Material3SettingsGroup
import com.voidplayer.music.ui.component.Material3SettingsItem
import com.voidplayer.music.ui.theme.DefaultThemeColor
import com.voidplayer.music.ui.utils.backToMain
import com.voidplayer.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    activity: Activity,
    @Suppress("UNUSED_PARAMETER") snackbarHostState: androidx.compose.material3.SnackbarHostState,
    highlightKey: String? = null
) {
    val scrollState = rememberScrollState()

    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )
    
    val (selectedThemeColorInt) = rememberPreference(
        SelectedThemeColorKey,
        defaultValue = DefaultThemeColor.toArgb()
    )
    
    val isUsingCustomColor = selectedThemeColorInt != DefaultThemeColor.toArgb()

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )
        Material3SettingsGroup(scrollState = scrollState, 
            title = stringResource(R.string.theme),
            items = buildList {
                add(
                    Material3SettingsItem(
                        isHighlighted = (highlightKey == stringResource(R.string.theme)),
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.theme)) },
                        description = { Text(stringResource(R.string.theme_desc)) },
                        onClick = { navController.navigate("settings/appearance/theme") }
                    )
                )
                
                if (!isUsingCustomColor) {
                    add(
                        Material3SettingsItem(
                            isHighlighted = (highlightKey == stringResource(R.string.enable_dynamic_theme)),
                            icon = painterResource(R.drawable.palette),
                            title = { Text(stringResource(R.string.enable_dynamic_theme)) },
                            trailingContent = {
                                Switch(
                                    checked = dynamicTheme,
                                    onCheckedChange = onDynamicThemeChange,
                                    thumbContent = {
                                        Icon(
                                            painter = painterResource(
                                                id = if (dynamicTheme) R.drawable.check else R.drawable.close
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                )
                            },
                            onClick = { onDynamicThemeChange(!dynamicTheme) }
                        )
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.appearance)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

enum class DarkMode {
    ON,
    OFF,
    AUTO,
}

enum class NavigationTab {
    HOME,
    SEARCH,
    LIBRARY,
}

enum class LyricsPosition {
    LEFT,
    CENTER,
    RIGHT,
}

enum class PlayerTextAlignment {
    SIDED,
    CENTER,
}

