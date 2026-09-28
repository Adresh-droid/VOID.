# Fix Streaming and Library Browsing Issues

The streaming system is currently failing due to regex extraction errors for YouTube player JS functions, missing PoToken assets, and strict JSON parsing of thumbnails. This plan addresses these points to restore playback and prevent UI crashes.

## User Review Required

> [!IMPORTANT]
> The `po_token.html` asset is missing from the project. While I will add a workaround to prevent redundant failures, the absence of this file will degrade playback reliability for clients that strictly require it (like `WEB_REMIX`). You should ensure this file is added to `app/src/main/assets/` if possible.

## Proposed Changes

### InnerTube Module

#### [MODIFY] [ThumbnailRenderer.kt](file:///C:/Users/AdreshGamer94/Downloads/Void-BETA%200.1/Echo-Music-main/innertube/src/main/kotlin/com/voidplayer/music/innertube/models/ThumbnailRenderer.kt)
- Make the `thumbnail` field in `MusicThumbnailRenderer` and `MusicAnimatedThumbnailRenderer` optional.
- This prevents `JsonConvertException` when YouTube returns an item without a thumbnail.

---

### App Module

#### [MODIFY] [FunctionNameExtractor.kt](file:///C:/Users/AdreshGamer94/Downloads/Void-BETA%200.1/Echo-Music-main/app/src/main/kotlin/com/voidplayer/music/utils/cipher/FunctionNameExtractor.kt)
- Update `SIG_FUNCTION_PATTERNS` and `N_FUNCTION_PATTERNS` with more robust regexes to match current YouTube player JS variations.
- Improve error logging to identify which pattern is failing.

#### [MODIFY] [YTPlayerUtils.kt](file:///C:/Users/AdreshGamer94/Downloads/Void-BETA%200.1/Echo-Music-main/app/src/main/kotlin/com/voidplayer/music/utils/YTPlayerUtils.kt)
- Update `tryGetWebClientPoToken` to cache the "broken" state if `FileNotFoundException` occurs, avoiding repeated overhead.
- Refine the fallback loop to handle `403` and `400` errors more gracefully.
- Add `ANDROID_NO_SDK` to the fallback chain as it often works without PoToken.

## Verification Plan

### Automated Tests
- I will attempt to run a build to ensure syntax correctness.

### Manual Verification
1.  Launch the app.
2.  Attempt to play a song that previously failed.
3.  Check the "Library" or "Liked Playlists" section to ensure it no longer crashes due to missing thumbnails.
4.  Monitor Logcat for "N-transform applied successfully" and "Stream validated successfully".
