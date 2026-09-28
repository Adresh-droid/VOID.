

package com.voidplayer.music.models

import com.voidplayer.music.innertube.models.YTItem
import com.voidplayer.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)

