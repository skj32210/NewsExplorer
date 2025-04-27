package com.example.newsexplorer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.data.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: UserPreferencesManager,
    private val repository: NewsRepository
) : ViewModel() {


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

    fun clearCache() {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Attempting to clear non-bookmarked articles...")
                repository.clearNonBookmarkedArticles()
                Log.d("SettingsViewModel", "Cache cleared successfully.")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error clearing cache", e)
            }
        }
    }
}