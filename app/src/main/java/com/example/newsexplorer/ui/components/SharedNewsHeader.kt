package com.example.newsexplorer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedNewsHeader(
    title: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
