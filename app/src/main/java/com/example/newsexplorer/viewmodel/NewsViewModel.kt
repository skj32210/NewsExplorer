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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("general")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val articles: StateFlow<List<Article>> = selectedCategory.flatMapLatest { category ->
        repository.getArticlesByCategory(category)
            .catch { e ->
                Log.e("NewsViewModel", "Error fetching articles for $category", e)
                _error.value = "Failed to load articles: ${e.localizedMessage}"
                emit(emptyList())
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setSelectedCategory(category: String) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            refreshDataForCategory(category)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshDataForCategory(category: String = _selectedCategory.value) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("NewsViewModel", "Refreshing data for category: $category")
            val result = repository.refreshNewsForCategory(category)
            if (result.isFailure) {
                Log.e("NewsViewModel", "Error refreshing category $category", result.exceptionOrNull())
                _error.value = "Failed to refresh news: ${result.exceptionOrNull()?.localizedMessage}"
            }
            _isLoading.value = false
            Log.d("NewsViewModel", "Finished refreshing data for category: $category")
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            Log.d("NewsViewModel", "Toggling bookmark for: $articleId")
            repository.toggleBookmark(articleId)
        }
    }
}