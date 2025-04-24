package com.example.newsexplorer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val PREFERRED_CATEGORIES = stringPreferencesKey("preferred_categories")
        val FONT_SIZE = intPreferencesKey("font_size")
        val PREFERRED_SOURCES = stringPreferencesKey("preferred_sources")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    // Theme mode: 0 - system default, 1 - light, 2 - dark
    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: 0
    }

    // Preferred categories as comma-separated string
    val preferredCategories: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[PREFERRED_CATEGORIES]?.split(",") ?: listOf("general", "technology", "business")
    }

    // Font size: 0 - small, 1 - medium, 2 - large
    val fontSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 1
    }

    // Preferred news sources as comma-separated string
    val preferredSources: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[PREFERRED_SOURCES]?.split(",") ?: emptyList()
    }

    // Notifications enabled
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setPreferredCategories(categories: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_CATEGORIES] = categories.joinToString(",")
        }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setPreferredSources(sources: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_SOURCES] = sources.joinToString(",")
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
}