package com.voidplayer.music.innertube.pages

import com.voidplayer.music.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)

