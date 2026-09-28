@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.voidplayer.music.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.voidplayer.music.ui.screens.Screens
import com.voidplayer.music.ui.theme.VoidColors

/**
 * Slim, edge-to-edge bottom navigation bar.
 *
 * Replaces the old [HorizontalFloatingToolbar] + three-dot overflow design.
 * Uses a standard Material 3–styled bar that sits flush at the screen bottom,
 * matching the compact proportions of the reference design (Screenshot 2).
 *
 * The overflow-menu parameters (shuffle, settings, music recognition) are
 * retained in the signature for call-site compatibility but are intentionally
 * unused here — settings are accessible via the Settings destination and
 * shuffle via the player controls.
 */
@Composable
fun FloatingNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    onShuffleClick: (() -> Unit)? = null,
    shuffleIconRes: Int? = null,
    shuffleContentDescription: String = "",
    onSettingsClick: (() -> Unit)? = null,
    settingsIconRes: Int? = null,
    settingsContentDescription: String = "",
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
) {
    val backgroundColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { screen ->
                val selected = isSelected(screen)
                SlimNavItem(
                    screen = screen,
                    selected = selected,
                    pureBlack = pureBlack,
                    onClick = { onItemClick(screen, selected) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SlimNavItem(
    screen: Screens,
    selected: Boolean,
    pureBlack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(targetState = selected, label = "navItem_${screen.route}")

    val activeColor = MaterialTheme.colorScheme.primary
    val contentColor by transition.animateColor(
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) },
        label = "contentColor",
    ) { isSelected ->
        if (isSelected) {
            if (pureBlack) Color.White else activeColor
        } else {
            if (pureBlack) Color.White.copy(alpha = 0.55f) else VoidColors.Text3
        }
    }

    val iconScale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        },
        label = "iconScale",
    ) { isSelected -> if (isSelected) 1.12f else 1.0f }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressScale",
    )

    // Pill indicator behind the active icon
    val pillAlpha by transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "pillAlpha",
    ) { isSelected -> if (isSelected) 1f else 0f }

    Box(
        modifier = modifier
            .scale(pressScale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle pill background for selected item
        val pillColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = pillColor.copy(alpha = 0.13f * pillAlpha),
                )
        )

        Crossfade(
            targetState = selected,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "iconCrossfade",
            modifier = Modifier.scale(iconScale),
        ) { isSelected ->
            Icon(
                painter = painterResource(if (isSelected) screen.iconIdActive else screen.iconIdInactive),
                contentDescription = stringResource(screen.titleId),
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun floatingToolbarContainerColor(pureBlack: Boolean): Color {
    return if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
}

@Composable
private fun floatingToolbarSelectedItemContainerColor(pureBlack: Boolean): Color {
    return MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
}

@Composable
private fun floatingToolbarSelectedItemContentColor(pureBlack: Boolean): Color {
    return MaterialTheme.colorScheme.primary
}

@Composable
private fun floatingToolbarItemContentColor(pureBlack: Boolean): Color {
    return VoidColors.Text3
}

@Composable
private fun floatingToolbarMenuIconContainerColor(pureBlack: Boolean): Color {
    return MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun floatingToolbarMenuIconContentColor(pureBlack: Boolean): Color {
    return MaterialTheme.colorScheme.onSecondaryContainer
}

