package com.example.newsexplorer

import android.app.Application
import androidx.room.Room
import com.example.newsexplorer.data.api.NewsApiService
import com.example.newsexplorer.data.database.NewsDatabase
import com.example.newsexplorer.data.preferences.UserPreferencesManager
import com.example.newsexplorer.data.repository.NewsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsExplorerApplication : Application() {

    val database: NewsDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            NewsDatabase::class.java, "news-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val newsApiService: NewsApiService by lazy {
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
    }
}