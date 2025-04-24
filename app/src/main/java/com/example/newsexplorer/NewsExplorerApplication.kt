package com.example.newsexplorer // Match your namespace

import android.app.Application
import androidx.room.Room
import com.example.newsexplorer.data.api.NewsApiService
import com.example.newsexplorer.data.database.NewsDatabase
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.data.repository.NewsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Custom Application class to hold app-scoped singletons
class NewsExplorerApplication : Application() {

    // Use lazy delegate for singleton creation
    // These will be created only when first accessed
    val database: NewsDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            NewsDatabase::class.java, "news-database"
        )
            .fallbackToDestructiveMigration() // Use proper migration in real app
            // Consider adding .setQueryExecutor / .setTransactionExecutor if needed for background threads
            .build()
        // Note: .build() itself *can* still do some main thread disk I/O on first creation.
        // For extreme optimization, consider manual threading or Dagger/Hilt's @Singleton patterns.
    }

    val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/") // Base URL for NewsAPI
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    val newsRepository: NewsRepository by lazy {
        NewsRepository(newsApiService, database.articleDao())
    }

    val userPreferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        // Optional: Initialize things here if needed, but lazy often suffices
    }
}