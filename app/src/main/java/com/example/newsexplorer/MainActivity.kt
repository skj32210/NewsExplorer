package com.example.newsexplorer

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.ui.navigation.NewsNavigation
import com.example.newsexplorer.ui.theme.FontSize
import com.example.newsexplorer.ui.theme.NewsExplorerTheme
import com.example.newsexplorer.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as NewsExplorerApplication
        val newsRepository = app.newsRepository
        val userPreferencesManager = app.userPreferencesManager

        setContent {
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

            val windowSizeClass = calculateWindowSizeClass(this)

            NewsExplorerTheme(darkTheme = useDarkTheme, fontSize = appFontSize) {
                Surface {
                    NewsNavigation(
                        repository = newsRepository,
                        userPreferencesManager = userPreferencesManager
                    )
                }
            }
        }
    }
}

class SettingsViewModelFactory(private val prefsManager: UserPreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(prefsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}