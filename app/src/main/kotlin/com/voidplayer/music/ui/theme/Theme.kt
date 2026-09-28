package com.voidplayer.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.VectorConverter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

val DefaultThemeColor = VoidColors.Accent

// Mirrors the HTML's _applyDynColor(): a slow ease (same curve as its
// `ease(t) = t<0.5 ? 4t^3 : 1-(-2t+2)^3/2`) over ~20s, so the accent glides
// between track colors instead of cutting.
private val DynAccentEasing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private const val DYN_ACCENT_DURATION_MS = 1200

// The animated seed color feeds MaterialTheme.colorScheme, which is read by
// ~2000 call sites across the app -- every one of them recomposes whenever
// colorScheme gets a new identity. animateColorAsState naturally ticks on
// every display frame (~60/sec), so during the ~1.2s fade that was forcing
// a full-tree recomposition 60+ times a second, which is the lag. The fade
// is still smooth to the eye well below display frame rate, so the color
// driving colorScheme is sampled down to this instead -- ~24 updates/sec,
// same perceptual smoothness as film, ~60% fewer full-app recompositions.
private const val THEME_COLOR_SAMPLE_MS = 42L

@Composable
fun voidPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    // VOID behaves like the web app: it never pulls the system Monet/wallpaper
    // color, and dynamic accent is scoped -- it only ever tints the accent
    // roles (primary/secondary/tertiary + their containers), never re-derives
    // outline, surfaceTint, or the neutral surfaces. In the HTML, --dyn is
    // used on exactly: play button glow, active nav icon, mini progress bar,
    // now-playing backdrop, playing-track highlight, favorite dot -- never on
    // general chrome. rememberDynamicColorScheme was too broad for that: it
    // builds a whole new tonal palette from the seed, which is how a subtle
    // per-track color ends up leaking into borders, secondary text, etc.
    //
    // The raw glide itself still runs at full frame rate internally (an
    // Animatable ticks every frame like animateColorAsState did) so the
    // motion is exactly as smooth as before -- only the *published* value
    // that flows into colorScheme (and therefore triggers the app-wide
    // recomposition) is sampled down, via snapshotFlow+sample below.
    val rawAnimatedColor = remember { Animatable(themeColor, Color.VectorConverter(themeColor.colorSpace)) }
    val animatedThemeColor by produceState(initialValue = rawAnimatedColor.value, themeColor) {
        // Sampled publish -- see THEME_COLOR_SAMPLE_MS comment above.
        val sampleJob = launch {
            snapshotFlow { rawAnimatedColor.value }
                .conflate()
                .sample(THEME_COLOR_SAMPLE_MS)
                .collect { value = it }
        }
        rawAnimatedColor.animateTo(
            targetValue = themeColor,
            animationSpec = androidx.compose.animation.core.tween(DYN_ACCENT_DURATION_MS, easing = DynAccentEasing),
        )
        // sample() only emits on its own timer, so the last tick before the
        // glide finished can land a hair short of the real target. Cancel
        // the sampler and publish the exact final color once settled.
        sampleJob.cancel()
        value = themeColor
    }

    val baseColorScheme = if (darkTheme) VoidDarkColorScheme else VoidLightColorScheme

    val colorScheme = remember(baseColorScheme, animatedThemeColor, pureBlack, darkTheme) {
        val onAccent = if (animatedThemeColor.luminance() > 0.5f) Color.Black else Color.White
        val accentContainer = animatedThemeColor.copy(alpha = 1f).let { c ->
            Color(
                red = if (darkTheme) c.red * 0.28f else c.red * 0.85f,
                green = if (darkTheme) c.green * 0.28f else c.green * 0.85f,
                blue = if (darkTheme) c.blue * 0.28f else c.blue * 0.85f,
            )
        }

        val themeBase = if (darkTheme) {
            baseColorScheme.copy(
                primary = animatedThemeColor,
                secondary = animatedThemeColor,
                tertiary = animatedThemeColor,
                onPrimary = onAccent,
                onSecondary = onAccent,
                onTertiary = onAccent,
                primaryContainer = accentContainer,
                secondaryContainer = accentContainer,
                tertiaryContainer = accentContainer,
                background = VoidColors.Bg,
                surface = VoidColors.Bg2,
                surfaceContainer = VoidColors.Bg3,
                surfaceContainerLow = VoidColors.Bg2,
                surfaceContainerLowest = VoidColors.Bg,
                surfaceContainerHigh = VoidColors.Bg4,
                surfaceContainerHighest = VoidColors.Bg5,
            )
        } else {
            baseColorScheme.copy(
                primary = animatedThemeColor,
                secondary = animatedThemeColor,
                tertiary = animatedThemeColor,
                onPrimary = onAccent,
                onSecondary = onAccent,
                onTertiary = onAccent,
                primaryContainer = accentContainer,
                secondaryContainer = accentContainer,
                tertiaryContainer = accentContainer,
                background = VoidLightColors.Bg,
                surface = VoidLightColors.Bg2,
                surfaceContainer = VoidLightColors.Bg3,
                surfaceContainerLow = VoidLightColors.Bg2,
                surfaceContainerLowest = VoidLightColors.Bg2,
                surfaceContainerHigh = VoidLightColors.Bg4,
                surfaceContainerHighest = VoidLightColors.Bg5,
            )
        }

        if (darkTheme && pureBlack) {
            themeBase.pureBlack(true)
        } else {
            themeBase
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = VoidShapes,
        content = content
    )
}

