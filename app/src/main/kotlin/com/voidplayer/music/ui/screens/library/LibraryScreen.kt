package com.voidplayer.music.ui.screens.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.voidplayer.music.LocalDatabase
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.LocalPlayerConnection
import com.voidplayer.music.R
import com.voidplayer.music.constants.ChipSortTypeKey
import com.voidplayer.music.constants.LibraryFilter
import com.voidplayer.music.constants.SongSortType
import com.voidplayer.music.playback.queues.ListQueue
import com.voidplayer.music.ui.component.CreatePlaylistDialog
import com.voidplayer.music.ui.component.TextFieldDialog
import com.voidplayer.music.utils.rememberEnumPreference
import kotlinx.coroutines.flow.map

@Composable
fun LibraryScreen(navController: NavController) {
    var filterType by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
    var showImportMenu by remember { mutableStateOf(false) }
    var showYoutubeImportDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current

    val likedSongsCount by remember(database) {
        database.likedSongsCount()
    }.collectAsState(initial = 0)

    val downloadedSongsCount by remember(database) {
        database.downloadedSongsByNameAsc().map { it.size }
    }.collectAsState(initial = 0)

    BackHandler(enabled = filterType != LibraryFilter.LIBRARY) {
        filterType = LibraryFilter.LIBRARY
    }

    val filterContent = @Composable {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            // Nothing OS 5.0 Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "LIBRARY",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showCreatePlaylistDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.add),
                            contentDescription = "Create Playlist",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = { showImportMenu = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = "Import",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showImportMenu,
                            onDismissRequest = { showImportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Import from Spotify") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.download),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showImportMenu = false
                                    navController.navigate("settings/spotify_import")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import from YouTube Music") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.link),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showImportMenu = false
                                    showYoutubeImportDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // 1. Quick Filter Chips Bar (Sorter)
            val filterPlaylistsLabel = stringResource(R.string.filter_playlists)
            val filterSongsLabel = stringResource(R.string.filter_songs)
            val filterAlbumsLabel = stringResource(R.string.filter_albums)
            val filterLocalLabel = stringResource(R.string.filter_local)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                val filters = listOf(
                    LibraryFilter.LIBRARY to "All",
                    LibraryFilter.PLAYLISTS to filterPlaylistsLabel,
                    LibraryFilter.SONGS to filterSongsLabel,
                    LibraryFilter.ALBUMS to filterAlbumsLabel,
                    LibraryFilter.LOCAL to filterLocalLabel,
                )

                items(filters) { (filter, label) ->
                    val isSelected = filterType == filter
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            filterType = if (filterType == filter && filter != LibraryFilter.LIBRARY) LibraryFilter.LIBRARY else filter
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // 2. Hero Action Cards directly under the Sorter Chips Bar (Liked, Downloads, History)
            if (filterType == LibraryFilter.LIBRARY) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Liked Songs Hero Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .width(160.dp)
                                .clickable {
                                    navController.navigate("auto_playlist/liked")
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.favorite),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.height(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.liked_songs),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$likedSongsCount ${stringResource(R.string.songs)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Downloads Hero Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .width(160.dp)
                                .clickable {
                                    navController.navigate("auto_playlist/downloaded")
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.download),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.height(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.downloaded_songs),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$downloadedSongsCount ${stringResource(R.string.songs)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // History Hero Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .width(160.dp)
                                .clickable {
                                    navController.navigate("history")
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.history),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.height(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.history),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Recent",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val currentInsets = LocalPlayerAwareWindowInsets.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val bottomInsetExtra = 90.dp
    val newInsets = remember(currentInsets, density, layoutDirection) {
        object : WindowInsets {
            override fun getLeft(density: Density, layoutDirection: LayoutDirection) =
                currentInsets.getLeft(density, layoutDirection)

            override fun getTop(density: Density) = currentInsets.getTop(density)

            override fun getRight(density: Density, layoutDirection: LayoutDirection) =
                currentInsets.getRight(density, layoutDirection)

            override fun getBottom(density: Density) =
                currentInsets.getBottom(density) + with(density) { bottomInsetExtra.roundToPx() }
        }
    }

    CompositionLocalProvider(LocalPlayerAwareWindowInsets provides newInsets) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = filterType,
                animationSpec = tween(durationMillis = 200),
                label = "LibraryScreenCrossfade"
            ) { currentFilter ->
                when (currentFilter) {
                    LibraryFilter.LIBRARY -> LibraryMixScreen(navController, filterContent)
                    LibraryFilter.PLAYLISTS -> LibraryPlaylistsScreen(navController, filterContent)
                    LibraryFilter.SONGS -> LibrarySongsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY }
                    )
                    LibraryFilter.ALBUMS -> LibraryAlbumsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY }
                    )
                    LibraryFilter.ARTISTS -> LibraryArtistsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY }
                    )
                    LibraryFilter.LOCAL -> LocalSongScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY },
                        isEmbedded = true
                    )
                }
            }
        }
    }

    if (showYoutubeImportDialog) {
        var url by remember { mutableStateOf(TextFieldValue("")) }
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.link), contentDescription = null) },
            title = {
                Column {
                    Text(text = "Import playlist from YouTube Music", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Paste the public YouTube Music playlist link below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            initialTextFieldValue = url,
            autoFocus = true,
            onDismiss = { showYoutubeImportDialog = false },
            onDone = { finalUrl ->
                val listId = Regex("[?&]list=([a-zA-Z0-9_-]+)").find(finalUrl)?.groupValues?.get(1)
                if (listId != null) {
                    navController.navigate("online_playlist/$listId")
                } else {
                    android.widget.Toast.makeText(context, "Invalid playlist URL", android.widget.Toast.LENGTH_SHORT).show()
                }
                showYoutubeImportDialog = false
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            initialTextFieldValue = null,
            allowSyncing = true,
            onPlaylistCreated = { playlistId ->
                showCreatePlaylistDialog = false
                navController.navigate("local_playlist/$playlistId")
            }
        )
    }
}
