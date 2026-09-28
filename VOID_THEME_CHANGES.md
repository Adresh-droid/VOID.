# VOID reskin — what changed

Restyled to match the VOID Player web app's visual identity. No playback,
data, or navigation logic was touched — only colors, fonts, shapes, and a
few default *values* for style settings that already existed in the app.

## New files
- `ui/theme/VoidColors.kt` — VOID's exact palette + a hand-authored dark ColorScheme
- `ui/theme/VoidShape.kt` — 16dp/10dp/20dp/28dp corner radius scale
- `res/font/dm_sans_variable.ttf`, `bricolage_grotesque_variable.ttf`,
  `space_mono_regular.ttf`, `space_mono_bold.ttf` — real Google Fonts files
  (from the google/fonts GitHub mirror), used as variable fonts via `FontVariation`

## Edited
- `ui/theme/Font.kt` — added DmSans / BricolageGrotesque / SpaceMono `FontFamily`s
- `ui/theme/Type.kt` — remapped the Material3 type scale onto those fonts.
  Sizes/line-heights are untouched, only family/weight/letter-spacing changed
- `ui/theme/Theme.kt` — `DefaultThemeColor` is now VOID green (was pink
  `0xFFED5564`); when nothing else overrides it (no system dynamic color,
  no per-track album-art color, no custom picked color), the app now uses
  `VoidDarkColorScheme` instead of an algorithmically-generated one, for exact
  hex fidelity. System dynamic color, the per-track extractor, and the color
  picker all still work exactly as before. Added `shapes = VoidShapes`
- `ui/component/PlayingIndicator.kt` — thinner bars/gaps to match VOID; default
  color now follows the theme accent instead of hardcoded white
- `ui/component/FloatingNavigationToolbar.kt` — this is the default nav bar
  (`useFloatingNavBar` defaults false). Inactive/active/pill colors now use
  VOID's exact values instead of the generic Material tones
- `ui/player/MiniPlayer.kt` — this is the default mini player
  (`useNewMiniPlayerDesign` defaults true). Swapped the circular art + ring
  progress for VOID's rounded-square art (12dp) + a bottom gradient progress bar
- `ui/player/Thumbnail.kt` — now-playing cover art: corner radius default
  changed from 3f to 20f (still a user-adjustable setting, just a different
  shipped default), added VOID's deep shadow + hairline edge highlight
- `ui/player/Player.kt` —
  - Default `PlayerButtonsStyle` changed from `DEFAULT` (white) to `PRIMARY`
    (theme accent), so the play button / active slider / controls render
    green by default instead of white. Mirrored in `AppearanceSettings.kt`
    so the settings screen shows the right state
  - Default `PlayerBackgroundStyle.GRADIENT` reworked: was a saturated
    full-screen wash of the extracted album color; now a contained glow in
    the upper ~45% fading to the neutral surface, matching VOID's
    `.np-overlay` radial glow. Still driven by the same per-track color
    extraction as before
  - Added VOID's accent glow shadow to the main play/pause button
- `ui/screens/settings/AppearanceSettings.kt` — kept the two preference
  defaults above (`PlayerButtonsStyle`, thumbnail corner radius) in sync so
  the settings UI doesn't show a stale/mismatched selection on first launch

## Left alone (already inherits VOID's colors automatically)
Home, Library, Search, Queue, and the settings/menu screens are almost
entirely `MaterialTheme.colorScheme`-driven (~1200 theme references vs. ~55
hardcoded hex values app-wide, and most of those 55 are legitimate fixed
colors — service brand icons, the color-picker's own preset swatches — not
theme leaks). They pick up the new palette and fonts without needing
individual edits.

## Not touched
Alternate style options a user can still switch to in Settings: the
floating-pill nav bar variant, the legacy mini player, and the other
now-playing background styles (Blur, Glow Animated, Apple Music, Live Mesh,
Liquid Glass). Only the *shipped defaults* were changed to match VOID —
every alternate is still there and still works.

## Can't verify
No Android SDK in the sandbox this was built in, so this hasn't been
compiled. Colors/shapes/fonts were checked by hand for type-correctness, but
build it in Android Studio before trusting it fully.
