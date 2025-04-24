package com.example.newsexplorer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isBookmarked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    val color by animateColorAsState(
        if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        label = "color"
    )

    Icon(
        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
        contentDescription = if (isBookmarked) "Remove from bookmarks" else "Add to bookmarks",
        tint = color,
        modifier = modifier
            .size(24.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onToggleBookmark()
            }
    )
}
