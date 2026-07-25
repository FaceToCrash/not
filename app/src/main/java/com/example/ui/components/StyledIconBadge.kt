package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan

/**
 * Reusable 3D Glassmorphic Icon Badge matching the user's uploaded icon design theme.
 */
@Composable
fun StyledUserIconBadge(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    shapeRadius: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(shapeRadius),
                ambientColor = PrimaryPurple,
                spotColor = SecondaryCyan
            )
            .clip(RoundedCornerShape(shapeRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2E1C4E),
                        Color(0xFF1E1535),
                        Color(0xFF121226)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFF818CF8),
                        Color(0xFF38BDF8)
                    )
                ),
                shape = RoundedCornerShape(shapeRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_user_style_icon),
            contentDescription = "App Icon Style",
            modifier = Modifier
                .size(size * 0.75f)
                .clip(RoundedCornerShape(shapeRadius * 0.7f))
        )
    }
}

/**
 * Custom 3D Glass Container for standard vector icons.
 */
@Composable
fun StyledIconTile(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = size * 0.55f,
    accentColor: Color = PrimaryPurple,
    containerColor: Color = Color(0xFF1A1A2A),
    shapeRadius: Dp = 12.dp,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .then(clickModifier)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(shapeRadius),
                ambientColor = accentColor,
                spotColor = accentColor
            )
            .clip(RoundedCornerShape(shapeRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.25f),
                        containerColor,
                        Color(0xFF0F0F18)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.8f),
                        accentColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(shapeRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = accentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
