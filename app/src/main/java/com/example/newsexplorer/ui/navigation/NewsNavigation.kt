package com.example.newsexplorer.ui.navigation

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
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.data.repository.NewsRepository
import com.example.newsexplorer.ui.screens.*
import com.example.newsexplorer.viewmodel.*
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
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
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
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                },
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(NavDestinations.SEARCH) }
            )
        }

        composable(
            route = "${NavDestinations.ARTICLE_DETAIL}/{articleUrl}",
            arguments = listOf(navArgument("articleUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedArticleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""

            val detailViewModel: ArticleDetailViewModel = viewModel(factory = ArticleDetailViewModelFactory(repository))
            ArticleDetailScreen(
                viewModel = detailViewModel,
                articleId = encodedArticleUrl,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SAVED) {
            val savedViewModel: SavedArticlesViewModel = viewModel(factory = SavedArticlesViewModelFactory(repository))
            SavedArticlesScreen(
                viewModel = savedViewModel,
                onArticleClick = { article ->
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(userPreferencesManager, repository)
            )
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
                    val encodedUrl = URLEncoder.encode(article.id, StandardCharsets.UTF_8.toString())
                    navController.navigate("${NavDestinations.ARTICLE_DETAIL}/$encodedUrl")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}