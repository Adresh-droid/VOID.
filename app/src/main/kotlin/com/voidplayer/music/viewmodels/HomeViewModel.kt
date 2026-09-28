

package com.voidplayer.music.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidplayer.music.innertube.YouTube
import com.voidplayer.music.innertube.models.AlbumItem
import com.voidplayer.music.innertube.models.Artist
import com.voidplayer.music.innertube.models.PlaylistItem
import com.voidplayer.music.innertube.models.SongItem
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import com.voidplayer.music.innertube.models.WatchEndpoint
import com.voidplayer.music.innertube.models.BrowseEndpoint
import com.voidplayer.music.innertube.models.YTItem
import com.voidplayer.music.innertube.models.filterExplicit
import com.voidplayer.music.innertube.models.filterVideoSongs
import com.voidplayer.music.innertube.models.filterYoutubeShorts
import com.voidplayer.music.innertube.pages.ExplorePage
import com.voidplayer.music.innertube.pages.HomePage
import com.voidplayer.music.innertube.utils.completed
import com.voidplayer.music.constants.HideExplicitKey
import com.voidplayer.music.constants.HideVideoSongsKey
import com.voidplayer.music.constants.HideYoutubeShortsKey
import com.voidplayer.music.constants.InnerTubeCookieKey
import com.voidplayer.music.constants.QuickPicks
import com.voidplayer.music.constants.QuickPicksKey
import com.voidplayer.music.db.MusicDatabase
import com.voidplayer.music.db.entities.Album
import com.voidplayer.music.db.entities.LocalItem
import com.voidplayer.music.db.entities.Song
import com.voidplayer.music.db.entities.SpeedDialItem
import com.voidplayer.music.extensions.filterVideoSongs
import com.voidplayer.music.extensions.toEnum
import com.voidplayer.music.models.SimilarRecommendation
import com.voidplayer.music.utils.SyncUtils
import com.voidplayer.music.utils.dataStore
import com.voidplayer.music.utils.get
import com.voidplayer.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

data class DailyDiscoverItem(
    val seed: Song,
    val recommendation: YTItem,
    val relatedEndpoint: BrowseEndpoint?
)

