package com.voidplayer.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voidplayer.music.ui.theme.VoidColors
import com.voidplayer.music.ui.theme.VoidRadius

/**
 * VOID's "special grid" library tile -- ported from the web app's
 * `.lib-spotify-item` / `renderSpecialGrid()`: a big rounded-square cover
 * with a centered icon, the name below it, and a small meta line (song
 * count) below that. Used for Favorites, Recently Played, Local Songs, and
 * the Upload Files tile on the Library screen.
 *
 * Sized the same way Home's grid tiles are (GridThumbnailHeight-based
 * square, VoidRadius.Medium corners) so Library reads as an extension of
 * Home's visual language rather than its own thing.
 */
@Composable
fun LibrarySpotifyCard(
    title: String,
    meta: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverBackground: Color = Color.White.copy(alpha = 0.05f),
    coverBorder: Color? = null,
    coverBorderDashed: Boolean = false,
    iconTint: Color = VoidColors.Text2,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(VoidRadius.Medium))
                .background(coverBackground)
                .then(
                    if (coverBorder != null) {
                        Modifier.border(
                            width = if (coverBorderDashed) 1.5.dp else 1.dp,
                            color = coverBorder,
                            shape = RoundedCornerShape(VoidRadius.Medium)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.aspectRatio(1f).padding(28.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = meta,
            style = MaterialTheme.typography.labelSmall,
            color = VoidColors.Text2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

