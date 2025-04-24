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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.ui.components.ArticleCard
import com.example.newsexplorer.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBrowseScreen(
    viewModel: NewsViewModel, // CORRECT ViewModel parameter
    categoryId: String,
    onArticleClick: (Article) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    // Set category when entering/changing
    LaunchedEffect(categoryId) {
        viewModel.setSelectedCategory(categoryId)
        // DO NOT collect state here
    }

    // Collect state at the top level
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar("Error: $it") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, // Capitalize category name
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) { // Add search icon button
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading && articles.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            articles.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (error != null) "Could not load articles." else "No articles found for this category.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(articles, key = { it.id }) { article ->
                        ArticleCard(
                            article = article,
                            onArticleClick = onArticleClick,
                            onToggleBookmark = { viewModel.toggleBookmark(article.id) }
                        )
                    }
                }
            }
        }
    }
}