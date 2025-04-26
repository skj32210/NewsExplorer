package com.example.newsexplorer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.newsexplorer.data.model.Category

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategory,
                onCategorySelected = { onCategorySelected(category.id) }
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onCategorySelected: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .clickable(onClick = onCategorySelected)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category.id),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getCategoryIcon(categoryId: String): ImageVector {
    return when (categoryId.lowercase()) {
        "general" -> Icons.Filled.Public
        "business" -> Icons.Filled.Business
        "technology" -> Icons.Filled.Memory
        "sports" -> Icons.Filled.SportsBasketball
        "entertainment" -> Icons.Filled.LocalMovies
        "science" -> Icons.Filled.Science
        "health" -> Icons.Filled.HealthAndSafety
        else -> Icons.Filled.Public
    }
}