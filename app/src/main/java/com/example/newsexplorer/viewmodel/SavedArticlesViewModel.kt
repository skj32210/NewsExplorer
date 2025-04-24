package com.example.newsexplorer.viewmodel

import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Verify import
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.data.repository.NewsRepository
import kotlinx.coroutines.flow.* // Use wildcard
import kotlinx.coroutines.launch // Verify import

class SavedArticlesViewModel constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val savedArticles: StateFlow<List<Article>> = repository.getBookmarkedArticles()
        .catch { e ->
            // Log the error, but don't emit here. stateIn will use initialValue.
            Log.e("SavedArticlesViewModel", "Error loading saved articles", e)
            // Optionally update a separate error StateFlow if needed for UI
            // _error.value = "Could not load bookmarks"
            // IMPORTANT: Remove emit call from here
            // emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList() // Default value if flow fails or hasn't emitted
        )

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch { // Verify viewModelScope and launch resolve
            repository.toggleBookmark(articleId)
        }
    }
}