package com.voidplayer.music.innertube.models.body

import com.voidplayer.music.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)

