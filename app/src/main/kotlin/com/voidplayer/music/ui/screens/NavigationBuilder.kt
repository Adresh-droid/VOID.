package com.voidplayer.music.ui.screens

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.voidplayer.music.constants.DarkModeKey
import com.voidplayer.music.constants.PureBlackKey
import com.voidplayer.music.ui.screens.artist.ArtistAlbumsScreen
import com.voidplayer.music.ui.screens.artist.ArtistItemsScreen
import com.voidplayer.music.ui.screens.artist.ArtistScreen
import com.voidplayer.music.ui.screens.artist.ArtistSongsScreen
import com.voidplayer.music.ui.screens.library.LibraryScreen
import com.voidplayer.music.ui.screens.library.LocalSongScreen
import com.voidplayer.music.ui.screens.playlist.AutoPlaylistScreen
import com.voidplayer.music.ui.screens.playlist.CachePlaylistScreen
import com.voidplayer.music.ui.screens.playlist.LocalPlaylistScreen
import com.voidplayer.music.ui.screens.playlist.OnlinePlaylistScreen
import com.voidplayer.music.ui.screens.playlist.TopPlaylistScreen
import com.voidplayer.music.ui.screens.search.OnlineSearchResult
import com.voidplayer.music.ui.screens.search.SearchScreen
import com.voidplayer.music.ui.screens.settings.AboutSettings
import com.voidplayer.music.ui.screens.settings.AppearanceSettings
import com.voidplayer.music.ui.screens.settings.BackupAndRestore
import com.voidplayer.music.ui.screens.settings.ContentSettings
import com.voidplayer.music.ui.screens.settings.DarkMode
import com.voidplayer.music.ui.screens.settings.SettingsScreen

import com.voidplayer.music.ui.screens.WelcomeScreen
import com.voidplayer.music.ui.screens.settings.AccountSettingsScreen
import com.voidplayer.music.ui.screens.settings.PlayerSettings
import com.voidplayer.music.ui.screens.settings.StorageSettings
import com.voidplayer.music.ui.screens.settings.ThemeScreen
import com.voidplayer.music.utils.rememberEnumPreference
import com.voidplayer.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    activity: Activity,
    snackbarHostState: SnackbarHostState,
    isUpdate: Boolean = false,
    onWelcomeFinished: (Int) -> Unit = {}
) {
    composable(Screens.Home.route) {
        HomeScreen(navController = navController, snackbarHostState = snackbarHostState)
    }



    composable(Screens.Search.route) {
        val pureBlackEnabled by rememberPreference(PureBlackKey, defaultValue = false)
        val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
            if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
        }
        val pureBlack = remember(pureBlackEnabled, useDarkTheme) {
            pureBlackEnabled && useDarkTheme
        }
        SearchScreen(
            navController = navController,
            pureBlack = pureBlack
        )
    }

    composable(Screens.Library.route) {
        LibraryScreen(navController)
    }

    composable(Screens.Studio.route) {
        StudioScreen(navController)
    }

    composable("history") {
        HistoryScreen(navController)
    }

    composable("local_songs") {
        LocalSongScreen(navController)
    }

    composable("stats") {
        StatsScreen(navController)
    }

    composable("mood_and_genres") {
        MoodAndGenresScreen(navController, scrollBehavior)
    }

    composable("account") {
        AccountScreen(navController, scrollBehavior)
    }

    composable("new_release") {
        NewReleaseScreen(navController, scrollBehavior)
    }

    composable("charts_screen") {
        ChartsScreen(navController)
    }

    composable(
        route = "browse/{browseId}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
            }
        )
    ) {
        BrowseScreen(
            navController,
            scrollBehavior,
            it.arguments?.getString("browseId")
        )
    }

    composable(
        route = "search/{query}",
        arguments = listOf(
            navArgument("query") {
                type = NavType.StringType
            },
        ),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            if (targetState.destination.route?.startsWith("search/") == true) {
                fadeOut(tween(200))
            } else {
                fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
            }
        },
        popEnterTransition = {
            if (initialState.destination.route?.startsWith("search/") == true) {
                fadeIn(tween(250))
            } else {
                fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
            }
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        OnlineSearchResult(navController)
    }

    composable(
        route = "album/{albumId}",
        arguments = listOf(
            navArgument("albumId") {
                type = NavType.StringType
            },
        ),
    ) {
        AlbumScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
    ) {
        ArtistScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/songs",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/albums",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            }
        )
    ) {
        ArtistAlbumsScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/items?browseId={browseId}?params={params}",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }

    composable(
        route = "online_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "local_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "auto_playlist/{playlist}",
        arguments = listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "cache_playlist/{playlist}",
        arguments = listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
    ) {
        CachePlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "top_playlist/{top}",
        arguments = listOf(
            navArgument("top") {
                type = NavType.StringType
            },
        ),
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        YouTubeBrowseScreen(navController)
    }

    composable("settings") {
        SettingsScreen(navController, scrollBehavior)
    }

    composable(
        route = "settings/account?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        AccountSettingsScreen(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable(
        route = "settings/appearance?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        AppearanceSettings(navController, scrollBehavior, activity, snackbarHostState, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable("settings/appearance/theme") {
        ThemeScreen(navController)
    }

    composable(
        route = "settings/content?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        ContentSettings(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable(
        route = "settings/player?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        PlayerSettings(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable(
        route = "settings/storage?autoOpenExportPicker={autoOpenExportPicker}&highlightKey={highlightKey}",
        arguments = listOf(
            navArgument("autoOpenExportPicker") {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("highlightKey") { type = NavType.StringType; nullable = true }
        )
    ) { backStackEntry ->
        val autoOpenExportPicker =
            backStackEntry.arguments?.getBoolean("autoOpenExportPicker") ?: false
        StorageSettings(
            navController = navController,
            scrollBehavior = scrollBehavior,
            autoOpenExportPicker = autoOpenExportPicker,
            highlightKey = backStackEntry.arguments?.getString("highlightKey")
        )
    }

    composable(
        route = "settings/backup_restore?highlightKey={highlightKey}",
        arguments = listOf(navArgument("highlightKey") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        BackupAndRestore(navController, scrollBehavior, highlightKey = backStackEntry.arguments?.getString("highlightKey"))
    }

    composable("settings/lastfm") {
        com.voidplayer.music.ui.screens.settings.LastFMSettingsScreen(navController)
    }

    composable("settings/spotify_import") {
        SpotifyImportScreen(navController)
    }

    composable("settings/about") {
        AboutSettings(navController, scrollBehavior)
    }

    composable("login") {
        LoginScreen(navController)
    }

    composable("welcome") {
        val (innerTubeCookie) = rememberPreference(com.voidplayer.music.constants.InnerTubeCookieKey, "")
        val isLoggedIn = remember(innerTubeCookie) {
            innerTubeCookie.isNotEmpty() && "SAPISID" in com.voidplayer.music.innertube.utils.parseCookieString(innerTubeCookie)
        }
        WelcomeScreen(
            isUpdate = isUpdate,
            isLoggedIn = isLoggedIn,
            onLogin = {
                navController.navigate("login")
            },
            onGetStarted = {
                onWelcomeFinished(com.voidplayer.music.BuildConfig.VERSION_CODE)
                navController.navigate(Screens.Home.route) {
                    popUpTo("welcome") { inclusive = true }
                }
            }
        )
    }

}


