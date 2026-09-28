package com.voidplayer.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.LocalPlayerConnection
import com.voidplayer.music.R
import com.voidplayer.music.constants.AlbumViewTypeKey
import com.voidplayer.music.constants.CONTENT_TYPE_HEADER
import com.voidplayer.music.constants.CONTENT_TYPE_PLAYLIST
import com.voidplayer.music.constants.GridItemSize
import com.voidplayer.music.constants.GridItemsSizeKey
import com.voidplayer.music.constants.GridThumbnailHeight
import com.voidplayer.music.constants.LibraryViewType
import com.voidplayer.music.constants.MixSortDescendingKey
import com.voidplayer.music.constants.MixSortType
import com.voidplayer.music.constants.MixSortTypeKey
import com.voidplayer.music.constants.ShowUploadedPlaylistKey
import com.voidplayer.music.constants.YtmSyncKey
import com.voidplayer.music.db.entities.Album
import com.voidplayer.music.db.entities.Artist
import com.voidplayer.music.db.entities.Playlist
import com.voidplayer.music.extensions.reversed
import com.voidplayer.music.ui.component.AlbumGridItem
import com.voidplayer.music.ui.component.AlbumListItem
import com.voidplayer.music.ui.component.ArtistGridItem
import com.voidplayer.music.ui.component.ArtistListItem
import com.voidplayer.music.ui.component.LocalMenuState
import com.voidplayer.music.ui.component.PlaylistGridItem
import com.voidplayer.music.ui.component.PlaylistListItem
import com.voidplayer.music.ui.component.SortHeader
import com.voidplayer.music.ui.menu.AlbumMenu
import com.voidplayer.music.ui.menu.ArtistMenu
import com.voidplayer.music.ui.menu.PlaylistMenu
import com.voidplayer.music.utils.rememberEnumPreference
import com.voidplayer.music.utils.rememberPreference
import com.voidplayer.music.viewmodels.LibraryMixViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.time.LocalDateTime
import java.util.Locale
import com.voidplayer.music.ui.component.LibrarySpotifyCard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import com.voidplayer.music.ui.theme.VoidColors

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryMixScreen(
    navController: NavController,
    filterContent: @Composable () -> Unit,
    viewModel: LibraryMixViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    var viewType by rememberEnumPreference(AlbumViewTypeKey, LibraryViewType.GRID)
    val (sortType, onSortTypeChange) = rememberEnumPreference(
        MixSortTypeKey,
        MixSortType.CREATE_DATE
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(MixSortDescendingKey, true)
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    val (ytmSync) = rememberPreference(YtmSyncKey, true)

    // VOID library special-grid counts (Favorites / Recently Played / Local Songs)
    val favoritesCount by viewModel.favoritesCount.collectAsState()
    val recentlyPlayedCount by viewModel.recentlyPlayedCount.collectAsState()
    val localSongsCount by viewModel.localSongsCount.collectAsState()

    fun songLabel(count: Int) = "$count song" + if (count != 1) "s" else ""

    // VOID's renderSpecialGrid(), as a 2-column grid: Favorites, Recently
    // Played, Local Songs (only shown once it's non-empty, same as the web
    // app), and Upload Files -- always last. FlowRow with 2 columns matches
    // the HTML's `.lib-spotify-grid { grid-template-columns: 1fr 1fr }`.
    val specialGridContent = @Composable {
        FlowRow(
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val cardModifier = Modifier.weight(1f)

            LibrarySpotifyCard(
                title = stringResource(R.string.liked),
                meta = songLabel(favoritesCount),
                icon = R.drawable.favorite,
                coverBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                coverBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = { navController.navigate("auto_playlist/liked") },
                modifier = cardModifier,
            )

            LibrarySpotifyCard(
                title = stringResource(R.string.history),
                meta = songLabel(recentlyPlayedCount),
                icon = R.drawable.history,
                onClick = { navController.navigate("history") },
                modifier = cardModifier,
            )

            if (localSongsCount > 0) {
                LibrarySpotifyCard(
                    title = stringResource(R.string.filter_local),
                    meta = songLabel(localSongsCount),
                    icon = R.drawable.snippet_folder,
                    coverBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    coverBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    titleColor = MaterialTheme.colorScheme.primary,
                    onClick = { navController.navigate("local_songs") },
                    modifier = cardModifier,
                )
            }
        }
    }


    val albums = viewModel.albums.collectAsState()
    val playlist = viewModel.playlists.collectAsState()

    var allItems = playlist.value + albums.value
    val collator = Collator.getInstance(Locale.getDefault())
    collator.strength = Collator.PRIMARY
    allItems =
        when (sortType) {
            MixSortType.CREATE_DATE ->
                allItems.sortedBy { item ->
                    when (item) {
                        is Album -> item.album.bookmarkedAt
                        is Playlist -> item.playlist.createdAt
                        else -> LocalDateTime.now()
                    }
                }

            MixSortType.NAME ->
                allItems.sortedWith(
                    compareBy(collator) { item ->
                        when (item) {
                            is Album -> item.album.title
                            is Playlist -> item.playlist.name
                            else -> ""
                        }
                    },
                )

            MixSortType.LAST_UPDATED ->
                allItems.sortedBy { item ->
                    when (item) {
                        is Album -> item.album.lastUpdateTime
                        is Artist -> item.artist.lastUpdateTime
                        is Playlist -> item.playlist.lastUpdateTime
                        else -> LocalDateTime.now()
                    }
                }
        }.reversed(sortDescending)

    allItems = allItems.filter { it is Playlist && it.playlist.isPinned } + allItems.filterNot { it is Playlist && it.playlist.isPinned }

    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            when (viewType) {
                LibraryViewType.LIST -> lazyListState.animateScrollToItem(0)
                LibraryViewType.GRID -> lazyGridState.animateScrollToItem(0)
            }
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        if (ytmSync) {
            withContext(Dispatchers.IO) {
                viewModel.syncAllLibrary()
            }
        }
    }

    val headerContent = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp),
        ) {
            SortHeader(
                sortType = sortType,
                sortDescending = sortDescending,
                onSortTypeChange = onSortTypeChange,
                onSortDescendingChange = onSortDescendingChange,
                sortTypeText = { sortType ->
                    when (sortType) {
                        MixSortType.CREATE_DATE -> R.string.sort_by_create_date
                        MixSortType.LAST_UPDATED -> R.string.sort_by_last_updated
                        MixSortType.NAME -> R.string.sort_by_name
                    }
                },
            )

            Spacer(Modifier.weight(1f))

            Spacer(modifier = Modifier.width(16.dp))
        }
    }

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    ) {
        when (viewType) {
            LibraryViewType.LIST ->
                LazyColumn(
                    state = lazyListState,
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    item(
                        key = "filter",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        filterContent()
                    }

                    item(
                        key = "header",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        headerContent()
                    }

                    item(
                        key = "auto_playlists_grid",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        specialGridContent()
                    }

                    item(
                        key = "playlists_header",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        androidx.compose.material3.Text(
                            text = "Playlists",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(
                        items = allItems.distinctBy { it.id },
                        key = { it.id },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { item ->
                        when (item) {
                            is Playlist -> {
                                PlaylistListItem(
                                    playlist = item,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    PlaylistMenu(
                                                        playlist = item,
                                                        coroutineScope = coroutineScope,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    val isSpotify = item.id.startsWith("SPOTIFY_PLAYLIST_") || item.playlist.browseId?.startsWith("SPOTIFY_PLAYLIST_") == true
                                                    val isOnlineYouTube = !isSpotify && (
                                                        item.id.startsWith("PL") || 
                                                        item.id.startsWith("VL") || 
                                                        item.id.startsWith("RD") || 
                                                        item.id.startsWith("MP") || 
                                                        (!item.playlist.isLocal && !item.playlist.browseId.isNullOrBlank())
                                                    )

                                                    if (isOnlineYouTube) {
                                                        val targetBrowseId = (item.playlist.browseId?.takeIf { !it.isNullOrBlank() } ?: item.id).removePrefix("VL")
                                                        navController.navigate("online_playlist/$targetBrowseId")
                                                    } else {
                                                        navController.navigate("local_playlist/${item.id}")
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        PlaylistMenu(
                                                            playlist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }

                            is Artist -> {
                                ArtistListItem(
                                    artist = item,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    ArtistMenu(
                                                        originalArtist = item,
                                                        coroutineScope = coroutineScope,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("artist/${item.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        ArtistMenu(
                                                            originalArtist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }

                            is Album -> {
                                AlbumListItem(
                                    album = item,
                                    isActive = item.id == mediaMetadata?.album?.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    AlbumMenu(
                                                        originalAlbum = item,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("album/${item.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        AlbumMenu(
                                                            originalAlbum = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }

                            else -> {}
                        }
                    }
                }

            LibraryViewType.GRID ->
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns =
                        GridCells.Adaptive(
                            minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp,
                        ),
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    item(
                        key = "filter",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        filterContent()
                    }

                    item(
                        key = "header",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        headerContent()
                    }

                    item(
                        key = "auto_playlists_grid",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        specialGridContent()
                    }

                    item(
                        key = "playlists_header",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        androidx.compose.material3.Text(
                            text = "Playlists",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(
                        items = allItems.distinctBy { it.id },
                        key = { it.id },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { item ->
                        when (item) {
                            is Playlist -> {
                                PlaylistGridItem(
                                    playlist = item,
                                    fillMaxWidth = true,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    val isSpotify = item.id.startsWith("SPOTIFY_PLAYLIST_") || item.playlist.browseId?.startsWith("SPOTIFY_PLAYLIST_") == true
                                                    val isOnlineYouTube = !isSpotify && (
                                                        item.id.startsWith("PL") || 
                                                        item.id.startsWith("VL") || 
                                                        item.id.startsWith("RD") || 
                                                        item.id.startsWith("MP") || 
                                                        (!item.playlist.isLocal && !item.playlist.browseId.isNullOrBlank())
                                                    )

                                                    if (isOnlineYouTube) {
                                                        val targetBrowseId = (item.playlist.browseId?.takeIf { !it.isNullOrBlank() } ?: item.id).removePrefix("VL")
                                                        navController.navigate("online_playlist/$targetBrowseId")
                                                    } else {
                                                        navController.navigate("local_playlist/${item.id}")
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        PlaylistMenu(
                                                            playlist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }

                            is Artist -> {
                                ArtistGridItem(
                                    artist = item,
                                    fillMaxWidth = true,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("artist/${item.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        ArtistMenu(
                                                            originalArtist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }

                            is Album -> {
                                AlbumGridItem(
                                    album = item,
                                    isActive = item.id == mediaMetadata?.album?.id,
                                    isPlaying = isPlaying,
                                    coroutineScope = coroutineScope,
                                    fillMaxWidth = true,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("album/${item.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        AlbumMenu(
                                                            originalAlbum = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                )
                            }

                            else -> {}
                        }
                    }
                }
        }
    }
}

