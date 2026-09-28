

package com.voidplayer.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * VOID corner-radius scale, ported from `--radius` (16px) / `--radius-sm`
 * (10px) plus the larger radii used on hero surfaces like the now-playing
 * cover art (20px) and sheet/dialog corners (28px).
 */
object VoidRadius {
    val ExtraSmall = 8.dp
    val Small = 10.dp   // --radius-sm
    val Medium = 16.dp  // --radius
    val Large = 20.dp   // now-playing cover, hero cards
    val ExtraLarge = 28.dp // sheets / large dialogs
    val Pill = 999.dp   // fully-rounded chips / badges
}

val VoidShapes = Shapes(
    extraSmall = RoundedCornerShape(VoidRadius.ExtraSmall),
    small = RoundedCornerShape(VoidRadius.Small),
    medium = RoundedCornerShape(VoidRadius.Medium),
    large = RoundedCornerShape(VoidRadius.Large),
    extraLarge = RoundedCornerShape(VoidRadius.ExtraLarge),
)

