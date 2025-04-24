// **** CORRECTED PACKAGE NAME ****
package com.example.newsexplorer.ui.navigation

// **** Import necessary classes for URL encoding ****
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
    const val ARTICLE_DETAIL = "article_detail" // The argument name is defined here implicitly
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
}

// @RequiresApi is likely needed IF ViewModels call repo methods needing it AND minSdk < O
// Let's keep it for now as repo/VMs use O features
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewsNavigation(
    repository: NewsRepository, // Accept repository instance
    userPreferencesManager: UserPreferencesManager, // Accept prefs manager instance
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
                    // --- Encode the URL (which is article.id) ---
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
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "general"
            val newsViewModel: NewsViewModel = viewModel(factory = NewsViewModelFactory(repository))
            CategoryBrowseScreen(
                viewModel = newsViewModel,
                categoryId = categoryId,
                onArticleClick = { article ->
                    // --- Encode the URL ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // ---------------------
                },
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(NavDestinations.SEARCH) }
            )
        }

        composable(
            // The argument name in the route definition MUST match the key used to retrieve it
            route = "${NavDestinations.ARTICLE_DETAIL}/{articleUrl}", // Use a descriptive name like articleUrl
            arguments = listOf(navArgument("articleUrl") { type = NavType.StringType }) // Match argument name
        ) { backStackEntry ->
            // Retrieve the encoded URL using the correct argument name
            val encodedArticleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            // Decoding is generally NOT needed here if the ViewModel uses the encoded URL as the ID
            // val articleUrl = URLDecoder.decode(encodedArticleUrl, StandardCharsets.UTF_8.toString())

            val detailViewModel: ArticleDetailViewModel = viewModel(factory = ArticleDetailViewModelFactory(repository))
            ArticleDetailScreen(
                viewModel = detailViewModel,
                // Pass the encoded URL (which is the ID) to the screen/ViewModel
                articleId = encodedArticleUrl,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SAVED) {
            val savedViewModel: SavedArticlesViewModel = viewModel(factory = SavedArticlesViewModelFactory(repository))
            SavedArticlesScreen(
                viewModel = savedViewModel,
                onArticleClick = { article ->
                    // --- Encode the URL ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // ---------------------
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(userPreferencesManager))
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SEARCH) {
            val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(repository))
            SearchResultsScreen(
                viewModel = searchViewModel,
                onArticleClick = { article ->
                    // --- Encode the URL ---
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                    // ---------------------
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}