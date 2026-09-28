package com.voidplayer.music.ui.player

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.voidplayer.music.LocalDatabase
import com.voidplayer.music.LocalPlayerConnection
import com.voidplayer.music.R
import com.voidplayer.music.constants.CropAlbumArtKey
import com.voidplayer.music.constants.DarkModeKey
import com.voidplayer.music.constants.MiniPlayerBackgroundStyleKey
import com.voidplayer.music.constants.MiniPlayerHeight
import com.voidplayer.music.constants.PlayerBackgroundStyle
import com.voidplayer.music.constants.PureBlackMiniPlayerKey
import com.voidplayer.music.constants.SwipeSensitivityKey
import com.voidplayer.music.constants.SwipeThumbnailKey
import com.voidplayer.music.constants.ThumbnailCornerRadius
import com.voidplayer.music.constants.UseNewMiniPlayerDesignKey
import com.voidplayer.music.db.entities.ArtistEntity
import com.voidplayer.music.models.MediaMetadata
import com.voidplayer.music.playback.PlayerConnection
import com.voidplayer.music.ui.screens.settings.DarkMode
import com.voidplayer.music.ui.component.GlassComponent
import com.voidplayer.music.ui.component.LocalGlassEffectConfig
import com.voidplayer.music.ui.component.isGlassSupported
import com.voidplayer.music.ui.component.liquidGlass
import com.voidplayer.music.ui.theme.PlayerColorExtractor
import com.voidplayer.music.ui.theme.VoidColors
import com.voidplayer.music.utils.rememberEnumPreference
import com.voidplayer.music.utils.rememberPreference
import com.voidplayer.music.VoidAudioDeviceBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import com.voidplayer.music.isBluetoothHeadphoneConnected
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import com.voidplayer.music.ui.component.Icon as MIcon

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density

import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.cos
import kotlin.math.sin

