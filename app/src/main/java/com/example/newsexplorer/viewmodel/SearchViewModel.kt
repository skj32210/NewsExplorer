package com.example.newsexplorer.viewmodel

import android.os.Build
import android.util.Log // Import Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Verify import
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.data.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi // Import OptIn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch // Verify import

class SearchViewModel constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow() // Expose if needed

    private val _searchResults = MutableStateFlow<List<Article>>(emptyList())
    val searchResults: StateFlow<List<Article>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Add OptIn for firstOrNull if needed (might depend on coroutine lib version)
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun searchNews(query: String) {
        if (query.isBlank()) {
            _searchQuery.value = "" // Clear stored query too
            _searchResults.value = emptyList()
            return
        }
        _searchQuery.value = query
        viewModelScope.launch { // Verify viewModelScope and launch resolve
            _isLoading.value = true
            _error.value = null
            Log.d("SearchViewModel", "Searching for: $query") // Add log
            val result = repository.searchNews(query)
            if (result.isSuccess) {
                try {
                    // Fetch results from DB after successful API call+insert
                    _searchResults.value = repository.getArticlesByCategory("search").firstOrNull() ?: emptyList()
                    Log.d("SearchViewModel", "Search success, found ${_searchResults.value.size} results from DB") // Add log
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "Error fetching results from DB after search", e) // Log error
                    _searchResults.value = emptyList()
                    _error.value = "Failed to retrieve search results from cache."
                }
            } else {
                Log.e("SearchViewModel", "Search API call failed", result.exceptionOrNull()) // Log error
                _searchResults.value = emptyList()
                _error.value = "Search failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch { // Verify viewModelScope and launch resolve
            repository.toggleBookmark(articleId)
            // Refresh local search results list to show updated bookmark status
            val currentResults = _searchResults.value
            val updatedResults = currentResults.map {
                if (it.id == articleId) it.copy(isBookmarked = !it.isBookmarked) else it
            }
            _searchResults.value = updatedResults
        }
    }
}