package com.example.newsexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.ui.components.ArticleCard
import com.example.newsexplorer.viewmodel.SavedArticlesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedArticlesScreen(
    viewModel: SavedArticlesViewModel, // CORRECT ViewModel parameter
    onArticleClick: (Article) -> Unit,
    onBackClick: () -> Unit
) {
    // Collect state from the CORRECT ViewModel
    val savedArticles by viewModel.savedArticles.collectAsState()
    // REMOVE unused state collections:
    // val articles by viewModel.articles.collectAsState()
    // val selectedCategory by viewModel.selectedCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Articles", style = MaterialTheme.typography.titleLarge) },
                // Add navigation icon for consistency
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Use AutoMirrored
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors() // Optional: Use default colors
            )
        }
    ) { paddingValues ->
        if (savedArticles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    "No articles saved yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(savedArticles, key = { it.id }) { article ->
                    ArticleCard(
                        article = article,
                        onArticleClick = onArticleClick,
                        onToggleBookmark = { viewModel.toggleBookmark(article.id) } // Correct ViewModel action
                    )
                }
            }
        }
    }
}
