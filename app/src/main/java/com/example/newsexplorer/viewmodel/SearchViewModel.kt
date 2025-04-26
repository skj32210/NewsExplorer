package com.example.newsexplorer.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.data.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SearchViewModel constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Article>>(emptyList())
    val searchResults: StateFlow<List<Article>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun searchNews(query: String) {
        if (query.isBlank()) {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
            return
        }
        _searchQuery.value = query
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("SearchViewModel", "Searching for: $query")
            val result = repository.searchNews(query)
            if (result.isSuccess) {
                try {
                    _searchResults.value = repository.getArticlesByCategory("search").firstOrNull() ?: emptyList()
                    Log.d("SearchViewModel", "Search success, found ${_searchResults.value.size} results from DB")
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "Error fetching results from DB after search", e)
                    _searchResults.value = emptyList()
                    _error.value = "Failed to retrieve search results from cache."
                }
            } else {
                Log.e("SearchViewModel", "Search API call failed", result.exceptionOrNull())
                _searchResults.value = emptyList()
                _error.value = "Search failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId)
            val currentResults = _searchResults.value
            val updatedResults = currentResults.map {
                if (it.id == articleId) it.copy(isBookmarked = !it.isBookmarked) else it
            }
            _searchResults.value = updatedResults
        }
    }
}