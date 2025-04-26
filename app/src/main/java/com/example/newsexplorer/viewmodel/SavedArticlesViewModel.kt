package com.example.newsexplorer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.data.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedArticlesViewModel constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val savedArticles: StateFlow<List<Article>> = repository.getBookmarkedArticles()
        .catch { e ->
            Log.e("SavedArticlesViewModel", "Error loading saved articles", e)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId)
        }
    }
}