package com.voidplayer.music.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.voidplayer.music.R

val bbhBartle = FontFamily(
    Font(R.font.bbh_bartle_regular, FontWeight.Normal)
)

// ── VOID identity type system ──────────────────────────────────────
// DM Sans        -> body copy, buttons, nav labels, general UI text
// Bricolage Grotesque -> display/headline text (hero titles, now-playing title)
// Space Mono     -> uppercase meta labels, durations, timestamps, badges
//
// DM Sans and Bricolage Grotesque ship from Google Fonts as variable
// fonts (weight axis), so every weight below points at the same font
// file with an explicit FontVariation telling Android which static
// instance to render.

val DmSans = FontFamily(
    Font(
        R.font.dm_sans_variable,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.dm_sans_variable,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.dm_sans_variable,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.dm_sans_variable,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

val BricolageGrotesque = FontFamily(
    Font(
        R.font.bricolage_grotesque_variable,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.bricolage_grotesque_variable,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.bricolage_grotesque_variable,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
    Font(
        R.font.bricolage_grotesque_variable,
        FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    ),
)

val SpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

