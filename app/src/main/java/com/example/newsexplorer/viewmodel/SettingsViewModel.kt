package com.example.newsexplorer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsexplorer.data.preferences.UserPreferencesManager
// Removed dagger/inject imports
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// import javax.inject.Inject // Removed

// Removed Hilt/Dagger annotations
class SettingsViewModel /* @Inject */ constructor(
    private val preferencesManager: UserPreferencesManager
    // You might need to inject NewsRepository here if you add the clearCache function
    // private val repository: NewsRepository
) : ViewModel() {

    // ... (rest of the ViewModel remains the same) ...

    val themeMode: StateFlow<Int> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val preferredCategories: StateFlow<List<String>> = preferencesManager.preferredCategories
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf("general"))

    val fontSize: StateFlow<Int> = preferencesManager.fontSize
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    val preferredSources: StateFlow<List<String>> = preferencesManager.preferredSources
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notificationsEnabled: StateFlow<Boolean> = preferencesManager.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setPreferredCategories(categories: List<String>) {
        viewModelScope.launch {
            preferencesManager.setPreferredCategories(categories)
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            preferencesManager.setFontSize(size)
        }
    }

    fun setPreferredSources(sources: List<String>) {
        viewModelScope.launch {
            preferencesManager.setPreferredSources(sources)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    // Example: Function to clear cache (requires NewsRepository)
    /*
    fun clearCache() {
        viewModelScope.launch {
            // repository.clearNonBookmarkedArticles() // Uncomment if repository is injected
            Log.d("SettingsViewModel", "Clear cache action triggered (implementation needed)")
        }
    }
    */
}