private data class PolygonCookieShape(
    val sides: Int,
    val indent: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val maxRadius = minOf(size.width, size.height) / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f

        val steps = 120
        for (i in 0..steps) {
            val angle = i * Math.PI * 2 / steps
            val r = maxRadius * (1f - indent + indent * cos(sides * angle))
            val x = cx + r * cos(angle)
            val y = cy + r * sin(angle)
            if (i == 0) path.moveTo(x.toFloat(), y.toFloat())
            else path.lineTo(x.toFloat(), y.toFloat())
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Stable
class ProgressState(
    private val positionState: MutableLongState,
    private val durationState: MutableLongState
) {
    val progress: Float
        get() {
            val duration = durationState.longValue
            return if (duration > 0) (positionState.longValue.toFloat() / duration).coerceIn(0f, 1f) else 0f
        }
}

@Composable
fun MiniPlayer(
    positionState: MutableLongState,
    durationState: MutableLongState,
    modifier: Modifier = Modifier
) {
    val useNewMiniPlayerDesign = true


    val progressState = remember { ProgressState(positionState, durationState) }

    if (useNewMiniPlayerDesign) {
        NewMiniPlayer(
            progressState = progressState,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            LegacyMiniPlayer(
                progressState = progressState,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}





@Composable
private fun NewMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier
) {
    val playerConnection = LocalPlayerConnection.current ?: return


    val pureBlack = false
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val miniPlayerBackground = PlayerBackgroundStyle.DEFAULT


    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()


    val context = LocalContext.current
    val isBluetoothConnected = isBluetoothHeadphoneConnected(context)
    var showVoidAudioDeviceBottomSheet by remember { mutableStateOf(false) }


    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)


    // Listen Together feature removed; playback controls are never guest-restricted.
    val isListenTogetherGuest = false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest

    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isTabletLandscape = remember(configuration.screenWidthDp, configuration.orientation) {
        configuration.screenWidthDp >= 600 && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }


    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    }

    val autoSwipeThreshold = remember(swipeSensitivity) {
        (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }

    val (gradientColors, onGradientColorsChange) = remember { mutableStateOf<List<Color>>(emptyList()) }

    MiniPlayerColorExtractor(
        mediaMetadata = mediaMetadata,
        miniPlayerBackground = miniPlayerBackground,
        onGradientColorsChange = onGradientColorsChange
    )


    val isDynamicBackground = false

    val backgroundColor = if (pureBlack && useDarkTheme) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(0f, animationSpec)
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val adjustedDragAmount =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                                val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                                val tryingToSwipeRight = adjustedDragAmount > 0
                                val tryingToSwipeLeft = adjustedDragAmount < 0
                                val allowLeft = tryingToSwipeLeft && canSkipNext
                                val allowRight = tryingToSwipeRight && canSkipPrevious

                                val canReturnToCenter =
                                    (tryingToSwipeRight && !canSkipPrevious && offsetXAnimatable.value < 0) ||
                                            (tryingToSwipeLeft && !canSkipNext && offsetXAnimatable.value > 0)

                                if (allowLeft || allowRight || canReturnToCenter) {
                                    totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                    coroutineScope.launch {
                                        offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                    }
                                }
                            },
                            onDragEnd = {
                                val dragDuration = System.currentTimeMillis() - dragStartTime
                                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                val currentOffset = offsetXAnimatable.value
                                val minDistanceThreshold = 50f
                                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                val shouldChangeSong = (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                                        (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    if (currentOffset > 0 && canSkipPrevious) {
                                        playerConnection.player.seekToPreviousMediaItem()
                                    } else if (currentOffset <= 0 && canSkipNext) {
                                        playerConnection.player.seekToNext()
                                    }
                                }
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(0f, animationSpec)
                                }
                            }
                        )
                    }
                } else baseModifier
            }
    ) {
        Box(
            modifier = Modifier
                .then(if (isTabletLandscape) Modifier.width(480.dp).align(Alignment.Center) else Modifier.fillMaxWidth())
                .height(MiniPlayerHeight)
                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                .background(color = backgroundColor)
        ) {

            MiniPlayerBackgroundLayer(
                style = miniPlayerBackground,
                mediaMetadata = mediaMetadata,
                gradientColors = gradientColors
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(fraction = progressState.progress.coerceIn(0f, 1f))
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
            ) {

                NewMiniPlayerThumbnail(
                    progressState = progressState,
                    mediaMetadata = mediaMetadata,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor,
                )

                Spacer(modifier = Modifier.width(16.dp))


                NewMiniPlayerSongInfo(
                    mediaMetadata = mediaMetadata,
                    onSurfaceColor = onSurfaceColor,
                    errorColor = errorColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))


                MiniPlayerControls(
                    playerConnection = playerConnection,
                    playbackState = playbackState,
                    canSkipPrevious = canSkipPrevious,
                    canSkipNext = canSkipNext,
                    onSurfaceColor = onSurfaceColor,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor
                )
            }
        }
    }

    if (showVoidAudioDeviceBottomSheet) {
        VoidAudioDeviceBottomSheet(onDismiss = { showVoidAudioDeviceBottomSheet = false })
    }
}


@Composable
private fun NewMiniPlayerThumbnail(
    progressState: ProgressState,
    mediaMetadata: MediaMetadata?,
    primaryColor: Color,
    outlineColor: Color,
) {
    val playerConnection = LocalPlayerConnection.current
    val isPlaying by playerConnection?.isPlaying?.collectAsState() ?: remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = rotation.value + 3600000f,
                animationSpec = tween(
                    durationMillis = (3600000f / 360f * 12000f).toInt(),
                    easing = LinearEasing
                )
            )
        } else {
            rotation.stop()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                rotationZ = rotation.value
            }
            .clip(CircleShape)
            .background(VoidColors.Bg4)
            .border(3.dp, primaryColor.copy(alpha = 0.8f), CircleShape)
    ) {
        mediaMetadata?.let { metadata ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(metadata.thumbnailUrl)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
    }
}


