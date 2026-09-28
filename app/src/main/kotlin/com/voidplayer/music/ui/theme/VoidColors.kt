

package com.voidplayer.music.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * VOID identity palette -- ported 1:1 from the VOID Player web app's
 * `:root` CSS custom properties. These are the raw tokens; [VoidDarkColorScheme]
 * maps them onto Material3's ColorScheme roles.
 *
 * Layered near-black surfaces (Bg -> Bg5, each a touch lighter) with a single
 * saturated accent green. The per-track "dynamic accent" extracted from
 * album art (see [PlayerColorExtractor] / [Bitmap.extractThemeColor]) still
 * drives individual glow/highlight elements exactly as before -- this palette
 * only fixes what the *neutral* chrome (backgrounds, borders, body text)
 * looks like when nothing else overrides it.
 */
object VoidColors {
    val Bg = Color(0xFF050508)
    val Bg2 = Color(0xFF0C0C10)
    val Bg3 = Color(0xFF13131A)
    val Bg4 = Color(0xFF1C1C26)
    val Bg5 = Color(0xFF252532)

    val Border = Color(0xFF1F1F2E)
    val Border2 = Color(0xFF2D2D40)

    val Accent = Color(0xFFC528D5)
    val Accent2 = Color(0xFFA939B9)

    val Text = Color(0xFFF0F0F5)
    val Text2 = Color(0xFF8888A0)
    val Text3 = Color(0xFF3A3A52)

    val Danger = Color(0xFFE74C3C)
    val YouTubeRed = Color(0xFFFF4444)
    val SoundCloudOrange = Color(0xFFFF5500)

    val AccentContainer = Color(0xFF350749)
    val OnAccentContainer = Color(0xFFF3C6FF)
}

object VoidLightColors {
    val Bg = Color(0xFFF8F9FA)
    val Bg2 = Color(0xFFFFFFFF)
    val Bg3 = Color(0xFFF1F3F5)
    val Bg4 = Color(0xFFE9ECEF)
    val Bg5 = Color(0xFFDEE2E6)

    val Border = Color(0xFFE2E8F0)
    val Border2 = Color(0xFFCBD5E1)

    val Accent = Color(0xFFC528D5)
    val Accent2 = Color(0xFFA939B9)

    val Text = Color(0xFF1A1D20)
    val Text2 = Color(0xFF6C757D)
    val Text3 = Color(0xFFA0AEC0)

    val AccentContainer = Color(0xFFF3C6FF)
    val OnAccentContainer = Color(0xFF350749)
}

val VoidLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = VoidLightColors.Accent,
    onPrimary = Color.White,
    primaryContainer = VoidLightColors.AccentContainer,
    onPrimaryContainer = VoidLightColors.OnAccentContainer,
    inversePrimary = VoidLightColors.Accent,

    secondary = VoidLightColors.Accent2,
    onSecondary = Color.White,
    secondaryContainer = VoidLightColors.AccentContainer,
    onSecondaryContainer = VoidLightColors.OnAccentContainer,

    tertiary = VoidLightColors.Accent2,
    onTertiary = Color.White,
    tertiaryContainer = VoidLightColors.AccentContainer,
    onTertiaryContainer = VoidLightColors.OnAccentContainer,

    background = VoidLightColors.Bg,
    onBackground = VoidLightColors.Text,

    surface = VoidLightColors.Bg2,
    onSurface = VoidLightColors.Text,
    surfaceVariant = VoidLightColors.Bg3,
    onSurfaceVariant = VoidLightColors.Text2,
    surfaceContainer = VoidLightColors.Bg3,
    surfaceContainerLow = VoidLightColors.Bg2,
    surfaceContainerLowest = VoidLightColors.Bg2,
    surfaceContainerHigh = VoidLightColors.Bg4,
    surfaceContainerHighest = VoidLightColors.Bg5,
    outline = VoidLightColors.Border,
    outlineVariant = VoidLightColors.Border2
)

val VoidDarkColorScheme = darkColorScheme(
    primary = VoidColors.Accent,
    onPrimary = Color.Black,
    primaryContainer = VoidColors.AccentContainer,
    onPrimaryContainer = VoidColors.OnAccentContainer,
    inversePrimary = VoidColors.Accent,

    secondary = VoidColors.Accent2,
    onSecondary = Color.Black,
    secondaryContainer = VoidColors.AccentContainer,
    onSecondaryContainer = VoidColors.OnAccentContainer,

    tertiary = VoidColors.Accent2,
    onTertiary = Color.Black,
    tertiaryContainer = VoidColors.AccentContainer,
    onTertiaryContainer = VoidColors.OnAccentContainer,

    background = VoidColors.Bg,
    onBackground = VoidColors.Text,

    surface = VoidColors.Bg2,
    onSurface = VoidColors.Text,
    surfaceVariant = VoidColors.Bg3,
    onSurfaceVariant = VoidColors.Text2,
    surfaceTint = VoidColors.Accent,

    surfaceContainerLowest = VoidColors.Bg,
    surfaceContainerLow = VoidColors.Bg2,
    surfaceContainer = VoidColors.Bg3,
    surfaceContainerHigh = VoidColors.Bg4,
    surfaceContainerHighest = VoidColors.Bg5,

    inverseSurface = VoidColors.Text,
    inverseOnSurface = VoidColors.Bg,

    error = VoidColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFF3A1410),
    onErrorContainer = VoidColors.Danger,

    outline = VoidColors.Border2,
    outlineVariant = VoidColors.Border,
    scrim = Color.Black,
)

