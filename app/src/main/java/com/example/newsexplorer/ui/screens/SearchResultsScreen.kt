package com.example.newsexplorer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.ui.components.ArticleCard
import com.example.newsexplorer.ui.components.SearchBar
import com.example.newsexplorer.viewmodel.*
import androidx.compose.foundation.layout.Arrangement // Import Arrangement
import androidx.compose.runtime.* // Use wildcard
import androidx.compose.ui.text.style.TextAlign // Import TextAlign
import com.example.newsexplorer.viewmodel.SearchViewModel // CORRECT ViewModel Import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    viewModel: SearchViewModel, // CORRECT ViewModel parameter
    onArticleClick: (Article) -> Unit,
    onBackClick: () -> Unit
) {
    // Collect state from the CORRECT ViewModel
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState() // Collect error state
    var searchQuery by remember { mutableStateOf("") }
    // REMOVE unused state collections:
    // val articles by viewModel.articles.collectAsState()
    // val selectedCategory by viewModel.selectedCategory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() } // For errors

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar("Error: $it") }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Add host
        topBar = {
            TopAppBar(
                title = { Text("Search News", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Use AutoMirrored
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors() // Use default colors
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { query ->
                    // Call the correct ViewModel function
                    if (query.isNotBlank()) {
                        viewModel.searchNews(query)
                        // Removed hasSearched logic, handled by viewmodel state
                    } else {
                        viewModel.searchNews("") // Clear search if query is blank
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                autoFocus = true
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                searchResults.isEmpty() && searchQuery.isNotBlank() -> { // Show 'not found' only if query entered
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No articles found for '$searchQuery'",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                searchQuery.isBlank() -> { // Initial state
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Enter search terms to find news.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> { // Results found
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Add spacing
                    ) {
                        items(searchResults, key = { it.id }) { article ->
                            ArticleCard(
                                article = article,
                                onArticleClick = onArticleClick,
                                // Call the correct ViewModel function
                                onToggleBookmark = { viewModel.toggleBookmark(article.id) }
                            )
                        }
                    }
                }
            } // End When
        } // End Column
    } // End Scaffold
}