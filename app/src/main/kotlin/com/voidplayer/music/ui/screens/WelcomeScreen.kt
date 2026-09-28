package com.voidplayer.music.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidplayer.music.R
import com.voidplayer.music.ui.theme.VoidColors
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    isUpdate: Boolean = false,
    isLoggedIn: Boolean = false,
    onLogin: () -> Unit,
    onGetStarted: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { if (isUpdate) 1 else 5 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        VoidColors.Bg,
                        VoidColors.Bg.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = !isUpdate
            ) { page ->
                when {
                    isUpdate -> UpdatePage()
                    page == 0 -> IntroPage()
                    page == 1 -> FeaturesPage()
                    page == 2 -> StudioPage()
                    page == 3 -> OfflinePage()
                    page == 4 -> LoginPage(isLoggedIn, onLogin, onGetStarted)
                }
            }

            // Bottom Navigation Area
            if (!isUpdate && pagerState.currentPage < 4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { iteration ->
                            val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f)
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == iteration) 24.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }

                    // Next/Skip Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { coroutineScope.launch { pagerState.scrollToPage(4) } }) {
                            Text(stringResource(R.string.welcome_skip), color = VoidColors.Text2)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.welcome_next))
                        }
                    }
                }
            } else if (isUpdate) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(stringResource(R.string.continue_to_app), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun IntroPage() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_animation")
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "logo_scale"
    )
    val alphaState = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "logo_alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(200.dp).graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = alphaState.value
            },
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_nobg), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = "VOID", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = Color.White)
            Text(text = ".", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.welcome_description), style = MaterialTheme.typography.bodyLarge, color = VoidColors.Text2, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FeaturesPage() {
    OnboardingContent(
        iconRes = R.drawable.music_note,
        title = stringResource(R.string.welcome_page_features_title),
        description = stringResource(R.string.welcome_page_features_desc)
    )
}

@Composable
private fun StudioPage() {
    OnboardingContent(
        iconRes = R.drawable.graphic_eq,
        title = stringResource(R.string.welcome_page_studio_title),
        description = stringResource(R.string.welcome_page_studio_desc)
    )
}

@Composable
private fun OfflinePage() {
    OnboardingContent(
        iconRes = R.drawable.download,
        title = stringResource(R.string.welcome_page_offline_title),
        description = stringResource(R.string.welcome_page_offline_desc)
    )
}

@Composable
private fun LoginPage(isLoggedIn: Boolean, onLogin: () -> Unit, onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(painter = painterResource(id = R.drawable.account), contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.welcome_page_login_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.welcome_page_login_desc), style = MaterialTheme.typography.bodyLarge, color = VoidColors.Text2, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(48.dp))

        if (!isLoggedIn) {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sign in to YouTube Music")
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onGetStarted) {
                Text(stringResource(R.string.welcome_later), color = VoidColors.Text2)
            }
        } else {
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(stringResource(R.string.welcome_get_started), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun UpdatePage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_google), 
            contentDescription = null, 
            modifier = Modifier.size(100.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.welcome_whats_new), 
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black), 
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Update successful",
            style = MaterialTheme.typography.titleMedium,
            color = VoidColors.Text2
        )
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Text(
                text = stringResource(R.string.changelog_beta_v010),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun OnboardingContent(iconRes: Int, title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(48.dp))
        Text(text = title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, style = MaterialTheme.typography.bodyLarge, color = VoidColors.Text2, textAlign = TextAlign.Center)
    }
}
