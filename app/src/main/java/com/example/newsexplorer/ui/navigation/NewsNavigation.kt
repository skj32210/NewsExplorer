package com.example.newsexplorer.ui.navigation // CORRECTED package name

// Import the viewModel() function
// Import all the ViewModel Factories you should have created
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.newsexplorer.SettingsViewModelFactory
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.data.repository.NewsRepository
import com.example.newsexplorer.ui.screens.ArticleDetailScreen
import com.example.newsexplorer.ui.screens.CategoryBrowseScreen
import com.example.newsexplorer.ui.screens.NewsHomeScreen
import com.example.newsexplorer.ui.screens.SavedArticlesScreen
import com.example.newsexplorer.ui.screens.SearchResultsScreen
import com.example.newsexplorer.ui.screens.SettingsScreen
import com.example.newsexplorer.viewmodel.ArticleDetailViewModel
import com.example.newsexplorer.viewmodel.ArticleDetailViewModelFactory
import com.example.newsexplorer.viewmodel.NewsViewModel
import com.example.newsexplorer.viewmodel.NewsViewModelFactory
import com.example.newsexplorer.viewmodel.SavedArticlesViewModel
import com.example.newsexplorer.viewmodel.SavedArticlesViewModelFactory
import com.example.newsexplorer.viewmodel.SearchViewModel
import com.example.newsexplorer.viewmodel.SearchViewModelFactory
import com.example.newsexplorer.viewmodel.SettingsViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object NavDestinations {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val ARTICLE_DETAIL = "article_detail"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
}

// Add repository AND userPreferencesManager parameters
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewsNavigation(
    repository: NewsRepository,
    userPreferencesManager: UserPreferencesManager,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavDestinations.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavDestinations.HOME) {
            val newsViewModel: NewsViewModel = viewModel(factory = NewsViewModelFactory(repository))
            NewsHomeScreen(
                viewModel = newsViewModel,
                onArticleClick = { article ->
                    // --- Encode the URL before navigating ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // -----------------------------------------
                },
                onCategoryClick = { category -> navController.navigate("${NavDestinations.CATEGORY}/$category") },
                onSearchClick = { navController.navigate(NavDestinations.SEARCH) },
                onSettingsClick = { navController.navigate(NavDestinations.SETTINGS) },
                onSavedArticlesClick = { navController.navigate(NavDestinations.SAVED) }
            )
        }

        composable(
            route = "${NavDestinations.CATEGORY}/{categoryId}",
            arguments = listOf(navArgument("categoryId") { /* ... */ })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "general"
            val newsViewModel: NewsViewModel = viewModel(factory = NewsViewModelFactory(repository))
            CategoryBrowseScreen(
                viewModel = newsViewModel,
                categoryId = categoryId,
                onArticleClick = { article ->
                    // --- Encode the URL before navigating ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // -----------------------------------------
                },
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(NavDestinations.SEARCH) }
            )
        }

        composable(
            route = "${NavDestinations.ARTICLE_DETAIL}/{articleId}", // Keep {articleId} placeholder
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Retrieve the encoded URL argument
            val encodedArticleId = backStackEntry.arguments?.getString("articleId") ?: ""
            // Decode if needed for display, but pass encoded ID (which is the URL) to VM
            // val articleId = URLDecoder.decode(encodedArticleId, StandardCharsets.UTF_8.toString())

            val detailViewModel: ArticleDetailViewModel = viewModel(factory = ArticleDetailViewModelFactory(repository))
            ArticleDetailScreen(
                viewModel = detailViewModel,
                // Pass the encoded ID (URL) directly to loadArticle, as that's the primary key now
                articleId = encodedArticleId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SAVED) {
            val savedViewModel: SavedArticlesViewModel = viewModel(factory = SavedArticlesViewModelFactory(repository))
            SavedArticlesScreen(
                viewModel = savedViewModel,
                onArticleClick = { article ->
                    // --- Encode the URL before navigating ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // -----------------------------------------
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SETTINGS) {
            // Settings VM needs its factory, which requires UserPreferencesManager
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(userPreferencesManager))
            SettingsScreen(
                viewModel = settingsViewModel, // Pass the instance
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SEARCH) {
            val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(repository))
            SearchResultsScreen(
                viewModel = searchViewModel,
                onArticleClick = { article ->
                    // --- Encode the URL before navigating ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // -----------------------------------------
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}