package com.example.newsexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.newsexplorer.data.api.NewsApiService
import com.example.newsexplorer.data.database.NewsDatabase
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.data.repository.NewsRepository
import com.example.newsexplorer.ui.navigation.NewsNavigation
import com.example.newsexplorer.ui.theme.FontSize
import com.example.newsexplorer.ui.theme.NewsExplorerTheme
import com.example.newsexplorer.viewmodel.SettingsViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    // --- Instantiate Dependencies ---
    private val newsDatabase: NewsDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            NewsDatabase::class.java, "news-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/") // Base URL for NewsAPI
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    private val newsRepository: NewsRepository by lazy { // Explicit type
        NewsRepository(newsApiService, newsDatabase.articleDao())
    }

    private val userPreferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(applicationContext)
    }
    // --- End Dependency Instantiation ---


    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Settings VM is used for theme/font, created here
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(userPreferencesManager)
            )

            val themeMode by settingsViewModel.themeMode.collectAsState()
            val fontSizeSetting by settingsViewModel.fontSize.collectAsState()

            val useDarkTheme = when (themeMode) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System default
            }
            val appFontSize = when (fontSizeSetting) {
                0 -> FontSize.Small
                2 -> FontSize.Large
                else -> FontSize.Medium
            }

            // windowSizeClass is calculated but not explicitly used below (ok for now)
            val windowSizeClass = calculateWindowSizeClass(this)

            NewsExplorerTheme(darkTheme = useDarkTheme, fontSize = appFontSize) {
                Surface {
                    // Pass BOTH repository and prefs manager needed by factories within Nav graph
                    NewsNavigation(
                        repository = newsRepository,
                        userPreferencesManager = userPreferencesManager
                    )
                }
            }
        }
    }
}

// Settings Factory remains the same
class SettingsViewModelFactory(private val prefsManager: UserPreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(prefsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}