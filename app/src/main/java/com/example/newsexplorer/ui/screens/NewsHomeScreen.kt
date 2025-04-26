package com.example.newsexplorer.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.data.model.Category
import com.example.newsexplorer.ui.components.ArticleCard
import com.example.newsexplorer.ui.components.CategorySelector
import com.example.newsexplorer.ui.components.NavigationDrawerContent
import com.example.newsexplorer.ui.components.SharedNewsHeader
import com.example.newsexplorer.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NewsHomeScreen(
    viewModel: NewsViewModel,
    onArticleClick: (Article) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSavedArticlesClick: () -> Unit,
) {
    val articles by viewModel.articles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    // Calculate window size class
    val windowSizeClass = calculateWindowSizeClass(LocalContext.current as android.app.Activity)
    val widthSizeClass = windowSizeClass.widthSizeClass

    // Define default categories (Could be fetched from Prefs or constant)
    val categories = remember {
        listOf(
            Category("general", "General", 0),
            Category("business", "Business", 0),
            Category("technology", "Technology", 0),
            Category("sports", "Sports", 0),
            Category("entertainment", "Entertainment", 0),
            Category("science", "Science", 0),
            Category("health", "Health", 0)
        )
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar("Error: $it")
        }
    }

    LaunchedEffect(selectedCategory) {
        viewModel.refreshDataForCategory(selectedCategory)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    onCategorySelected = { category ->
                        viewModel.setSelectedCategory(category)
                        scope.launch { drawerState.close() }
                    },
                    onSavedArticlesClick = {
                        onSavedArticlesClick()
                        scope.launch { drawerState.close() }
                    },
                    onSettingsClick = {
                        onSettingsClick()
                        scope.launch { drawerState.close() }
                    },
                    selectedCategory = selectedCategory
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Add Snackbar host
            topBar = {
                SharedNewsHeader(
                    title = "News Explorer",
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CategorySelector(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.setSelectedCategory(category)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (isLoading && articles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (articles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (error != null) "Could not load articles." else "No articles found.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val columns = when (widthSizeClass) {
                        WindowWidthSizeClass.Compact -> 1
                        WindowWidthSizeClass.Medium -> 2
                        else -> 3 // Expanded
                    }

                    if (columns == 1) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxSize(),
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
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
    }
}