package com.example.newsexplorer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Ensure this import exists
import com.example.newsexplorer.data.model.Article
import com.example.newsexplorer.data.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch // Ensure this import exists

class ArticleDetailViewModel constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _articleId = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val article: StateFlow<Article?> = _articleId.filterNotNull().flatMapLatest { id ->
        flow { emit(repository.getArticleById(id)) }
            .onStart { _isLoading.value = true }
            .onCompletion { _isLoading.value = false }
            .catch { e ->
                _error.value = "Error loading article: ${e.localizedMessage}"
                // Emit null within catch for flow builder is okay
                emit(null)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null) // Use WhileSubscribed for robustness

    fun loadArticle(articleId: String) {
        if (_articleId.value != articleId) {
            _articleId.value = articleId
            _error.value = null
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch { // This should resolve with correct imports
            repository.toggleBookmark(articleId)
        }
    }
}