package com.voidplayer.music.innertube.pages

import com.voidplayer.music.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)

