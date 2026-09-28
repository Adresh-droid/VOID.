/**
 * vivimusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.voidplayer.music.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Stable
data class GlassEffectConfig(
    val globalEnabled: Boolean = false,
    val vibrancy: Float = 1f,
    val blurRadius: Float = 8f,
    val lensHeight: Float = 0.5f,
    val lensAmount: Float = 0.5f,
    val chromaticAberration: Boolean = true,
    val depthEffect: Boolean = true,
    val surfaceTintColor: Color = Color.Unspecified,
    val surfaceOpacity: Float = 0.4f,
    val textColor: Color = Color.Unspecified,
    val playerEnabled: Boolean = true,
    val miniPlayerEnabled: Boolean = true,
    val navBarEnabled: Boolean = true,
) {
    fun isEnabledFor(component: GlassComponent): Boolean = false
}

enum class GlassComponent {
    PLAYER,
    MINI_PLAYER,
    NAV_BAR,
}

val LocalGlassEffectConfig = staticCompositionLocalOf { GlassEffectConfig() }

fun isGlassSupported(): Boolean = false

@Composable
fun Modifier.liquidGlass(
    config: GlassEffectConfig,
    shape: CornerBasedShape = RoundedCornerShape(0.dp),
    applyEdgeEffects: Boolean = true,
    blurRadiusDp: Float = config.blurRadius,
): Modifier = this
