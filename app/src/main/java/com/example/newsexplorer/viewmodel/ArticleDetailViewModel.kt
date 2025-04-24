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
        // *** CHANGE THIS PART ***
        // Observe the Flow directly from the repository/DAO
        repository.getArticleFlowById(id)
            .onStart {
                // You might want to set loading=true only if the initial value is null
                // This avoids flashing the loading indicator on every minor update
                Log.d("ArticleDetailVM", "Starting article flow observation for ID: $id")
                _isLoading.value = true // Keep for initial load indication
            }
            .onEach { // Use onEach to reset loading state after first emission
                _isLoading.value = false
            }
            .catch { e ->
                Log.e("ArticleDetailVM", "Error in article flow for ID: $id", e)
                _error.value = "Error loading article: ${e.localizedMessage}"
                emit(null) // Emit null in case of an error in the flow itself
            }
        // *** END OF CHANGE ***
    }.stateIn(
        scope = viewModelScope,
        // Use WhileSubscribed to keep observing while UI is visible
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null // Start with null initially
    )


    fun loadArticle(articleId: String) {
        Log.d("ArticleDetailVM", "Loading article with ID: $articleId")
        if (_articleId.value != articleId) {
            _articleId.value = articleId // Trigger the flatMapLatest to switch observation
            _error.value = null
        } else {
            // If ID is the same, manually clear loading if needed, although
            // onStart/onEach in the flow should handle it.
            _isLoading.value = false
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            Log.d("ArticleDetailVM", "Toggling bookmark for ID: $articleId")
            repository.toggleBookmark(articleId)
            // No need to manually refresh `article` flow - Room's Flow triggers automatically
        }
    }
}