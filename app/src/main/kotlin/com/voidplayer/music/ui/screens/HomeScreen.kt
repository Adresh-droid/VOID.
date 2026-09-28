package com.voidplayer.music.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.carousel.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.voidplayer.music.innertube.models.*
import com.voidplayer.music.innertube.utils.completed
import com.voidplayer.music.innertube.utils.parseCookieString
import com.voidplayer.music.innertube.YouTube
import com.voidplayer.music.constants.*
import com.voidplayer.music.db.entities.*
import com.voidplayer.music.extensions.toMediaItem
import com.voidplayer.music.LocalDatabase
import com.voidplayer.music.LocalPlayerAwareWindowInsets
import com.voidplayer.music.LocalPlayerConnection
import com.voidplayer.music.models.toMediaMetadata
import com.voidplayer.music.playback.queues.ListQueue
import com.voidplayer.music.playback.queues.YouTubeQueue
import com.voidplayer.music.R
import com.voidplayer.music.ui.component.*
import com.voidplayer.music.ui.component.shimmer.*
import com.voidplayer.music.ui.menu.*
import com.voidplayer.music.ui.theme.VoidColors
import com.voidplayer.music.ui.utils.SnapLayoutInfoProvider
import com.voidplayer.music.ui.utils.resize
import com.voidplayer.music.utils.listItemShape
import com.voidplayer.music.utils.rememberEnumPreference
import com.voidplayer.music.utils.rememberPreference
import com.voidplayer.music.viewmodels.CommunityPlaylistItem
import com.voidplayer.music.viewmodels.DailyDiscoverItem
import com.voidplayer.music.viewmodels.HomeViewModel
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun NavController.navigateToPlaylistItem(playlist: PlaylistItem) {
    when (val playlistId = playlist.id.removePrefix("VL")) {
        "LM" -> navigate("auto_playlist/liked")
        "SE" -> navigate("auto_playlist/downloaded")
        else -> navigate("online_playlist/$playlistId")
    }
}

sealed class HomeSection(val id: String, val baseWeight: Int) {
    data object LibraryPlaylists : HomeSection("library_playlists", 20000)
    data object KeepListening : HomeSection("keep_listening", 10000)
    data object QuickPicks : HomeSection("quick_picks", 8000)
    data object DailyDiscover : HomeSection("daily_discover", 7000)
    data object AccountPlaylists : HomeSection("account_playlists", 4000)
    data object ForgottenFavorites : HomeSection("forgotten_favorites", 3000)
    data object FromTheCommunity : HomeSection("from_the_community", 2000)
    data class SimilarRecommendation(val index: Int) : HomeSection("similar_recommendation_$index", 1000)
    data class HomePageSection(val index: Int) : HomeSection("home_page_section_$index", 500)
    data object MoodAndGenres : HomeSection("mood_and_genres", 100)
}

