

package com.voidplayer.music.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidplayer.music.innertube.YouTube
import com.voidplayer.music.innertube.models.PlaylistItem
import com.voidplayer.music.innertube.models.SongItem
import com.voidplayer.music.innertube.models.YTItem
import com.voidplayer.music.innertube.models.filterVideoSongs
import com.voidplayer.music.constants.HideVideoSongsKey
import com.voidplayer.music.db.MusicDatabase
import com.voidplayer.music.utils.dataStore
import com.voidplayer.music.utils.get
import com.voidplayer.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    val database: MusicDatabase
) : ViewModel() {
    private val playlistId = savedStateHandle.get<String>("playlistId")!!

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val playlistSongs = MutableStateFlow<List<SongItem>>(emptyList())
    val relatedItems = MutableStateFlow<List<YTItem>>(emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    val dbPlaylist = database.playlistByBrowseId(playlistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    var continuation: String? = null
        private set

    private var proactiveLoadJob: Job? = null

    init {
        fetchInitialPlaylistData()
    }

    private fun fetchInitialPlaylistData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            continuation = null
            proactiveLoadJob?.cancel() 

            val cleanId = when {
                playlistId.startsWith("VLPL") -> playlistId.removePrefix("VL")
                playlistId.startsWith("VL") && playlistId.length > 2 -> playlistId.removePrefix("VL")
                else -> playlistId
            }

            val localDbPlaylist = database.getPlaylistById(playlistId)
                ?: database.getPlaylistById(cleanId)
                ?: database.playlistByBrowseId(playlistId).firstOrNull()
                ?: database.playlistByBrowseId(cleanId).firstOrNull()

            val targetYouTubeId = localDbPlaylist?.playlist?.browseId?.takeIf { it.isNotBlank() } ?: cleanId

            YouTube.playlist(targetYouTubeId)
                .onSuccess { playlistPage ->
                    playlist.value = playlistPage.playlist
                    playlistSongs.value = applySongFilters(playlistPage.songs)
                    relatedItems.value = playlistPage.related ?: emptyList()
                    continuation = playlistPage.songsContinuation
                    _isLoading.value = false

                    val currentPlaylistItem = playlistPage.playlist
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val existing = database.getPlaylistById(playlistId)
                                ?: database.playlistByBrowseId(playlistId).firstOrNull()
                            if (existing == null) {
                                database.insert(
                                    com.voidplayer.music.db.entities.PlaylistEntity(
                                        id = playlistId,
                                        name = currentPlaylistItem.title,
                                        browseId = targetYouTubeId,
                                        thumbnailUrl = currentPlaylistItem.thumbnail,
                                        isEditable = false,
                                        lastUpdateTime = java.time.LocalDateTime.now()
                                    )
                                )
                            } else {
                                database.update(
                                    existing.playlist.copy(
                                        lastUpdateTime = java.time.LocalDateTime.now(),
                                        thumbnailUrl = currentPlaylistItem.thumbnail ?: existing.playlist.thumbnailUrl
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            reportException(e)
                        }
                    }

                    if (continuation != null) {
                        startProactiveBackgroundLoading()
                    }
                }.onFailure { throwable ->
                    if (localDbPlaylist != null) {
                        val localSongs = database.playlistSongs(localDbPlaylist.playlist.id).firstOrNull() ?: emptyList()
                        val convertedSongs = localSongs.map { ps ->
                            val s = ps.song.song
                            val album = ps.song.album
                            val artists = ps.song.artists
                            SongItem(
                                id = s.id,
                                title = s.title,
                                artists = artists.map { com.voidplayer.music.innertube.models.Artist(id = it.id, name = it.name) },
                                album = album?.let { com.voidplayer.music.innertube.models.Album(id = it.id, name = it.title) },
                                duration = if (s.duration > 0) s.duration else null,
                                thumbnail = s.thumbnailUrl.orEmpty(),
                                explicit = s.explicit
                            )
                        }
                        playlist.value = PlaylistItem(
                            id = localDbPlaylist.playlist.id,
                            title = localDbPlaylist.playlist.name,
                            author = null,
                            songCountText = "${convertedSongs.size} songs",
                            thumbnail = localDbPlaylist.playlist.thumbnailUrl,
                            playEndpoint = null,
                            shuffleEndpoint = null,
                            radioEndpoint = null,
                            isEditable = false
                        )
                        playlistSongs.value = convertedSongs
                        _isLoading.value = false
                    } else {
                        _error.value = throwable.message?.takeIf { it.isNotBlank() }
                            ?: throwable::class.java.simpleName
                            ?: "Failed to load playlist"
                        _isLoading.value = false
                        reportException(throwable)
                    }
                }
        }
    }

    private fun startProactiveBackgroundLoading() {
        proactiveLoadJob?.cancel() 
        proactiveLoadJob = viewModelScope.launch(Dispatchers.IO) {
            var currentProactiveToken = continuation
            while (currentProactiveToken != null && isActive) {
                
                if (_isLoadingMore.value) {
                    
                    
                    break 
                }

                YouTube.playlistContinuation(currentProactiveToken)
                    .onSuccess { playlistContinuationPage ->
                        val currentSongs = playlistSongs.value.toMutableList()
                        currentSongs.addAll(playlistContinuationPage.songs)
                        playlistSongs.value = applySongFilters(currentSongs)
                        currentProactiveToken = playlistContinuationPage.continuation
                        
                        this@OnlinePlaylistViewModel.continuation = currentProactiveToken 
                    }.onFailure { throwable ->
                        reportException(throwable)
                        currentProactiveToken = null 
                    }
            }
            
        }
    }

    fun loadMoreSongs() {
        if (_isLoadingMore.value) return 
        
        val tokenForManualLoad = continuation ?: return 

        proactiveLoadJob?.cancel() 
        _isLoadingMore.value = true

        viewModelScope.launch(Dispatchers.IO) {
            YouTube.playlistContinuation(tokenForManualLoad)
                .onSuccess { playlistContinuationPage ->
                    val currentSongs = playlistSongs.value.toMutableList()
                    currentSongs.addAll(playlistContinuationPage.songs)
                    playlistSongs.value = applySongFilters(currentSongs)
                    continuation = playlistContinuationPage.continuation
                }.onFailure { throwable ->
                    reportException(throwable)
                }.also {
                    _isLoadingMore.value = false
                    
                    if (continuation != null && isActive) {
                        startProactiveBackgroundLoading()
                    }
                }
        }
    }

    fun retry() {
        proactiveLoadJob?.cancel()
        fetchInitialPlaylistData() 
    }

    private fun applySongFilters(songs: List<SongItem>): List<SongItem> {
        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
        val uniqueSongs = songs.distinctBy { it.id }
        if (!hideVideoSongs) return uniqueSongs

        val filtered = uniqueSongs.filterVideoSongs(true)
        // If filtering hides everything, keep original list to avoid false "empty playlist" UX.
        return if (filtered.isEmpty() && uniqueSongs.isNotEmpty()) uniqueSongs else filtered
    }

    override fun onCleared() {
        super.onCleared()
        proactiveLoadJob?.cancel()
    }
}