@Composable
private fun NewMiniPlayerSongInfo(
    mediaMetadata: MediaMetadata?,
    onSurfaceColor: Color,
    errorColor: Color,
    modifier: Modifier = Modifier
) {
    val error by LocalPlayerConnection.current?.error?.collectAsState() ?: remember { mutableStateOf(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        mediaMetadata?.let { metadata ->
            Text(
                text = metadata.title,
                color = onSurfaceColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (metadata.explicit) MIcon.Explicit()
                if (metadata.artists.any { it.name.isNotBlank() }) {
                    Text(
                        text = metadata.artists.joinToString { it.name },
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                    )
                }
            }

            AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = stringResource(R.string.error_playing),
                    color = errorColor,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}





@Composable
private fun LegacyMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val pureBlack by rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)

    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()

    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)


    // Listen Together feature removed; playback controls are never guest-restricted.
    val isListenTogetherGuest = false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest

    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isTabletLandscape = remember(configuration.screenWidthDp, configuration.orientation) {
        configuration.screenWidthDp >= 600 && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    }

    val autoSwipeThreshold = remember(swipeSensitivity) {
        (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .background(
                if (pureBlack && isSystemInDarkTheme()) Color.Black
                else MaterialTheme.colorScheme.surfaceContainer
            )
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val adjustedDragAmount =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                                val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                                val tryingToSwipeRight = adjustedDragAmount > 0
                                val tryingToSwipeLeft = adjustedDragAmount < 0
                                val allowLeft = tryingToSwipeLeft && canSkipNext
                                val allowRight = tryingToSwipeRight && canSkipPrevious

                                val canReturnToCenter =
                                    (tryingToSwipeRight && !canSkipPrevious && offsetXAnimatable.value < 0) ||
                                            (tryingToSwipeLeft && !canSkipNext && offsetXAnimatable.value > 0)

                                if (allowLeft || allowRight || canReturnToCenter) {
                                    totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                    coroutineScope.launch {
                                        offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                    }
                                }
                            },
                            onDragEnd = {
                                val dragDuration = System.currentTimeMillis() - dragStartTime
                                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                val currentOffset = offsetXAnimatable.value
                                val minDistanceThreshold = 50f
                                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                val shouldChangeSong = (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                                        (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    if (currentOffset > 0 && canSkipPrevious) {
                                        playerConnection.player.seekToPreviousMediaItem()
                                    } else if (currentOffset <= 0 && canSkipNext) {
                                        playerConnection.player.seekToNext()
                                    }
                                }
                                coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                            }
                        )
                    }
                } else baseModifier
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .drawWithContent {
                    val progress = progressState.progress
                    drawRect(trackColor)
                    drawRect(primaryColor, size = Size(size.width * progress, size.height))
                }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                .padding(end = 12.dp),
        ) {
            Box(Modifier.weight(1f)) {
                mediaMetadata?.let {
                    LegacyMiniMediaInfo(
                        mediaMetadata = it,
                        pureBlack = pureBlack,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }

            LegacyPlayPauseButton(
                playbackState = playbackState,
                playerConnection = playerConnection
            )

            IconButton(
                enabled = canSkipNext,
                onClick = { playerConnection.seekToNext() },
            ) {
                Icon(painter = painterResource(R.drawable.skip_next), contentDescription = null)
            }
        }


        if (offsetXAnimatable.value.absoluteValue > 50f) {
            Box(
                modifier = Modifier
                    .align(if (offsetXAnimatable.value > 0) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (offsetXAnimatable.value > 0) R.drawable.skip_previous else R.drawable.skip_next
                    ),
                    contentDescription = null,
                    tint = primaryColor.copy(
                        alpha = (offsetXAnimatable.value.absoluteValue / autoSwipeThreshold).coerceIn(0f, 1f)
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LegacyPlayPauseButton(
    playbackState: Int,
    playerConnection: PlayerConnection,
    tint: Color = androidx.compose.material3.LocalContentColor.current
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val effectiveIsPlaying = isPlaying

    IconButton(
        onClick = {
            if (playbackState == Player.STATE_ENDED) {
                playerConnection.player.seekTo(0, 0)
                playerConnection.player.playWhenReady = true
            } else {
                playerConnection.togglePlayPause()
            }
        },
    ) {
        Icon(
            painter = painterResource(
                when {
                    playbackState == Player.STATE_ENDED -> R.drawable.replay
                    effectiveIsPlaying -> R.drawable.pause
                    else -> R.drawable.play
                }
            ),
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
private fun LegacyMiniMediaInfo(
    mediaMetadata: MediaMetadata,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
) {
    val error by LocalPlayerConnection.current?.error?.collectAsState() ?: remember { mutableStateOf(null) }
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mediaMetadata.thumbnailUrl)
                    .build(),
                contentDescription = null,
                contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(ThumbnailCornerRadius)),
            )

            androidx.compose.animation.AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = if (pureBlack) Color.Black else Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius),
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
        ) {
            Text(
                text = mediaMetadata.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(),
            )

            if (mediaMetadata.artists.any { it.name.isNotBlank() }) {
                Text(
                    text = mediaMetadata.artists.joinToString { it.name },
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}






@Composable
private fun FavoriteButton(
    songId: String,
    onSurfaceColor: Color,
    errorColor: Color,
    outlineColor: Color
) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val librarySong by database.song(songId).collectAsState(initial = null)
    val isLiked = librarySong?.song?.liked == true

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = if (isLiked) errorColor.copy(alpha = 0.5f) else outlineColor.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .background(
                color = if (isLiked) errorColor.copy(alpha = 0.1f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable { playerConnection.service.toggleLike() }
    ) {
        Icon(
            painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
            contentDescription = null,
            tint = if (isLiked) errorColor else onSurfaceColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}
@Composable
private fun MiniPlayerColorExtractor(
    mediaMetadata: MediaMetadata?,
    miniPlayerBackground: PlayerBackgroundStyle,
    onGradientColorsChange: (List<Color>) -> Unit
) {
    val context = LocalContext.current
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()

    LaunchedEffect(mediaMetadata?.id, miniPlayerBackground) {
        if (miniPlayerBackground == PlayerBackgroundStyle.GRADIENT || miniPlayerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
            val currentMetadata = mediaMetadata
            if (currentMetadata?.thumbnailUrl != null) {
                withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(currentMetadata.thumbnailUrl)
                        .size(100, 100)
                        .allowHardware(false)
                        .build()

                    val result = runCatching { context.imageLoader.execute(request) }.getOrNull()
                    if (result != null) {
                        val bitmap = result.image?.toBitmap()
                        if (bitmap != null) {
                            val palette = withContext(Dispatchers.Default) {
                                Palette.from(bitmap)
                                    .maximumColorCount(8)
                                    .resizeBitmapArea(100 * 100)
                                    .generate()
                            }
                            val extractedColors = if (miniPlayerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
                                listOfNotNull(
                                    palette.getVibrantColor(fallbackColor).let { Color(it) },
                                    palette.getLightVibrantColor(fallbackColor).let { Color(it) },
                                    palette.getDarkVibrantColor(fallbackColor).let { Color(it) },
                                    palette.getMutedColor(fallbackColor).let { Color(it) },
                                    palette.getLightMutedColor(fallbackColor).let { Color(it) },
                                    palette.getDarkMutedColor(fallbackColor).let { Color(it) }
                                ).distinct()
                            } else {
                                PlayerColorExtractor.extractGradientColors(
                                    palette = palette,
                                    fallbackColor = fallbackColor
                                )
                            }
                            withContext(Dispatchers.Main) { onGradientColorsChange(extractedColors) }
                        }
                    }
                }
            }
        } else {
            onGradientColorsChange(emptyList())
        }
    }
}

@Composable
private fun MiniPlayerBackgroundLayer(
    style: PlayerBackgroundStyle,
    mediaMetadata: MediaMetadata?,
    gradientColors: List<Color>
) {
}

@Composable
private fun MiniPlayerControls(
    playerConnection: PlayerConnection,
    playbackState: Int,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    onSurfaceColor: Color,
    primaryColor: Color,
    onPrimaryColor: Color
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val effectiveIsPlaying = isPlaying

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            enabled = canSkipPrevious,
            onClick = { playerConnection.player.seekToPreviousMediaItem() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(painter = painterResource(R.drawable.skip_previous), contentDescription = null, tint = onSurfaceColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable {
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.togglePlayPause()
                    }
                }
        ) {
            Icon(
                painter = painterResource(
                    when {
                        playbackState == Player.STATE_ENDED -> R.drawable.replay
                        effectiveIsPlaying -> R.drawable.pause
                        else -> R.drawable.play
                    }
                ),
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            enabled = canSkipNext,
            onClick = { playerConnection.player.seekToNext() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(painter = painterResource(R.drawable.skip_next), contentDescription = null, tint = onSurfaceColor, modifier = Modifier.size(20.dp))
        }
    }
}