data class CommunityPlaylistItem(
    val playlist: PlaylistItem,
    val songs: List<SongItem>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val database: MusicDatabase,
    val syncUtils: SyncUtils,
) : ViewModel() {
    val isRefreshing = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)
    val isRandomizing = MutableStateFlow(false)

    private val quickPicksEnum = context.dataStore.data.map {
        it[QuickPicksKey].toEnum(QuickPicks.QUICK_PICKS)
    }.distinctUntilChanged()

    val quickPicks = MutableStateFlow<List<Song>?>(null)
    val dailyDiscover = MutableStateFlow<List<DailyDiscoverItem>?>(null)
    val forgottenFavorites = MutableStateFlow<List<Song>?>(null)
    val keepListening = MutableStateFlow<List<LocalItem>?>(null)
    val similarRecommendations = MutableStateFlow<List<SimilarRecommendation>?>(null)
    val accountPlaylists = MutableStateFlow<List<PlaylistItem>?>(null)
    val homePage = MutableStateFlow<HomePage?>(null)
    val explorePage = MutableStateFlow<ExplorePage?>(null)
    val communityPlaylists = MutableStateFlow<List<CommunityPlaylistItem>?>(null)
    val selectedChip = MutableStateFlow<HomePage.Chip?>(null)
    private val previousHomePage = MutableStateFlow<HomePage?>(null)


    val libraryPlaylists = database.allRecentPlaylists()
        .map { playlists ->
            playlists.distinctBy { (it.playlist.browseId ?: it.playlist.name).lowercase() }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allLocalItems = MutableStateFlow<List<LocalItem>>(emptyList())
    val allYtItems = MutableStateFlow<List<YTItem>>(emptyList())

    val speedDialItems: StateFlow<List<YTItem>> =
        combine(
            database.speedDialDao.getAll(),
            keepListening,
            quickPicks
        ) { pinned, keepListening, quick ->
            val pinnedItems = pinned.map { it.toYTItem() }
            val filled = pinnedItems.toMutableList()
            val targetSize = 27

            if (filled.size < targetSize) {
                
                keepListening?.let { k ->
                    val needed = targetSize - filled.size
                    val available = k.filter { item ->
                        filled.none { p -> p.id == item.id }
                    }.mapNotNull { item ->
                        when (item) {
                            is Song -> SongItem(
                                id = item.id,
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                thumbnail = item.thumbnailUrl ?: "",
                                explicit = false
                            )
                            is Album -> AlbumItem(
                                browseId = item.id,
                                playlistId = item.album.playlistId ?: "",
                                title = item.title,
                                artists = item.artists.map { Artist(name = it.name, id = it.id) },
                                year = item.album.year,
                                thumbnail = item.thumbnailUrl ?: ""
                            )
                            else -> null
                        }
                    }
                    filled.addAll(available.take(needed))
                }
            }

            if (filled.size < targetSize) {
                
                quick?.let { q ->
                    val needed = targetSize - filled.size
                    val available = q.filter { song ->
                        filled.none { p -> p.id == song.id }
                    }.map { song ->
                        SongItem(
                            id = song.id,
                            title = song.title,
                            artists = song.artists.map { Artist(name = it.name, id = it.id) },
                            thumbnail = song.thumbnailUrl ?: "",
                            explicit = false
                        )
                    }
                    filled.addAll(available.take(needed))
                }
            }

            filled.take(targetSize)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun getRandomItem(): YTItem? {
        try {
            isRandomizing.value = true
            
            kotlinx.coroutines.delay(1000)

            val userSongs = mutableListOf<YTItem>()
            val otherSources = mutableListOf<YTItem>()

            quickPicks.value?.let { songs ->
                userSongs.addAll(songs.map { song ->
                    SongItem(
                        id = song.id,
                        title = song.title,
                        artists = song.artists.map { Artist(name = it.name, id = it.id) },
                        thumbnail = song.thumbnailUrl ?: "",
                        explicit = false
                    )
                })
            }

            keepListening.value?.let { items ->
                items.forEach { item ->
                    when (item) {
                        is Song -> userSongs.add(SongItem(
                            id = item.id,
                            title = item.title,
                            artists = item.artists.map { Artist(name = it.name, id = it.id) },
                            thumbnail = item.thumbnailUrl ?: "",
                            explicit = false
                        ))
                        is Album -> otherSources.add(AlbumItem(
                            browseId = item.id,
                            playlistId = item.album.playlistId ?: "",
                            title = item.title,
                            artists = item.artists.map { Artist(name = it.name, id = it.id) },
                            year = item.album.year,
                            thumbnail = item.thumbnailUrl ?: ""
                        ))
                        else -> {}
                    }
                }
            }

            otherSources.addAll(allYtItems.value)

            
            val item = if (userSongs.isNotEmpty() && (otherSources.isEmpty() || Random.nextFloat() < 0.8f)) {
                userSongs.distinctBy { it.id }.shuffled().firstOrNull()
            } else {
                otherSources.distinctBy { it.id }.shuffled().firstOrNull()
            } ?: userSongs.firstOrNull() ?: otherSources.firstOrNull()

            return item
        } finally {
            isRandomizing.value = false
        }
    }

    val accountName = MutableStateFlow("Guest")
    val accountImageUrl = MutableStateFlow<String?>(null)

    fun togglePin(item: YTItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val speedDialItem = SpeedDialItem.fromYTItem(item)
            val isPinned = database.speedDialDao.isPinned(speedDialItem.id).first()
            if (isPinned) {
                database.speedDialDao.delete(speedDialItem.id)
            } else {
                database.speedDialDao.insert(speedDialItem)
            }
        }
    }
    
    private var lastProcessedCookie: String? = null
    
    private var isProcessingAccountData = false

    private suspend fun getDailyDiscover() {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val likedSongs = database.likedSongsByCreateDateAsc().first()
        if (likedSongs.isEmpty()) return

        val seeds = likedSongs.shuffled().distinctBy { it.id }.take(5)

        
        val items = java.util.Collections.synchronizedList(mutableListOf<DailyDiscoverItem>())

        kotlinx.coroutines.coroutineScope {
            seeds.map { seed ->
                launch(Dispatchers.IO) {
                    val endpoint = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
                    if (endpoint != null) {
                        YouTube.related(endpoint).onSuccess { page ->
                            val recommendations = page.songs
                                .filter { item ->
                                    if (hideVideoSongs && item.isVideoSong) return@filter false
                                    if (item.explicit) return@filter false
                                    true
                                }
                                .shuffled()

                            
                            val recommendation = recommendations.firstOrNull { rec ->
                                rec.id != seed.id
                            }

                            if (recommendation != null) {
                                items.add(
                                    DailyDiscoverItem(
                                        seed = seed,
                                        recommendation = recommendation,
                                        relatedEndpoint = endpoint
                                    )
                                )
                            }
                        }
                    }
                }
            }.forEach { it.join() }
        }

        
        dailyDiscover.value = items.toList().distinctBy { it.recommendation.id }.shuffled()
    }

    private suspend fun getQuickPicks() = coroutineScope {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        when (quickPicksEnum.first()) {
            QuickPicks.QUICK_PICKS -> {
                val relatedSongsJob = async { database.quickPicks().first().filterVideoSongs(hideVideoSongs) }
                val forgottenJob = async { database.forgottenFavorites().first().filterVideoSongs(hideVideoSongs).take(8) }
                val recentSongJob = async { database.events().first().firstOrNull()?.song }

                val recentSong = recentSongJob.await()
                val ytSimilarSongs = mutableListOf<Song>()

                if (recentSong != null) {
                    val endpoint = YouTube.next(WatchEndpoint(videoId = recentSong.id)).getOrNull()?.relatedEndpoint
                    if (endpoint != null) {
                        YouTube.related(endpoint).onSuccess { page ->
                            page.songs.take(10).forEach { ytSong ->
                                database.song(ytSong.id).first()?.let { localSong ->
                                    if (!hideVideoSongs || !localSong.song.isVideo) {
                                        ytSimilarSongs.add(localSong)
                                    }
                                }
                            }
                        }
                    }
                }

                val relatedSongs = relatedSongsJob.await()
                val forgotten = forgottenJob.await()
                
                val combined = (relatedSongs + forgotten + ytSimilarSongs)
                    .distinctBy { it.id }
                    .shuffled()
                    .take(20)

                quickPicks.value = combined.ifEmpty { relatedSongs.shuffled().take(20) }
            }
            QuickPicks.LAST_LISTEN -> {
                val song = database.events().first().firstOrNull()?.song
                if (song != null && database.hasRelatedSongs(song.id)) {
                    quickPicks.value = database.getRelatedSongs(song.id).first().filterVideoSongs(hideVideoSongs).shuffled().take(20)
                }
            }
        }
    }

    private suspend fun getCommunityPlaylists() {
        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 4
        val artistSeeds = database.mostPlayedArtists(fromTimeStamp, limit = 10).first()
            .filter { it.artist.isYouTubeArtist }
            .shuffled().take(3)
        val songSeeds = database.mostPlayedSongs(fromTimeStamp, limit = 5).first()
            .shuffled().take(2)

        val candidatePlaylists = java.util.Collections.synchronizedList(mutableListOf<PlaylistItem>())

        kotlinx.coroutines.coroutineScope {
            artistSeeds.map { seed ->
                launch(Dispatchers.IO) {
                    YouTube.artist(seed.id).onSuccess { page ->
                        page.sections.forEach { section ->
                            section.items.filterIsInstance<PlaylistItem>().forEach { playlist ->
                                if (playlist.author?.name != "YouTube Music" &&
                                    playlist.author?.name != "YouTube" &&
                                    playlist.author?.name != "Playlist" &&
                                    playlist.author?.name != seed.artist.name &&
                                    !playlist.id.startsWith("RD") &&
                                    !playlist.id.startsWith("OLAK")
                                ) {
                                    candidatePlaylists.add(playlist)
                                }
                            }
                        }
                    }
                }
            }

            songSeeds.map { seed ->
                launch(Dispatchers.IO) {
                    val endpoint = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
                    if (endpoint != null) {
                        YouTube.related(endpoint).onSuccess { page ->
                            page.playlists.forEach { playlist ->
                                if (playlist.author?.name != "YouTube Music" &&
                                    playlist.author?.name != "YouTube" &&
                                    playlist.author?.name != "Playlist" &&
                                    !playlist.id.startsWith("RD") &&
                                    !playlist.id.startsWith("OLAK")
                                ) {
                                    candidatePlaylists.add(playlist)
                                }
                            }
                        }
                    }
                }
            }
        }

        val uniqueCandidates = candidatePlaylists.distinctBy { it.id }.shuffled().take(5)

        val playlists = java.util.Collections.synchronizedList(mutableListOf<CommunityPlaylistItem>())

        kotlinx.coroutines.coroutineScope {
            uniqueCandidates.map { playlist ->
                launch(Dispatchers.IO) {
                    YouTube.playlist(playlist.id).onSuccess { page ->
                        val songs = page.songs.take(10)
                        if (songs.isNotEmpty()) {
                            
                            val songCountText = page.playlist.songCountText ?: playlist.songCountText
                            val updatedPlaylist = playlist.copy(songCountText = songCountText)
                            playlists.add(CommunityPlaylistItem(updatedPlaylist, songs))
                        }
                    }
                }
            }.forEach { it.join() }
        }

        communityPlaylists.value = playlists.shuffled()
    }

    
    private suspend fun loadLocalDataPhase() = coroutineScope {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)

        val quickPicksJob = launch { getQuickPicks() }
        val forgottenJob = async {
            database.forgottenFavorites().first()
                .filterVideoSongs(hideVideoSongs).shuffled().take(20)
        }

        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2
        val keepListeningSongsJob = async {
            database.mostPlayedSongs(fromTimeStamp, limit = 15, offset = 5).first()
                .filterVideoSongs(hideVideoSongs).shuffled().take(10)
        }
        val keepListeningAlbumsJob = async {
            database.mostPlayedAlbums(fromTimeStamp, limit = 8, offset = 2).first()
                .filter { it.album.thumbnailUrl != null }.shuffled().take(5)
        }
        val keepListeningArtistsJob = async {
            database.mostPlayedArtists(fromTimeStamp).first()
                .filter { it.artist.isYouTubeArtist && it.artist.thumbnailUrl != null }.shuffled().take(5)
        }

        quickPicksJob.join()
        forgottenFavorites.value = forgottenJob.await()
        val songs = keepListeningSongsJob.await()
        val albums = keepListeningAlbumsJob.await()
        val artists = keepListeningArtistsJob.await()
        keepListening.value = (songs + albums + artists).shuffled()

        allLocalItems.value = (quickPicks.value.orEmpty() + forgottenFavorites.value.orEmpty() + keepListening.value.orEmpty())
            .filter { it is Song || it is Album }
    }

    
    private suspend fun loadSimilarRecommendations() {
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val fromTimeStamp = System.currentTimeMillis() - 86400000L * 7 * 2

        coroutineScope {
            val artistDeferreds = database.mostPlayedArtists(fromTimeStamp, limit = 15).first()
                .filter { it.artist.isYouTubeArtist }
                .shuffled().take(4)
                .map { artist ->
                    async(Dispatchers.IO) {
                        val items = mutableListOf<YTItem>()
                        YouTube.artist(artist.id).onSuccess { page ->
                            page.sections.takeLast(3).forEach { section -> items += section.items }
                        }
                        SimilarRecommendation(
                            title = artist,
                            items = items
                                .distinctBy { item -> item.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .shuffled()
                                .take(12)
                                .ifEmpty { return@async null }
                        )
                    }
                }

            val songDeferreds = database.mostPlayedSongs(fromTimeStamp, limit = 15).first()
                .filter { it.album != null }
                .shuffled().take(3)
                .map { song ->
                    async(Dispatchers.IO) {
                        val endpoint = YouTube.next(WatchEndpoint(videoId = song.id)).getOrNull()?.relatedEndpoint
                            ?: return@async null
                        val page = YouTube.related(endpoint).getOrNull() ?: return@async null
                        SimilarRecommendation(
                            title = song,
                            items = (page.songs.shuffled().take(10) +
                                    page.albums.shuffled().take(5) +
                                    page.artists.shuffled().take(3) +
                                    page.playlists.shuffled().take(3))
                                .distinctBy { it.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .shuffled()
                                .ifEmpty { return@async null }
                        )
                    }
                }

            val albumDeferreds = database.mostPlayedAlbums(fromTimeStamp, limit = 10).first()
                .filter { it.album.thumbnailUrl != null }
                .shuffled().take(2)
                .map { album ->
                    async(Dispatchers.IO) {
                        val items = mutableListOf<YTItem>()
                        YouTube.album(album.id).onSuccess { page ->
                            page.otherVersions.let { items += it }
                        }
                        album.artists.firstOrNull()?.id?.let { artistId ->
                            YouTube.artist(artistId).onSuccess { page ->
                                page.sections.lastOrNull()?.items?.let { items += it }
                            }
                        }
                        SimilarRecommendation(
                            title = album,
                            items = items
                                .distinctBy { it.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .shuffled()
                                .take(10)
                                .ifEmpty { return@async null }
                        )
                    }
                }

            val results = (artistDeferreds + songDeferreds + albumDeferreds).awaitAll()
            similarRecommendations.value = results.filterNotNull().shuffled()
        }
    }

    
    private suspend fun loadNetworkDataPhase() {
        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)

        coroutineScope {
            launch(Dispatchers.IO) { getDailyDiscover() }
            launch(Dispatchers.IO) { getCommunityPlaylists() }
            launch(Dispatchers.IO) { loadSimilarRecommendations() }
            launch(Dispatchers.IO) {
                YouTube.home().onSuccess { page ->
                    homePage.value = page.copy(
                        sections = page.sections.mapNotNull { section ->
                            val filteredItems = section.items
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs)
                                .filterYoutubeShorts(hideYoutubeShorts)
                            if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
                        }
                    )
                }.onFailure { reportException(it) }
            }
            launch(Dispatchers.IO) {
                YouTube.explore().onSuccess { page ->
                    explorePage.value = page.copy(
                        newReleaseAlbums = page.newReleaseAlbums.filterExplicit(hideExplicit)
                    )
                }.onFailure { reportException(it) }
            }
            if (YouTube.cookie != null) {
                launch(Dispatchers.IO) { loadAccountPlaylists() }
            }
        }

        
        allYtItems.value = similarRecommendations.value?.flatMap { it.items }.orEmpty() +
                homePage.value?.sections?.flatMap { it.items }.orEmpty()
    }

    private suspend fun load() = coroutineScope {
        isLoading.value = true
        
        // Start both phases immediately.
        val localJob = launch { loadLocalDataPhase() }
        val networkJob = launch { loadNetworkDataPhase() }
        
        // As soon as local items are ready, we can reveal the UI with placeholders for network data.
        localJob.join()
        if (allLocalItems.value.isNotEmpty()) {
            isLoading.value = false
        }
        
        networkJob.join()
        isLoading.value = false
    }

    private val _isLoadingMore = MutableStateFlow(false)
    fun loadMoreYouTubeItems(continuation: String?) {
        if (_isLoadingMore.value) return
        val currentCont = continuation ?: homePage.value?.continuation
        if (currentCont == null && homePage.value == null) return

        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)

        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingMore.value = true
            var currentContinuation = currentCont
            var hasNewItems = false

            if (currentContinuation != null && !currentContinuation.startsWith("ENDLESS_FEED")) {
                while (currentContinuation != null && !currentContinuation.startsWith("ENDLESS_FEED") && !hasNewItems) {
                    val nextSections = YouTube.home(currentContinuation).getOrNull()
                    if (nextSections == null) {
                        currentContinuation = null
                        break
                    }
                    currentContinuation = nextSections.continuation

                    val newSections = nextSections.sections.mapNotNull { section ->
                        val filteredItems = section.items.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs).filterYoutubeShorts(hideYoutubeShorts)
                        if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
                    }

                    if (newSections.isNotEmpty()) {
                        hasNewItems = true
                    }

                    homePage.value = nextSections.copy(
                        chips = homePage.value?.chips,
                        continuation = currentContinuation ?: "ENDLESS_FEED_${System.currentTimeMillis()}",
                        sections = homePage.value?.sections.orEmpty() + newSections
                    )
                }
            }

            if (!hasNewItems || currentContinuation == null || currentContinuation.startsWith("ENDLESS_FEED")) {
                val newSections = mutableListOf<HomePage.Section>()
                val topSongs = runCatching { database.topSongs(30).first().shuffled() }.getOrNull().orEmpty()
                val quickSongs = quickPicks.value.orEmpty().shuffled()
                val combinedSeeds = (topSongs.map { it.id to it.title } + quickSongs.map { it.id to it.title })
                    .distinctBy { it.first }
                    .take(8)

                for ((sId, sTitle) in combinedSeeds) {
                    val nextResult = YouTube.next(WatchEndpoint(videoId = sId)).getOrNull()
                    val items = nextResult?.items?.filterIsInstance<SongItem>()
                        ?.filterExplicit(hideExplicit)
                        ?.filterVideoSongs(hideVideoSongs)
                        ?.distinctBy { it.id }
                    if (!items.isNullOrEmpty()) {
                        newSections.add(
                            HomePage.Section(
                                title = "More Like $sTitle",
                                label = "Recommended for you",
                                thumbnail = items.firstOrNull()?.thumbnail,
                                endpoint = null,
                                items = items.take(16)
                            )
                        )
                    }
                }

                val nextCont = "ENDLESS_FEED_${System.currentTimeMillis()}"
                if (newSections.isNotEmpty()) {
                    homePage.value = (homePage.value ?: HomePage(chips = emptyList(), sections = emptyList(), continuation = nextCont)).let { current ->
                        current.copy(
                            continuation = nextCont,
                            sections = current.sections + newSections
                        )
                    }
                } else {
                    homePage.value = homePage.value?.copy(continuation = nextCont)
                }
            }
            _isLoadingMore.value = false
        }
    }

    fun toggleChip(chip: HomePage.Chip?) {
        if (chip == null || chip == selectedChip.value && previousHomePage.value != null) {
            homePage.value = previousHomePage.value
            previousHomePage.value = null
            selectedChip.value = null
            return
        }

        if (selectedChip.value == null) {
            previousHomePage.value = homePage.value
        }

        viewModelScope.launch(Dispatchers.IO) {
            val hideExplicit = context.dataStore.get(HideExplicitKey, false)
            val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
            val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
            val nextSections = YouTube.home(params = chip.endpoint?.params).getOrNull() ?: return@launch

            homePage.value = nextSections.copy(
                chips = homePage.value?.chips,
                sections = nextSections.sections.map { section ->
                    section.copy(items = section.items.filterExplicit(hideExplicit).filterVideoSongs(hideVideoSongs).filterYoutubeShorts(hideYoutubeShorts))
                }
            )
            selectedChip.value = chip
        }
    }

    private suspend fun loadAccountPlaylists() {
        val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
        YouTube.library("FEmusic_liked_playlists").completed().onSuccess {
            accountPlaylists.value = it.items.filterIsInstance<PlaylistItem>()
                .filterNot { it.id == "SE" }
                .filterYoutubeShorts(hideYoutubeShorts)
        }.onFailure {
            reportException(it)
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isRefreshing.value = true
                load()
            } finally {
                isRefreshing.value = false
            }
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            syncUtils.tryAutoSync()
        }
    }

    init {
        // Pre-load local data immediately without waiting for anything else.
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            loadLocalDataPhase()
            // Local data is enough to show the skeleton/list and stop the global spinner.
            if (allLocalItems.value.isNotEmpty()) {
                isLoading.value = false
            }
        }

        // Network data needs the cookie and settings to be stable.
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { it[InnerTubeCookieKey] }
                .distinctUntilChanged()
                .first()

            loadNetworkDataPhase()
            isLoading.value = false
        }

        viewModelScope.launch(Dispatchers.IO) {
            syncUtils.tryAutoSync()
        }

        
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { it[InnerTubeCookieKey] }
                .collect { cookie ->
                    
                    if (isProcessingAccountData) return@collect

                    
                    lastProcessedCookie = cookie
                    isProcessingAccountData = true

                    try {
                        if (cookie != null && cookie.isNotEmpty()) {

                            
                            YouTube.cookie = cookie

                            
                            YouTube.accountInfo().onSuccess { info ->
                                accountName.value = info.name
                                accountImageUrl.value = info.thumbnailUrl
                            }.onFailure {
                                reportException(it)
                            }
                        } else {
                            accountName.value = "Guest"
                            accountImageUrl.value = null
                            accountPlaylists.value = null
                        }
                    } finally {
                        isProcessingAccountData = false
                    }
                }
        }

        
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { it[HideYoutubeShortsKey] ?: false }
                .distinctUntilChanged()
                .collect {
                    if (YouTube.cookie != null && accountPlaylists.value != null) {
                        loadAccountPlaylists()
                    }
                }
        }
    }
}


