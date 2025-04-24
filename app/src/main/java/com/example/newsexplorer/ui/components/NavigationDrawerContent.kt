package com.example.newsexplorer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NavigationDrawerContent(
    onCategorySelected: (String) -> Unit,
    onSavedArticlesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    selectedCategory: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "News Explorer",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        HorizontalDivider()

        // Home option
        DrawerItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = selectedCategory == "general",
            onClick = { onCategorySelected("general") }
        )

        // Categories
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        DrawerItem(
            icon = Icons.Default.Business,
            label = "Business",
            isSelected = selectedCategory == "business",
            onClick = { onCategorySelected("business") }
        )

        DrawerItem(
            icon = Icons.Default.LocalMovies,
            label = "Entertainment",
            isSelected = selectedCategory == "entertainment",
            onClick = { onCategorySelected("entertainment") }
        )

        DrawerItem(
            icon = Icons.Default.Science,
            label = "Science & Technology",
            isSelected = selectedCategory == "science" || selectedCategory == "technology",
            onClick = { onCategorySelected("technology") }
        )

        DrawerItem(
            icon = Icons.Default.SportsBasketball,
            label = "Sports",
            isSelected = selectedCategory == "sports",
            onClick = { onCategorySelected("sports") }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Saved articles
        DrawerItem(
            icon = Icons.Default.Bookmark,
            label = "Saved Articles",
            isSelected = false,
            onClick = onSavedArticlesClick
        )

        // Settings
        DrawerItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = false,
            onClick = onSettingsClick
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
