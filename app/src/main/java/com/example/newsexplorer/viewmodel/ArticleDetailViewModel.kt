package com.example.newsexplorer.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        repository.getArticleFlowById(id)
            .onStart {
                Log.d("ArticleDetailVM", "Starting article flow observation for ID: $id")
                _isLoading.value = true
            }
            .onEach {
                _isLoading.value = false
            }
            .catch { e ->
                Log.e("ArticleDetailVM", "Error in article flow for ID: $id", e)
                _error.value = "Error loading article: ${e.localizedMessage}"
                emit(null)
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )


    fun loadArticle(articleId: String) {
        Log.d("ArticleDetailVM", "Loading article with ID: $articleId")
        if (_articleId.value != articleId) {
            _articleId.value = articleId
            _error.value = null
        } else {
            _isLoading.value = false
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            Log.d("ArticleDetailVM", "Toggling bookmark for ID: $articleId")
            repository.toggleBookmark(articleId)
        }
    }
}