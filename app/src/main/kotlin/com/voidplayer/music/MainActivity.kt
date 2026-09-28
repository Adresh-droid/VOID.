package com.voidplayer.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.valentinilk.shimmer.LocalShimmerTheme
import com.voidplayer.music.BuildConfig
import com.voidplayer.music.db.MusicDatabase
import com.voidplayer.music.db.entities.SearchHistory
import com.voidplayer.music.extensions.toEnum
import com.voidplayer.music.innertube.YouTube
import com.voidplayer.music.innertube.models.SongItem
import com.voidplayer.music.innertube.models.WatchEndpoint
import com.voidplayer.music.innertube.utils.parseCookieString
import com.voidplayer.music.models.toMediaMetadata
import com.voidplayer.music.playback.DownloadUtil
import com.voidplayer.music.playback.MusicService
import com.voidplayer.music.playback.MusicService.MusicBinder
import com.voidplayer.music.playback.PlayerConnection
import com.voidplayer.music.playback.queues.YouTubeQueue
import com.voidplayer.music.ui.component.*
import com.voidplayer.music.ui.component.floatingtabbar.rememberFloatingTabBarScrollConnection
import com.voidplayer.music.ui.component.shimmer.getShimmerTheme
import com.voidplayer.music.ui.menu.YouTubeSongMenu
import com.voidplayer.music.ui.player.BottomSheetPlayer
import com.voidplayer.music.ui.screens.Screens
import com.voidplayer.music.ui.screens.SettingDialoge
import com.voidplayer.music.ui.screens.navigationBuilder
import com.voidplayer.music.ui.screens.settings.DarkMode
import com.voidplayer.music.ui.screens.settings.NavigationTab
import com.voidplayer.music.ui.screens.settings.RingtoneViewModel
import com.voidplayer.music.ui.theme.ColorSaver
import com.voidplayer.music.ui.theme.DefaultThemeColor
import com.voidplayer.music.ui.theme.VoidColors
import com.voidplayer.music.ui.theme.extractThemeColor
import com.voidplayer.music.ui.theme.rememberVoidGreeting
import com.voidplayer.music.ui.theme.voidPlayerTheme
import com.voidplayer.music.ui.utils.appBarScrollBehavior
import com.voidplayer.music.ui.utils.resetHeightOffset
import com.voidplayer.music.utils.dataStore
import com.voidplayer.music.utils.get
import com.voidplayer.music.utils.rememberEnumPreference
import com.voidplayer.music.utils.rememberPreference
import com.voidplayer.music.utils.reportException
import com.voidplayer.music.utils.setAppLocale
import com.voidplayer.music.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import javax.inject.Inject
import com.voidplayer.music.constants.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_SEARCH = "com.voidplayer.music.action.SEARCH"
        const val ACTION_LIBRARY = "com.voidplayer.music.action.LIBRARY"
    }

    @Inject
    lateinit var database: MusicDatabase
    @Inject
    lateinit var downloadUtil: DownloadUtil
    @Inject
    lateinit var syncUtils: com.voidplayer.music.utils.SyncUtils

    private lateinit var navController: NavHostController
    private var pendingIntent: Intent? = null
    private var playerConnection by mutableStateOf<PlayerConnection?>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                try {
                    playerConnection = PlayerConnection(this@MainActivity, service, database, lifecycleScope)
                } catch (e: Exception) {
                    lifecycleScope.launch {
                        delay(500)
                        try {
                            playerConnection = PlayerConnection(this@MainActivity, service, database, lifecycleScope)
                        } catch (e2: Exception) {
                            Timber.tag("MainActivity").e(e2, "Failed to create PlayerConnection on retry")
                        }
                    }
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1000)
            }
        }
        bindService(Intent(this, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::navController.isInitialized) {
            handleDeepLinkIntent(intent, navController)
            handleAssistantSearchIntent(intent, navController)
        } else {
            pendingIntent = intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val locale = dataStore[AppLanguageKey]
                ?.takeUnless { it == SYSTEM_DEFAULT }
                ?.let { Locale.forLanguageTag(it) }
                ?: Locale.getDefault()
            setAppLocale(this, locale)
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        setContent {
            voidPlayerApp(
                playerConnection = playerConnection,
                database = database,
                downloadUtil = downloadUtil,
            )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun voidPlayerApp(
        playerConnection: PlayerConnection?,
        database: MusicDatabase,
        downloadUtil: DownloadUtil,
    ) {
        val enableDynamicTheme by rememberPreference(DynamicThemeKey, defaultValue = true)
        val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
            if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
        }

        LaunchedEffect(useDarkTheme) { setSystemBarAppearance(useDarkTheme) }

        val pureBlackEnabled by rememberPreference(PureBlackKey, defaultValue = false)
        val pureBlack = remember(pureBlackEnabled, useDarkTheme) { pureBlackEnabled && useDarkTheme }

        val oldGreenArgb = Color(0xFF1DB954).toArgb()
        val (selectedThemeColorInt) = rememberPreference(SelectedThemeColorKey, defaultValue = DefaultThemeColor.toArgb())
        val activeThemeColorInt = if (selectedThemeColorInt == oldGreenArgb) DefaultThemeColor.toArgb() else selectedThemeColorInt
        val selectedThemeColor = Color(activeThemeColorInt)
        var themeColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(selectedThemeColor) }

        LaunchedEffect(selectedThemeColor) { if (!enableDynamicTheme) themeColor = selectedThemeColor }

        LaunchedEffect(playerConnection, enableDynamicTheme, selectedThemeColor) {
            val conn = playerConnection ?: return@LaunchedEffect
            if (!enableDynamicTheme) return@LaunchedEffect
            conn.service.currentMediaMetadata.collectLatest { song ->
                if (song?.thumbnailUrl != null) {
                    withContext(Dispatchers.IO) {
                        try {
                            val result = imageLoader.execute(
                                ImageRequest.Builder(this@MainActivity)
                                    .data(song.thumbnailUrl)
                                    .allowHardware(false)
                                    .build()
                            )
                            themeColor = result.image?.toBitmap()?.extractThemeColor() ?: selectedThemeColor
                        } catch (e: Exception) { themeColor = selectedThemeColor }
                    }
                } else themeColor = selectedThemeColor
            }
        }

        voidPlayerTheme(darkTheme = useDarkTheme, pureBlack = pureBlack, themeColor = themeColor) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.surface)) {
                val focusManager = LocalFocusManager.current
                val density = LocalDensity.current
                val configuration = LocalWindowInfo.current
                val cutoutInsets = WindowInsets.displayCutout
                val windowsInsets = WindowInsets.systemBars
                val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }
                val bottomInsetDp = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = hiltViewModel()
                val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val (previousTab, setPreviousTab) = rememberSaveable { mutableStateOf("home") }

                val navigationItems = remember { Screens.MainScreens }
                val (useNewMiniPlayerDesign) = rememberPreference(UseNewMiniPlayerDesignKey, defaultValue = true)
                val defaultOpenTab = remember { dataStore[DefaultOpenTabKey].toEnum(defaultValue = NavigationTab.HOME) }
                val tabOpenedFromShortcut = remember {
                    when (intent?.action) {
                        ACTION_SEARCH -> NavigationTab.LIBRARY
                        ACTION_LIBRARY -> NavigationTab.SEARCH
                        else -> null
                    }
                }

                val topLevelScreens = remember { listOf(Screens.Home.route, Screens.Library.route, "settings") }
                val (query, onQueryChange) = rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

                val onSearch: (String) -> Unit = remember {
                    { searchQuery ->
                        if (searchQuery.isNotEmpty()) {
                            navController.navigate("search/${URLEncoder.encode(searchQuery, "UTF-8")}")
                            lifecycleScope.launch(Dispatchers.IO) { database.query { insert(SearchHistory(query = searchQuery)) } }
                        }
                    }
                }

                val (innerTubeCookie, _) = rememberPreference(InnerTubeCookieKey, "")
                val isLoggedIn by remember(innerTubeCookie) {
                    derivedStateOf { innerTubeCookie.isNotEmpty() && "SAPISID" in parseCookieString(innerTubeCookie) }
                }
                val (lastOpenedVersionCode, setLastOpenedVersionCode) = rememberPreference(LastOpenedVersionCodeKey, -1)
                val showWelcome by remember(lastOpenedVersionCode) {
                    derivedStateOf { lastOpenedVersionCode < BuildConfig.VERSION_CODE }
                }

                val currentRoute by remember { derivedStateOf { navBackStackEntry?.destination?.route } }
                val inSearchScreen by remember { derivedStateOf { currentRoute?.startsWith("search/") == true } }
                val navigationItemRoutes = remember(navigationItems) { navigationItems.map { it.route }.toSet() }

                val shouldShowNavigationBar = remember(currentRoute, navigationItemRoutes, showWelcome) {
                    if (showWelcome || currentRoute == "welcome") false
                    else if (currentRoute == null) true
                    else (navigationItemRoutes.contains(currentRoute) || currentRoute!!.startsWith("search/"))
                }

                val isLandscape = configuration.containerDpSize.width > configuration.containerDpSize.height
                val showRail = isLandscape && !inSearchScreen
                val navPadding = if (shouldShowNavigationBar && !showRail) NavigationBarHeight + FloatingToolbarBottomPadding else 0.dp

                val navigationBarHeight by animateDpAsState(
                    targetValue = if (shouldShowNavigationBar && !showRail) NavigationBarHeight else 0.dp,
                    animationSpec = NavigationBarAnimationSpec, label = "navBarHeight"
                )

                val (useFloatingNavBar) = rememberPreference(UseFloatingNavBarKey, defaultValue = false)
                val floatingNavBarScrollConnection = rememberFloatingTabBarScrollConnection()

                val playerBottomSheetState = rememberBottomSheetState(
                    dismissedBound = 0.dp,
                    collapsedBound = if (useFloatingNavBar && !showRail && shouldShowNavigationBar) 0.dp 
                    else bottomInset + (if (!showRail && shouldShowNavigationBar) navPadding else 0.dp) + (if (useNewMiniPlayerDesign) MiniPlayerBottomSpacing else 0.dp) + MiniPlayerHeight,
                    expandedBound = maxHeight
                )

                val playerMediaMetadata = playerConnection?.player?.currentMediaItem?.mediaMetadata
                val hasDockedPlayerAccessory = useFloatingNavBar && playerMediaMetadata != null && !showRail && shouldShowNavigationBar

                val playerAwareWindowInsets = remember(bottomInset, shouldShowNavigationBar, playerBottomSheetState.isDismissed, showRail) {
                    var bottom = bottomInset
                    if (shouldShowNavigationBar && !showRail) bottom += NavigationBarHeight
                    if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
                    windowsInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top).add(WindowInsets(top = AppBarHeight, bottom = bottom))
                }

                val topAppBarScrollBehavior = appBarScrollBehavior(canScroll = { !inSearchScreen && (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed) })

                LaunchedEffect(navBackStackEntry) {
                    if (inSearchScreen) {
                        val searchQuery = withContext(Dispatchers.IO) {
                            val rawQuery = navBackStackEntry?.arguments?.getString("query")!!
                            try { URLDecoder.decode(rawQuery, "UTF-8") } catch (e: IllegalArgumentException) { rawQuery }
                        }
                        onQueryChange(TextFieldValue(searchQuery, TextRange(searchQuery.length)))
                    } else if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) onQueryChange(TextFieldValue())
                    
                    if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } && navigationItems.fastAny { it.route == previousTab }) topAppBarScrollBehavior.state.resetHeightOffset()
                    topAppBarScrollBehavior.state.resetHeightOffset()
                    navController.currentBackStackEntry?.destination?.route?.let { setPreviousTab(it) }
                }

                LaunchedEffect(playerConnection) {
                    val player = playerConnection?.player ?: return@LaunchedEffect
                    if (player.currentMediaItem == null) { if (!playerBottomSheetState.isDismissed) playerBottomSheetState.dismiss() }
                    else { if (playerBottomSheetState.isDismissed) playerBottomSheetState.collapseSoft() }
                }

                DisposableEffect(playerConnection, playerBottomSheetState) {
                    val player = playerConnection?.player ?: return@DisposableEffect onDispose { }
                    val listener = object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null && playerBottomSheetState.isDismissed) playerBottomSheetState.collapseSoft()
                        }
                    }
                    player.addListener(listener)
                    onDispose { player.removeListener(listener) }
                }

                var shouldShowTopBar by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(navBackStackEntry) {
                    val currentRoute = navBackStackEntry?.destination?.route
                    shouldShowTopBar = currentRoute in topLevelScreens && currentRoute != "settings"
                }

                val coroutineScope = rememberCoroutineScope()
                var sharedSong: SongItem? by remember { mutableStateOf(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                var showSettingDialoge by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (pendingIntent != null) {
                        handleDeepLinkIntent(pendingIntent!!, navController)
                        handleAssistantSearchIntent(pendingIntent!!, navController)
                        pendingIntent = null
                    }
                }

                DisposableEffect(Unit) {
                    val listener = Consumer<Intent> { intent ->
                        if (intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEND) handleDeepLinkIntent(intent, navController)
                        else if (intent.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) handleAssistantSearchIntent(intent, navController)
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                val currentTitle = when (navBackStackEntry?.destination?.route) {
                    Screens.Home.route -> "VOID"
                    Screens.Search.route -> stringResource(R.string.search)
                    Screens.Library.route -> stringResource(R.string.filter_library)
                    else -> ""
                }

                val eventCount by database.eventCount().collectAsState(initial = 0)
                val showHistoryButton = remember(eventCount) { eventCount != 0 }
                val baseBg = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer

                val ringtoneViewModel: RingtoneViewModel = viewModel()
                val ringtoneUiState by ringtoneViewModel.uiState.collectAsState()

                CompositionLocalProvider(
                    LocalRingtoneViewModel provides ringtoneViewModel,
                    LocalDatabase provides database,
                    LocalContentColor provides if (pureBlack) Color.White else contentColorFor(MaterialTheme.colorScheme.surface),
                    LocalPlayerConnection provides playerConnection,
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                    LocalDownloadUtil provides downloadUtil,
                    LocalShimmerTheme provides getShimmerTheme(),
                    LocalSyncUtils provides syncUtils,
                ) {
                    val localPlayerConnectionForGreeting = LocalPlayerConnection.current
                    val greetingMediaMetadata = localPlayerConnectionForGreeting?.mediaMetadata?.collectAsState()?.value
                    val accountName by homeViewModel.accountName.collectAsState()
                    val voidGreeting = rememberVoidGreeting(
                        currentTitle = greetingMediaMetadata?.title,
                        currentArtist = greetingMediaMetadata?.artists?.joinToString(", ") { it.name },
                        accountName = accountName,
                        currentRoute = navBackStackEntry?.destination?.route
                    )

                    val onNavItemClick: (Screens, Boolean) -> Unit = remember(navController, coroutineScope, topAppBarScrollBehavior, playerBottomSheetState) {
                        { screen: Screens, isSelected: Boolean ->
                            if (playerBottomSheetState.isExpanded) playerBottomSheetState.collapseSoft()
                            if (isSelected) {
                                navController.currentBackStackEntry?.savedStateHandle?.set("scrollToTop", true)
                                coroutineScope.launch { topAppBarScrollBehavior.state.resetHeightOffset() }
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            if (shouldShowNavigationBar) {
                                AnimatedVisibility(visible = shouldShowTopBar, enter = fadeIn(tween(300)), exit = fadeOut(tween(200))) {
                                    TopAppBar(
                                        title = {
                                            if (navBackStackEntry?.destination?.route == Screens.Home.route) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.Bottom) {
                                                        Text(text = "VOID", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 24.sp))
                                                        Text(text = ".", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 24.sp), color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    val (easterEggTriggered, onEasterEggTriggeredChange) = rememberPreference(
                                                        key = com.voidplayer.music.constants.EasterEggTriggeredKey,
                                                        defaultValue = false
                                                    )
                                                    var fastClickCount by remember { mutableStateOf(0) }
                                                    var lastFastClickTime by remember { mutableStateOf(0L) }
                                                    var overrideText by remember { mutableStateOf<String?>(null) }

                                                    var showSecondPart by remember(voidGreeting) { mutableStateOf(false) }
                                                    var isPulsing by remember { mutableStateOf(false) }
                                                    val pulseScale by animateFloatAsState(
                                                        targetValue = if (isPulsing) 1.25f else 1.0f,
                                                        animationSpec = tween(180),
                                                        label = "pulseScale"
                                                    )
                                                    LaunchedEffect(isPulsing) {
                                                        if (isPulsing) {
                                                            delay(200)
                                                            isPulsing = false
                                                        }
                                                    }
                                                    val pulseInfinite = rememberInfiniteTransition(label = "pulseAlpha")
                                                    val pulseAlpha by pulseInfinite.animateFloat(
                                                        initialValue = 0.5f,
                                                        targetValue = 1.0f,
                                                        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                                                        label = "pulseAlphaVal"
                                                    )
                                                    val greetingText = overrideText ?: if (showSecondPart && voidGreeting.secondPart != null) {
                                                        voidGreeting.secondPart!!
                                                    } else {
                                                        voidGreeting.firstPart
                                                    }
                                                    val isRainbowText = voidGreeting.isRainbow || overrideText != null
                                                    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
                                                    val offset by infiniteTransition.animateFloat(0f, 1000f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "offset")
                                                    val rainbowBrush = remember(offset) {
                                                        Brush.linearGradient(
                                                            colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta, Color.Red),
                                                            start = Offset(offset, 0f), end = Offset(offset + 300f, 0f), tileMode = TileMode.Repeated
                                                        )
                                                    }
                                                    Text(
                                                        text = greetingText,
                                                        style = if (isRainbowText) MaterialTheme.typography.labelMedium.copy(brush = rainbowBrush) else MaterialTheme.typography.labelMedium,
                                                        color = if (isRainbowText) Color.Unspecified else if (greetingText.uppercase() == greetingText && greetingText.endsWith("MODE")) VoidColors.Text2 else MaterialTheme.colorScheme.secondary,
                                                        fontWeight = FontWeight.Medium, maxLines = 1,
                                                        modifier = Modifier
                                                            .graphicsLayer(
                                                                scaleX = pulseScale,
                                                                scaleY = pulseScale,
                                                                alpha = if (voidGreeting.secondPart != null && !showSecondPart) pulseAlpha else 1f
                                                            )
                                                            .clickable {
                                                                isPulsing = true
                                                                val now = System.currentTimeMillis()
                                                                if (now - lastFastClickTime < 350L) {
                                                                    fastClickCount++
                                                                } else {
                                                                    fastClickCount = 1
                                                                }
                                                                lastFastClickTime = now

                                                                if (fastClickCount >= 20 && !easterEggTriggered) {
                                                                    onEasterEggTriggeredChange(true)
                                                                    overrideText = "Never gonna give you up-"
                                                                } else if (voidGreeting.secondPart != null) {
                                                                    showSecondPart = !showSecondPart
                                                                }
                                                            }
                                                            .basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 3000, velocity = 30.dp)
                                                    )
                                                }
                                            } else Text(text = currentTitle, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp))
                                        },
                                        actions = {
                                             IconButton(onClick = { showSettingDialoge = true }) {
                                                BadgedBox(badge = {}) {
                                                    if (accountImageUrl != null) {
                                                        AsyncImage(model = accountImageUrl, contentDescription = stringResource(R.string.account), modifier = Modifier.size(24.dp).clip(CircleShape))
                                                     } else {
                                                         Icon(painter = painterResource(R.drawable.settings), contentDescription = stringResource(R.string.account), modifier = Modifier.size(24.dp))
                                                     }
                                                }
                                            }
                                        },
                                        scrollBehavior = topAppBarScrollBehavior,
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                                            scrolledContainerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                                            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
                                        modifier = Modifier.windowInsetsPadding(if (showRail) WindowInsets(left = NavigationBarHeight).add(cutoutInsets.only(WindowInsetsSides.Start)) else cutoutInsets.only(WindowInsetsSides.Start + WindowInsetsSides.End))
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (shouldShowNavigationBar) {
                                if (!showRail) {
                                    Box {
                                        BottomSheetPlayer(state = playerBottomSheetState, navController = navController, pureBlack = pureBlack)
                                        val navSlideDistance = bottomInset + FloatingToolbarBottomPadding + NavigationBarHeight
                                        val navOffsetY = if (navigationBarHeight == 0.dp) navSlideDistance else {
                                            val slideOffset = navSlideDistance * playerBottomSheetState.progress.coerceIn(0f, 1f)
                                            val hideOffset = navSlideDistance * (1 - navigationBarHeight.coerceAtMost(NavigationBarHeight) / NavigationBarHeight)
                                            slideOffset + hideOffset
                                        }
                                        if (useFloatingNavBar) {
                                            AppFloatingNavBar(
                                                navigationItems = navigationItems, currentRoute = currentRoute, onItemClick = onNavItemClick, scrollConnection = floatingNavBarScrollConnection, pureBlack = pureBlack, showPlayerAccessory = hasDockedPlayerAccessory, onAccessoryClick = { playerBottomSheetState.expandSoft() },
                                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = (bottomInset + FloatingToolbarBottomPadding).coerceAtLeast(0.dp))
                                                    .graphicsLayer {
                                                        val hiddenOffset = size.height + (bottomInset + FloatingToolbarBottomPadding).coerceAtLeast(0.dp).toPx()
                                                        val navBarHeightPx = navigationBarHeight.toPx()
                                                        translationY = if (navBarHeightPx == 0f) hiddenOffset else {
                                                            val progress = playerBottomSheetState.progress.coerceIn(0f, 1f)
                                                            val slideOffset = hiddenOffset * progress
                                                            val hideOffset = hiddenOffset * (1 - navBarHeightPx / NavigationBarHeight.toPx())
                                                            slideOffset + hideOffset
                                                        }
                                                    }
                                            )
                                        } else {
                                            Box(modifier = Modifier.align(Alignment.BottomCenter).height(navSlideDistance).offset(y = navOffsetY)) {
                                                FloatingNavigationToolbar(
                                                    items = navigationItems, pureBlack = pureBlack, isSelected = { screen -> currentRoute == screen.route || currentRoute?.startsWith("${screen.route}/") == true }, onItemClick = onNavItemClick,
                                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = (bottomInset + FloatingToolbarBottomPadding).coerceAtLeast(0.dp)).height(NavigationBarHeight)
                                                )
                                            }
                                            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(bottomInsetDp).graphicsLayer { alpha = if (playerBottomSheetState.progress > 0f || (useNewMiniPlayerDesign && !shouldShowNavigationBar)) 0f else 1f }.background(baseBg))
                                        }
                                    }
                                } else Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(bottomInsetDp).graphicsLayer { alpha = if (playerBottomSheetState.progress > 0f || (useNewMiniPlayerDesign && !shouldShowNavigationBar)) 0f else 1f }.background(baseBg))
                            }
                        },
                        modifier = Modifier.fillMaxSize().nestedScroll(topAppBarScrollBehavior.nestedScrollConnection).then(if (useFloatingNavBar) Modifier.nestedScroll(floatingNavBarScrollConnection) else Modifier)
                    ) { paddingValues ->
                        Row(Modifier.fillMaxSize()) {
                            if (showRail && currentRoute != "update") {
                                AppNavigationRail(
                                    navigationItems = navigationItems,
                                    currentRoute = currentRoute,
                                    onItemClick = onNavItemClick,
                                    pureBlack = pureBlack,
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                NavHost(
                                    navController = navController,
                                    startDestination = if (showWelcome) "welcome" else {
                                        when (tabOpenedFromShortcut ?: defaultOpenTab) {
                                            NavigationTab.HOME -> Screens.Home
                                            NavigationTab.LIBRARY -> Screens.Library
                                            else -> Screens.Home
                                        }.route
                                    },
                                    enterTransition = {
                                        val currentRouteIndex = navigationItems.indexOfFirst { it.route == targetState.destination.route }
                                        val previousRouteIndex = navigationItems.indexOfFirst { it.route == initialState.destination.route }
                                        if (currentRouteIndex == -1 || currentRouteIndex > previousRouteIndex) slideInHorizontally { it / 8 } + fadeIn(tween(200))
                                        else slideInHorizontally { -it / 8 } + fadeIn(tween(200))
                                    },
                                    exitTransition = {
                                        val currentRouteIndex = navigationItems.indexOfFirst { it.route == initialState.destination.route }
                                        val targetRouteIndex = navigationItems.indexOfFirst { it.route == targetState.destination.route }
                                        if (targetRouteIndex == -1 || targetRouteIndex > currentRouteIndex) slideOutHorizontally { -it / 8 } + fadeOut(tween(200))
                                        else slideOutHorizontally { it / 8 } + fadeOut(tween(200))
                                    },
                                    modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                                ) {
                                    navigationBuilder(
                                        navController = navController,
                                        scrollBehavior = topAppBarScrollBehavior,
                                        activity = this@MainActivity,
                                        snackbarHostState = snackbarHostState,
                                        isUpdate = lastOpenedVersionCode != -1,
                                        onWelcomeFinished = { code ->
                                            setLastOpenedVersionCode(code)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    BottomSheetMenu(state = LocalMenuState.current, modifier = Modifier.align(Alignment.BottomCenter))
                    BottomSheetPage(state = LocalBottomSheetPageState.current, modifier = Modifier.align(Alignment.BottomCenter))

                    sharedSong?.let { song ->
                        playerConnection?.let {
                            Dialog(
                                onDismissRequest = { sharedSong = null },
                                properties = DialogProperties(usePlatformDefaultWidth = false),
                            ) {
                                Surface(
                                    modifier = Modifier.padding(24.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = AlertDialogDefaults.containerColor,
                                    tonalElevation = AlertDialogDefaults.TonalElevation,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        YouTubeSongMenu(song = song, navController = navController, onDismiss = { sharedSong = null })
                                    }
                                }
                            }
                        }
                    }

                    RingtoneTrimmerDialog(
                        isVisible = ringtoneUiState.showTrimmer,
                        songId = ringtoneUiState.targetSongId,
                        songTitle = ringtoneUiState.targetSongTitle,
                        duration = ringtoneUiState.targetSongDuration,
                        onDismiss = { ringtoneViewModel.hideTrimmer() },
                        onResolveStreamUrl = { ringtoneViewModel.getStreamUrl(this@MainActivity, it) },
                        onConfirm = { start, end -> ringtoneViewModel.setAsRingtone(this@MainActivity, start, end) }
                    )

                    if (ringtoneUiState.showProgress) {
                        RingtoneProgressDialog(
                            isVisible = ringtoneUiState.showProgress,
                            progress = ringtoneUiState.progress,
                            statusMessage = ringtoneUiState.statusMessage,
                            isComplete = ringtoneUiState.isComplete,
                            isSuccess = ringtoneUiState.isSuccess,
                            onDismiss = { ringtoneViewModel.dismissProgress() },
                            onOpenSettings = { ringtoneViewModel.openRingtoneSettings(this@MainActivity) }
                        )
                    }

                    if (showSettingDialoge) {
                        SettingDialoge(
                            onDismissRequest = { showSettingDialoge = false },
                            onNavigate = { route -> showSettingDialoge = false; navController.navigate(route) },
                            homeViewModel = homeViewModel
                        )
                    }
                }
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent, navController: NavHostController) {
        var uri = intent.data
        if (uri == null) {
            val extraText = intent.extras?.getString(Intent.EXTRA_TEXT)
            if (extraText != null) {
                val urlRegex = "(https?://[^\\s]+)".toRegex()
                val match = urlRegex.find(extraText)
                if (match != null) uri = match.value.toUri()
            }
        }
        if (uri == null) return
        intent.data = null
        intent.removeExtra(Intent.EXTRA_TEXT)
        val coroutineScope = lifecycle.coroutineScope
        when (val path = uri.pathSegments.firstOrNull()) {
            "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                if (playlistId.startsWith("OLAK5uy_")) {
                    coroutineScope.launch(Dispatchers.IO) {
                        YouTube.albumSongs(playlistId).onSuccess { songs ->
                            songs.firstOrNull()?.album?.id?.let { browseId ->
                                withContext(Dispatchers.Main) { navController.navigate("album/$browseId") }
                            }
                        }.onFailure { reportException(it) }
                    }
                } else navController.navigate("online_playlist/$playlistId")
            }
            "browse" -> uri.lastPathSegment?.let { browseId -> navController.navigate("album/$browseId") }
            "channel", "c" -> uri.lastPathSegment?.let { artistId -> navController.navigate("artist/$artistId") }
            "search" -> uri.getQueryParameter("q")?.let { navController.navigate("search/${URLEncoder.encode(it, "UTF-8")}") }
            else -> {
                val videoId = when {
                    path == "watch" -> uri.getQueryParameter("v")
                    uri.host == "youtu.be" || uri.host == "share.voidplayer.fun" -> uri.pathSegments.firstOrNull()
                    else -> null
                }
                val playlistId = uri.getQueryParameter("list")
                if (videoId != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        YouTube.queue(listOf(videoId), playlistId).onSuccess { queue ->
                            withContext(Dispatchers.Main) {
                                var attempts = 0
                                while (playerConnection == null && attempts < 20) { delay(100); attempts++ }
                                playerConnection?.playQueue(YouTubeQueue(WatchEndpoint(videoId = queue.firstOrNull()?.id, playlistId = playlistId), queue.firstOrNull()?.toMediaMetadata()))
                            }
                        }.onFailure { reportException(it) }
                    }
                } else if (playlistId != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        YouTube.queue(null, playlistId).onSuccess { queue ->
                            val firstItem = queue.firstOrNull()
                            withContext(Dispatchers.Main) {
                                var attempts = 0
                                while (playerConnection == null && attempts < 20) { delay(100); attempts++ }
                                playerConnection?.playQueue(YouTubeQueue(WatchEndpoint(videoId = firstItem?.id, playlistId = playlistId), firstItem?.toMediaMetadata()))
                            }
                        }.onFailure { reportException(it) }
                    }
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setSystemBarAppearance(isDark: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView.rootView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.navigationBarColor = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
    }

    private fun handleAssistantSearchIntent(intent: Intent, navController: NavHostController) {
        if (intent.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            val query = intent.getStringExtra(android.app.SearchManager.QUERY) ?: return
            navController.navigate("search/${URLEncoder.encode(query, "UTF-8")}")
        }
    }
}

val LocalSyncUtils = androidx.compose.runtime.staticCompositionLocalOf<com.voidplayer.music.utils.SyncUtils> { error("No SyncUtils provided") }
val LocalDatabase = staticCompositionLocalOf<MusicDatabase> { error("No database provided") }
val LocalRingtoneViewModel = compositionLocalOf<RingtoneViewModel> { error("No RingtoneViewModel provided") }
val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalDownloadUtil = staticCompositionLocalOf<DownloadUtil> { error("No DownloadUtil provided") }
val LocalIsPlayerExpanded = compositionLocalOf { false }