@Composable
fun CommunityPlaylistCard(
    item: CommunityPlaylistItem,
    onClick: () -> Unit,
    onSongClick: (SongItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(320.dp).height(420.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(14.dp))) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.weight(1f)) {
                            AsyncImage(model = item.songs.getOrNull(0)?.thumbnail?.resize(544, 544), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize())
                            AsyncImage(model = item.songs.getOrNull(1)?.thumbnail?.resize(544, 544), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize())
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            AsyncImage(model = item.songs.getOrNull(2)?.thumbnail?.resize(544, 544), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize())
                            AsyncImage(model = item.songs.getOrNull(3)?.thumbnail?.resize(544, 544), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxSize())
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.playlist.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(text = item.playlist.author?.name ?: "", style = MaterialTheme.typography.bodyMedium, color = VoidColors.Text2, maxLines = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = item.playlist.songCountText ?: "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.songs.take(3).forEach { song ->
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)).combinedClickable(onClick = { onSongClick(song) }).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(model = song.thumbnail.resize(120, 120), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = song.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyDiscoverCard(
    dailyDiscover: DailyDiscoverItem,
    onClick: () -> Unit,
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(320.dp).height(320.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(dailyDiscover.recommendation.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.7f), Color.Black))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(text = dailyDiscover.recommendation.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = (dailyDiscover.recommendation as? SongItem)?.artists?.joinToString(", ") { it.name } ?: "", style = MaterialTheme.typography.titleMedium, color = VoidColors.Text2, maxLines = 1)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(id = R.drawable.search), contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(text = "Daily Discover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(text = dailyDiscover.seed.title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val libraryPlaylists by viewModel.libraryPlaylists.collectAsState()
    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val dailyDiscover by viewModel.dailyDiscover.collectAsState()
    val communityPlaylists by viewModel.communityPlaylists.collectAsState()

    val speedDialItems by viewModel.speedDialItems.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isRandomizing by viewModel.isRandomizing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val quickPicksLazyGridState = rememberLazyGridState()
    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val accountName by viewModel.accountName.collectAsState()
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val (randomizeHomeOrder) = rememberPreference(RandomizeHomeOrderKey, true)
    val (showSpeedDial) = rememberPreference(ShowSpeedDialKey, true)

    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val url = if (isLoggedIn) accountImageUrl else null
    val scope = rememberCoroutineScope()
    var randomizeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val lazylistState = rememberLazyListState()
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)
    val currentGridHeight = if (gridItemSize == GridItemSize.BIG) GridThumbnailHeight else SmallGridThumbnailHeight
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    var randomSeed by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isRefreshing) { if (isRefreshing) randomSeed = System.currentTimeMillis() }
    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazylistState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    NetworkReload(onReload = viewModel::refresh)
    if (selectedChip != null) BackHandler { viewModel.toggleChip(selectedChip) }

    val localGridItem: @Composable (LocalItem) -> Unit = { item ->
        when (item) {
            is Song -> SongGridItem(
                song = item,
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = {
                        if (item.id == mediaMetadata?.id) playerConnection.togglePlayPause()
                        else playerConnection.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show { SongMenu(originalSong = item, navController = navController, onDismiss = menuState::dismiss) }
                    },
                ),
                isActive = item.id == mediaMetadata?.id, isPlaying = isPlaying,
            )
            is com.voidplayer.music.db.entities.Album -> AlbumGridItem(
                album = item, isActive = item.id == mediaMetadata?.album?.id, isPlaying = isPlaying, coroutineScope = scope,
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { navController.navigate("album/${item.id}") },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show { AlbumMenu(originalAlbum = item, navController = navController, onDismiss = menuState::dismiss) }
                    }
                )
            )
            is com.voidplayer.music.db.entities.Artist -> ArtistGridItem(
                artist = item,
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { navController.navigate("artist/${item.id}") },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show { ArtistMenu(originalArtist = item, coroutineScope = scope, onDismiss = menuState::dismiss) }
                    },
                ),
            )
            else -> {}
        }
    }

    val ytGridItem: @Composable (YTItem) -> Unit = { item ->
        YouTubeGridItem(
            item = item, isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id), isPlaying = isPlaying, coroutineScope = scope, thumbnailRatio = 1f,
            modifier = Modifier.combinedClickable(
                onClick = {
                    when (item) {
                        is SongItem -> {
                            if (item.id == mediaMetadata?.id) {
                                playerConnection.togglePlayPause()
                            } else {
                                playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                            }
                        }
                        is AlbumItem -> navController.navigate("album/${item.id}")
                        is ArtistItem -> navController.navigate("artist/${item.id}")
                        is PlaylistItem -> navController.navigateToPlaylistItem(item)
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        when (item) {
                            is SongItem -> YouTubeSongMenu(song = item, navController = navController, onDismiss = menuState::dismiss)
                            is AlbumItem -> YouTubeAlbumMenu(albumItem = item, navController = navController, onDismiss = menuState::dismiss)
                            is ArtistItem -> YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
                            is PlaylistItem -> YouTubePlaylistMenu(playlist = item, coroutineScope = scope, onDismiss = menuState::dismiss)
                        }
                    }
                }
            )
        )
    }

    val homeSections = remember(randomizeHomeOrder, randomSeed, quickPicks, dailyDiscover, keepListening, accountPlaylists, libraryPlaylists, forgottenFavorites, communityPlaylists, similarRecommendations, homePage?.sections) {
        val list = mutableListOf<HomeSection>()
        if (libraryPlaylists.isNotEmpty()) list.add(HomeSection.LibraryPlaylists)
        if (keepListening?.isNotEmpty() == true) list.add(HomeSection.KeepListening)
        if (quickPicks?.isNotEmpty() == true) list.add(HomeSection.QuickPicks)
        if (communityPlaylists?.isNotEmpty() == true) list.add(HomeSection.FromTheCommunity)
        if (dailyDiscover?.isNotEmpty() == true) list.add(HomeSection.DailyDiscover)
        if (accountPlaylists?.isNotEmpty() == true) list.add(HomeSection.AccountPlaylists)
        if (forgottenFavorites?.isNotEmpty() == true) list.add(HomeSection.ForgottenFavorites)
        similarRecommendations?.indices?.forEach { i -> list.add(HomeSection.SimilarRecommendation(i)) }
        homePage?.sections?.indices?.forEach { i -> list.add(HomeSection.HomePageSection(i)) }

        if (randomizeHomeOrder) {
            list.sortedByDescending { section ->
                val sectionRandom = Random(randomSeed + section.id.hashCode())
                val base = when (section) {
                    HomeSection.LibraryPlaylists -> 20000
                    HomeSection.KeepListening -> 10000
                    HomeSection.QuickPicks -> 1000
                    HomeSection.DailyDiscover -> 500
                    HomeSection.AccountPlaylists, HomeSection.ForgottenFavorites, HomeSection.FromTheCommunity -> 300
                    else -> 100
                }
                val modifier = when (section) {
                    HomeSection.LibraryPlaylists, HomeSection.KeepListening -> 0
                    HomeSection.QuickPicks -> 0
                    HomeSection.DailyDiscover -> sectionRandom.nextInt(-200, 400)
                    HomeSection.AccountPlaylists, HomeSection.ForgottenFavorites, HomeSection.FromTheCommunity -> sectionRandom.nextInt(-100, 400)
                    else -> sectionRandom.nextInt(-50, 50)
                }
                base + modifier
            }
        } else {
            val defaultOrder = mapOf(HomeSection.LibraryPlaylists to 20000, HomeSection.KeepListening to 10000, HomeSection.QuickPicks to 1000, HomeSection.FromTheCommunity to 80, HomeSection.DailyDiscover to 70, HomeSection.AccountPlaylists to 50, HomeSection.ForgottenFavorites to 40)
            list.sortedByDescending { section ->
                when (section) {
                    is HomeSection.SimilarRecommendation -> 30 - section.index
                    is HomeSection.HomePageSection -> 20 - section.index
                    else -> defaultOrder[section] ?: 0
                }
            }
        }
    }

    PullToRefreshBox(
        state = pullRefreshState, isRefreshing = isRefreshing, onRefresh = viewModel::refresh,
        indicator = { PullToRefreshDefaults.LoadingIndicator(state = pullRefreshState, isRefreshing = isRefreshing, modifier = Modifier.align(Alignment.TopCenter).padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())) }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopStart) {
            val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
            val quickPicksSnapLayoutInfoProvider = remember(quickPicksLazyGridState) { SnapLayoutInfoProvider(lazyGridState = quickPicksLazyGridState, positionInLayout = { layoutSize, itemSize -> (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f) }) }
            val forgottenFavoritesSnapLayoutInfoProvider = remember(forgottenFavoritesLazyGridState) { SnapLayoutInfoProvider(lazyGridState = forgottenFavoritesLazyGridState, positionInLayout = { layoutSize, itemSize -> (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f) }) }

            LazyColumn(state = lazylistState, contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(), modifier = Modifier.fillMaxSize()) {
                item(key = "chips", contentType = "chips") {
                    ChipsRow(
                        chips = homePage?.chips?.filter { !it.title.equals("Podcasts", ignoreCase = true) && !it.title.equals("Uploaded", ignoreCase = true) }?.map { it to it.title } ?: emptyList(),
                        currentValue = selectedChip, onValueUpdate = { viewModel.toggleChip(it) }
                    )
                }

                if (isLoading && homePage?.chips.isNullOrEmpty()) {
                    item(key = "chips_shimmer", contentType = "shimmer") {
                        ShimmerHost {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(5) { TextPlaceholder(height = 30.dp, shape = RoundedCornerShape(16.dp), modifier = Modifier.width(72.dp)) }
                            }
                        }
                    }
                }

                homeSections.forEach { section ->
                    when (section) {
                        HomeSection.LibraryPlaylists -> {
                            libraryPlaylists.takeIf { it.isNotEmpty() }?.let { playlists ->
                                item(key = "library_playlists_title", contentType = "title") {
                                    NavigationTitle(
                                        title = stringResource(R.string.playlists),
                                        modifier = Modifier.animateItem()
                                    )
                                }
                                item(key = "library_playlists_grid", contentType = "horizontal_grid") {
                                    val rows = if (playlists.size >= 4) 4 else playlists.size.coerceAtLeast(1)
                                    val gridHeight = (rows * 44 + (rows - 1) * 8).dp
                                    LazyHorizontalGrid(
                                        rows = GridCells.Fixed(rows),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(gridHeight)
                                            .animateItem()
                                    ) {
                                        items(playlists.distinctBy { it.id }, key = { it.id }) { playlist ->
                                            val coverUrl = playlist.thumbnails.firstOrNull() ?: playlist.thumbnailUrl
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                modifier = Modifier
                                                    .width(220.dp)
                                                    .height(44.dp)
                                                    .combinedClickable(
                                                        onClick = {
                                                            val isSpotify = playlist.id.startsWith("SPOTIFY_PLAYLIST_") || playlist.playlist.browseId?.startsWith("SPOTIFY_PLAYLIST_") == true
                                                            val isOnlineYouTube = !isSpotify && (
                                                                playlist.id.startsWith("PL") || 
                                                                playlist.id.startsWith("VL") || 
                                                                playlist.id.startsWith("RD") || 
                                                                playlist.id.startsWith("MP") || 
                                                                (!playlist.playlist.isLocal && !playlist.playlist.browseId.isNullOrBlank())
                                                            )

                                                            if (isOnlineYouTube) {
                                                                val targetBrowseId = (playlist.playlist.browseId?.takeIf { !it.isNullOrBlank() } ?: playlist.id).removePrefix("VL")
                                                                navController.navigate("online_playlist/$targetBrowseId")
                                                            } else {
                                                                navController.navigate("local_playlist/${playlist.id}")
                                                            }
                                                        },
                                                        onLongClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            menuState.show {
                                                                DropdownMenuItem(
                                                                    text = { Text("Remove from Recent Playlists") },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            painter = painterResource(R.drawable.close),
                                                                            contentDescription = null
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        menuState.dismiss()
                                                                        scope.launch(Dispatchers.IO) {
                                                                            database.update(playlist.playlist.copy(lastUpdateTime = null))
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    if (!coverUrl.isNullOrBlank()) {
                                                        AsyncImage(
                                                            model = coverUrl,
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(R.drawable.playlist_play),
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                    }
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(horizontal = 8.dp)
                                                            .weight(1f)
                                                    ) {
                                                        Text(
                                                            text = playlist.title,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = pluralStringResource(
                                                                R.plurals.n_song,
                                                                playlist.realSongCount,
                                                                playlist.realSongCount
                                                            ),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HomeSection.QuickPicks -> {
                            quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                                item(key = "quick_picks_list", contentType = "carousel") {
                                    val distinctQuickPicks = quickPicks.distinctBy { it.id }
                                    HorizontalCenteredHeroCarousel(
                                        state = rememberCarouselState { distinctQuickPicks.size }, maxItemWidth = 250.dp, itemSpacing = 8.dp, contentPadding = PaddingValues(horizontal = 16.dp), modifier = Modifier.fillMaxWidth().height(290.dp).animateItem()
                                    ) { index ->
                                        val originalSong = distinctQuickPicks[index]
                                        val song by database.song(originalSong.id).collectAsState(initial = originalSong)
                                        val isActive = song!!.id == mediaMetadata?.id
                                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).focusable().combinedClickable(onClick = { if (isActive) playerConnection.togglePlayPause() else playerConnection.playQueue(YouTubeQueue.radio(song!!.toMediaMetadata())) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menuState.show { SongMenu(originalSong = song!!, navController = navController, onDismiss = menuState::dismiss) } })) {
                                            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(song!!.thumbnailUrl).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent, VoidColors.Bg.copy(alpha = 0.85f)))))
                                            if (isActive && isPlaying) {
                                                Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                                    Icon(painter = painterResource(R.drawable.volume_up), contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                                Text(text = song!!.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VoidColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(text = song!!.artists.joinToString { it.name }, style = MaterialTheme.typography.bodyMedium, color = VoidColors.Text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HomeSection.FromTheCommunity -> {
                            communityPlaylists?.takeIf { it.isNotEmpty() }?.let { playlists ->
                                item(key = "community_playlists_title", contentType = "title") { NavigationTitle(title = stringResource(R.string.from_the_community), modifier = Modifier.animateItem()) }
                                item(key = "community_playlists_content", contentType = "horizontal_list") {
                                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.animateItem()) {
                                        items(playlists, key = { it.playlist.id }, contentType = { "community_card" }) { item ->
                                            CommunityPlaylistCard(item = item, onClick = { navController.navigateToPlaylistItem(item.playlist) }, onSongClick = { song -> playerConnection.playQueue(YouTubeQueue(song.endpoint ?: WatchEndpoint(videoId = song.id), song.toMediaMetadata())) })
                                        }
                                    }
                                }
                            }
                        }
                        HomeSection.DailyDiscover -> {
                            dailyDiscover?.takeIf { it.isNotEmpty() }?.let { discoverList ->
                                item(key = "daily_discover_title", contentType = "title") {
                                    val title = stringResource(R.string.your_daily_discover)
                                    NavigationTitle(title = title, onPlayAllClick = { val queueItems = discoverList.mapNotNull { (it.recommendation as? SongItem)?.toMediaMetadata() }; if (queueItems.isNotEmpty()) { playerConnection.playQueue(ListQueue(title = title, items = queueItems.map { it.toMediaItem() })) } })
                                }
                                item(key = "daily_discover_content", contentType = "carousel") {
                                    Box(modifier = Modifier.fillMaxWidth().height(340.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                        val carouselState = rememberCarouselState { discoverList.size }
                                        HorizontalMultiBrowseCarousel(state = carouselState, preferredItemWidth = 320.dp, itemSpacing = 16.dp, modifier = Modifier.fillMaxWidth().height(320.dp)) { i ->
                                            val item = discoverList[i]
                                            DailyDiscoverCard(dailyDiscover = item, onClick = { val song = item.recommendation as? SongItem; val md = song?.toMediaMetadata(); if (md != null) { playerConnection.playQueue(YouTubeQueue(song.endpoint ?: WatchEndpoint(videoId = song.id), md)) } }, navController = navController, modifier = Modifier.clip(RoundedCornerShape(24.dp)))
                                        }
                                    }
                                }
                            }
                        }
                        HomeSection.KeepListening -> {
                            keepListening?.takeIf { it.isNotEmpty() }?.let { keepListening ->
                                item(key = "keep_listening_title", contentType = "title") { NavigationTitle(title = stringResource(R.string.keep_listening), modifier = Modifier.animateItem()) }
                                item(key = "keep_listening_list", contentType = "horizontal_grid") {
                                    val rows = if (keepListening.size > 6) 2 else 1
                                    LazyHorizontalGrid(state = rememberLazyGridState(), rows = GridCells.Fixed(rows), contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(), modifier = Modifier.fillMaxWidth().height((currentGridHeight + with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 2 + MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2 }) * rows).animateItem()) {
                                        items(keepListening, key = { it.id }, contentType = { "local_item" }) { localGridItem(it) }
                                    }
                                }
                            }
                        }
                        HomeSection.AccountPlaylists -> {
                            accountPlaylists?.takeIf { it.isNotEmpty() }?.let { accountPlaylists ->
                                item(key = "account_playlists_title", contentType = "title") {
                                    NavigationTitle(label = stringResource(R.string.your_youtube_playlists), title = accountName, thumbnail = { if (url != null) { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).diskCachePolicy(CachePolicy.ENABLED).diskCacheKey(url).crossfade(false).build(), placeholder = painterResource(id = R.drawable.person), error = painterResource(id = R.drawable.person), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(ListThumbnailSize).clip(CircleShape)) } else { Icon(painter = painterResource(id = R.drawable.person), contentDescription = null, modifier = Modifier.size(ListThumbnailSize)) } }, onClick = { navController.navigate("account") }, modifier = Modifier.animateItem())
                                }
                                item(key = "account_playlists_list", contentType = "horizontal_list") {
                                    LazyRow(contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(), modifier = Modifier.animateItem()) {
                                        items(items = accountPlaylists.distinctBy { it.id }, key = { it.id }, contentType = { "yt_item" }) { item -> ytGridItem(item) }
                                    }
                                }
                            }
                        }
                        HomeSection.ForgottenFavorites -> {
                            forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                                item(key = "forgotten_favorites_title", contentType = "title") {
                                    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
                                    NavigationTitle(title = forgottenFavoritesTitle, modifier = Modifier.animateItem(), onPlayAllClick = { playerConnection.playQueue(ListQueue(title = forgottenFavoritesTitle, items = forgottenFavorites.distinctBy { it.id }.map { it.toMediaItem() })) })
                                }
                                item(key = "forgotten_favorites_list", contentType = "horizontal_grid") {
                                    val rows = min(4, forgottenFavorites.size)
                                    LazyHorizontalGrid(state = forgottenFavoritesLazyGridState, rows = GridCells.Fixed(rows), contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(), flingBehavior = rememberSnapFlingBehavior(forgottenFavoritesSnapLayoutInfoProvider), modifier = Modifier.fillMaxWidth().height(ListItemHeight * rows).animateItem()) {
                                        itemsIndexed(items = forgottenFavorites.distinctBy { it.id }, key = { _, it -> it.id }, contentType = { _, _ -> "song" }) { index, originalSong ->
                                            val song by database.song(originalSong.id).collectAsState(initial = originalSong)
                                            SongListItem(
                                                song = song!!, showInLibraryIcon = true, isActive = song!!.id == mediaMetadata?.id, isPlaying = isPlaying, isSwipeable = false, shape = listItemShape(index = index % rows, count = rows),
                                                trailingContent = { IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menuState.show { SongMenu(originalSong = song!!, navController = navController, onDismiss = menuState::dismiss) } }) { Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null) } },
                                                modifier = Modifier.width(horizontalLazyGridItemWidth).combinedClickable(onClick = { if (song!!.id == mediaMetadata?.id) playerConnection.togglePlayPause() else playerConnection.playQueue(YouTubeQueue.radio(song!!.toMediaMetadata())) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menuState.show { SongMenu(originalSong = song!!, navController = navController, onDismiss = menuState::dismiss) } })
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is HomeSection.SimilarRecommendation -> {
                            val recommendation = similarRecommendations?.getOrNull(section.index)
                            recommendation?.let {
                                item(key = "similar_to_title_${section.index}", contentType = "title") {
                                    NavigationTitle(label = stringResource(R.string.similar_to), title = recommendation.title.title, thumbnail = recommendation.title.thumbnailUrl?.let { thumbnailUrl -> { val shape = if (recommendation.title is com.voidplayer.music.db.entities.Artist) CircleShape else RoundedCornerShape(ThumbnailCornerRadius); AsyncImage(model = thumbnailUrl, contentDescription = null, modifier = Modifier.size(ListThumbnailSize).clip(shape)) } }, onClick = { when (recommendation.title) { is Song -> navController.navigate("album/${recommendation.title.album!!.id}"); is com.voidplayer.music.db.entities.Album -> navController.navigate("album/${recommendation.title.id}"); is com.voidplayer.music.db.entities.Artist -> navController.navigate("artist/${recommendation.title.id}"); else -> {} } }, modifier = Modifier.animateItem())
                                }
                                item(key = "similar_to_list_${section.index}", contentType = "horizontal_list") {
                                    LazyRow(contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(), modifier = Modifier.animateItem()) {
                                        items(recommendation.items.distinctBy { it.id }, key = { it.id }, contentType = { "yt_item" }) { item -> ytGridItem(item) }
                                    }
                                }
                            }
                        }
                        is HomeSection.HomePageSection -> {
                            val sectionData = homePage?.sections?.getOrNull(section.index)
                            sectionData?.let {
                                val sectionSongs = sectionData.items.filterIsInstance<SongItem>()
                                val hasPlayableSongs = sectionSongs.isNotEmpty()
                                val isSongsOnlySection = sectionData.items.isNotEmpty() && sectionData.items.all { it is SongItem }
                                item(key = "home_section_title_${section.index}", contentType = "title") {
                                    NavigationTitle(title = sectionData.title, label = sectionData.label, thumbnail = sectionData.thumbnail?.let { thumbnailUrl -> { val shape = if (sectionData.endpoint?.isArtistEndpoint == true) CircleShape else RoundedCornerShape(ThumbnailCornerRadius); AsyncImage(model = thumbnailUrl, contentDescription = null, modifier = Modifier.size(ListThumbnailSize).clip(shape)) } }, onClick = sectionData.endpoint?.let { endpoint -> { when { endpoint.browseId == "FEmusic_moods_and_genres" -> navController.navigate("mood_and_genres"); endpoint.params != null -> navController.navigate("youtube_browse/${endpoint.browseId}?params=${endpoint.params}"); else -> navController.navigate("browse/${endpoint.browseId}") } } }, onPlayAllClick = if (hasPlayableSongs) { { playerConnection.playQueue(ListQueue(title = sectionData.title, items = sectionSongs.map { it.toMediaMetadata().toMediaItem() })) } } else null, modifier = Modifier.animateItem())
                                }
                                if (isSongsOnlySection) {
                                    item(key = "home_section_list_${section.index}", contentType = "horizontal_grid") {
                                        LazyHorizontalGrid(state = rememberLazyGridState(), rows = GridCells.Fixed(4), contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(), modifier = Modifier.fillMaxWidth().height(ListItemHeight * 4).animateItem()) {
                                            itemsIndexed(items = sectionSongs.distinctBy { it.id }, key = { _, it -> it.id }, contentType = { _, _ -> "yt_item" }) { index, song ->
                                                YouTubeListItem(item = song, isActive = song.id == mediaMetadata?.id, isPlaying = isPlaying, isSwipeable = false, shape = listItemShape(index = index % 4, count = 4), trailingContent = { IconButton(onClick = { menuState.show { YouTubeSongMenu(song = song, navController = navController, onDismiss = menuState::dismiss) } }) { Icon(painter = painterResource(R.drawable.more_vert), contentDescription = null) } }, modifier = Modifier.width(horizontalLazyGridItemWidth).combinedClickable(onClick = { if (song.id == mediaMetadata?.id) playerConnection.togglePlayPause() else playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata())) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); menuState.show { YouTubeSongMenu(song = song, navController = navController, onDismiss = menuState::dismiss) } }))
                                            }
                                        }
                                    }
                                } else {
                                    item(key = "home_section_list_${section.index}", contentType = "horizontal_list") {
                                        LazyRow(contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(), modifier = Modifier.animateItem()) {
                                            items(sectionData.items.distinctBy { it.id }, key = { it.id }, contentType = { "yt_item" }) { item -> ytGridItem(item) }
                                        }
                                    }
                                }
                            }
                        }
                        HomeSection.MoodAndGenres -> {}
                    }
                }

                if (isLoading || (homePage?.continuation != null && homePage?.sections?.isNotEmpty() == true)) {
                    item(key = "bottom_loading_spinner", contentType = "spinner") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
                item(key = "bottom_spacer", contentType = "spacer") { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}
