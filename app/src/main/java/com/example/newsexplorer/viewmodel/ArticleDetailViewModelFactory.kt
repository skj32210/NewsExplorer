package com.example.newsexplorer.viewmodel // Or your DI package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.newsexplorer.data.repository.NewsRepository

class ArticleDetailViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}