// Same vivid fallback palette as the web app's resetDynColor() -- used when a
// cover has no usable hue (grey/B&W art) so the accent still pops instead of
// collapsing to near-black or a muddy grey.
private val VividFallbackPalette = listOf(
    Color(0xFFFF5078), // hot pink
    Color(0xFFFF8C28), // amber
    Color(0xFF50B4FF), // electric blue
    Color(0xFFB450FF), // violet
    Color(0xFF00D2AA), // teal
    Color(0xFFFFD228), // golden yellow
    Color(0xFF1DB954), // VOID green
)

fun Bitmap.extractThemeColor(): Color {
    val palette = Palette.from(this).maximumColorCount(24).generate()
    val colorsToPopulation = palette.swatches.associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    val pickedArgb = rankedColors.firstOrNull() ?: return VividFallbackPalette.last()

    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(pickedArgb, hsv)

    // Boost saturation aggressively (matches the HTML's boost = 2.6 on the
    // raw RGB channels) so even muted/tinted covers produce a punchy accent.
    hsv[1] = (hsv[1] * 1.8f).coerceAtMost(1f)

    // Ensure minimum brightness so the accent stays readable on a black UI.
    if (hsv[2] < 0.45f) hsv[2] = 0.45f
    hsv[2] = hsv[2].coerceAtMost(0.9f)

    // Never-black/never-grey guard: if after boosting there's still no real
    // saturation or brightness, snap to the vivid fallback palette instead,
    // seeded by the picked color so the same cover always lands on the same
    // fallback (mirrors resetDynColor()'s deterministic-by-hue behavior).
    if (hsv[1] < 0.18f || hsv[2] < 0.25f) {
        val idx = kotlin.math.abs(pickedArgb) % VividFallbackPalette.size
        return VividFallbackPalette[idx]
    }

    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun Bitmap.extractGradientColors(): List<Color> {
    val extractedColors = Palette.from(this)
        .maximumColorCount(64)
        .generate()
        .swatches
        .associate { it.rgb to it.population }

    val rankedColors = Score.score(extractedColors, 2, 0xff4285f4.toInt(), true)
    
    val colors = if (rankedColors.size >= 2) {
        listOf(Color(rankedColors[0]), Color(rankedColors[1]))
    } else if (rankedColors.size == 1) {
        listOf(Color(rankedColors[0]), Color(rankedColors[0]).copy(alpha = 0.7f))
    } else {
        listOf(Color(0xFF595959), Color(0xFF0D0D0D))
    }
    
    return colors.sortedByDescending { it.luminance() }
}

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()}


