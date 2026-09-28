package com.voidplayer.music.innertube.pages

import com.voidplayer.music.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)

