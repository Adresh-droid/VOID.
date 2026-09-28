# Package Rename Implementation Plan: `iad1tya.echo.music` → `void.player`

This plan covers the project-wide rename of the root package name to `void.player`. This involves updating Gradle configurations, Android Manifests, and refactoring all source code packages and imports.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/AdreshGamer94/Downloads/Void-Music-VOID-theme/Void-Music-main/app/build.gradle.kts)
- Update `namespace` from `"iad1tya.echo.music"` to `"void.player"`.
- Update `applicationId` from `"iad1tya.echo.music"` to `"void.player"`.

#### [MODIFY] [artistvideo/build.gradle.kts](file:///C:/Users/AdreshGamer94/Downloads/Void-Music-VOID-theme/Void-Music-main/artistvideo/build.gradle.kts)
- Update `namespace` from `"iad1tya.echo.music.artistvideo"` to `"void.player.artistvideo"`.

### Android Manifest

#### [MODIFY] [app/src/main/AndroidManifest.xml](file:///C:/Users/AdreshGamer94/Downloads/Void-Music-VOID-theme/Void-Music-main/app/src/main/AndroidManifest.xml)
- Update all component references starting with `iad1tya.echo.music`.
- Update provider authorities (e.g., `FileProvider`).
- Update custom intent actions and schemes if they contain the old package name.

### Source Code Refactoring

#### [MODIFY] All `.kt` and `.java` files
- Update `package iad1tya.echo.music...` declarations.
- Update `import iad1tya.echo.music...` statements.
- Update any hardcoded package strings in the code.

#### [MODIFY] All `.xml` resource files (Layouts, Drawables, etc.)
- Update custom view tags or attribute references that use the full package name.

### Directory Migration

#### [MOVE] Source Directories
- Move `app/src/main/kotlin/iad1tya/echo/music/*` to `app/src/main/kotlin/void/player/`.
- Move `artistvideo/src/main/kotlin/iad1tya/echo/music/artistvideo/*` to `artistvideo/src/main/kotlin/void/player/artistvideo/`.

## Verification Plan

### Automated Verification
- Run `./gradlew clean` to clear old build artifacts.
- Run `./gradlew :app:assembleDebug` to verify that the project compiles with the new package structure.

### Manual Verification
- Deploy the app to a device and ensure it launches.
- Check if features like sharing (FileProvider) or deep links still work.

