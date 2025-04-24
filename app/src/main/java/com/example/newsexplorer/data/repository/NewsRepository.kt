package com.example.newsexplorer.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.newsexplorer.BuildConfig
import com.example.newsexplorer.data.api.ApiArticle
import com.example.newsexplorer.data.api.NewsApiService
import com.example.newsexplorer.data.database.ArticleDao
import com.example.newsexplorer.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NewsRepository(
    private val newsApiService: NewsApiService,
    private val articleDao: ArticleDao
) {
    private val apiKey = BuildConfig.NEWS_API_KEY
    private val apiDateFormatter: DateTimeFormatter? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
    } else {
        null
    }

    private val fallbackDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // --- Public Functions Needed by ViewModels ---

    fun getAllArticles(): Flow<List<Article>> {
        Log.d("NewsRepository", "Getting all articles from DAO")
        return articleDao.getAllArticles()
    }

    fun getArticlesByCategory(category: String): Flow<List<Article>> {
        Log.d("NewsRepository", "Getting articles by category '$category' from DAO")
        return articleDao.getArticlesByCategory(category)
    }

    fun getBookmarkedArticles(): Flow<List<Article>> {
        Log.d("NewsRepository", "Getting bookmarked articles from DAO")
        return articleDao.getBookmarkedArticles()
    }

    suspend fun getArticleById(articleId: String): Article? {
        Log.d("NewsRepository", "Getting article by ID '$articleId' from DAO")
        return articleDao.getArticleById(articleId)
    }

    @RequiresApi(Build.VERSION_CODES.O) // Still needed because toArticle uses OffsetDateTime
    suspend fun refreshNewsForCategory(category: String, country: String = "us"): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NewsRepository", "Refreshing news for category: $category")
                val response = newsApiService.getTopHeadlines(country, category, apiKey)
                if (response.status == "ok") {
                    Log.d("NewsRepository", "Fetched ${response.articles.size} articles for $category")
                    val articles = response.articles.mapNotNull { apiArticle ->
                        apiArticle.toArticle(category) // Map API articles
                    }
                    if (articles.isNotEmpty()) {
                        articleDao.insertArticles(articles)
                        Log.d("NewsRepository", "Inserted ${articles.size} articles for $category")
                    } else {
                        Log.d("NewsRepository", "No valid articles to insert for $category")
                    }
                    Result.success(Unit)
                } else {
                    Log.e("NewsRepository", "API error for $category: ${response.status}")
                    Result.failure(Exception("API Error: ${response.status}"))
                }
            } catch (e: Exception) {
                Log.e("NewsRepository", "Network error fetching $category: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O) // Still needed because toArticle uses OffsetDateTime
    suspend fun searchNews(query: String, sortBy: String = "publishedAt"): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NewsRepository", "Searching news for query: $query")
                val response = newsApiService.searchNews(query, sortBy, apiKey)
                if (response.status == "ok") {
                    Log.d("NewsRepository", "Found ${response.articles.size} articles for query '$query'")
                    val articles = response.articles.mapNotNull { apiArticle ->
                        apiArticle.toArticle("search") // Map API articles
                    }
                    if (articles.isNotEmpty()) {
                        articleDao.insertArticles(articles)
                        Log.d("NewsRepository", "Inserted ${articles.size} search results")
                    } else {
                        Log.d("NewsRepository", "No valid search results to insert for query '$query'")
                    }
                    Result.success(Unit)
                } else {
                    Log.e("NewsRepository", "API error searching '$query': ${response.status}")
                    Result.failure(Exception("API Error: ${response.status}"))
                }
            } catch (e: Exception) {
                Log.e("NewsRepository", "Network error searching '$query': ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun toggleBookmark(articleId: String) {
        withContext(Dispatchers.IO) { // Ensure DB operations are off main thread
            val article = articleDao.getArticleById(articleId)
            article?.let {
                val updatedArticle = it.copy(isBookmarked = !it.isBookmarked)
                articleDao.updateArticle(updatedArticle)
                Log.d("NewsRepository", "Toggled bookmark for article ID: $articleId to ${updatedArticle.isBookmarked}")
            } ?: run {
                Log.w("NewsRepository", "Article not found for bookmark toggle: $articleId")
            }
        }
    }

    suspend fun clearNonBookmarkedArticles() {
        withContext(Dispatchers.IO) { // Ensure DB operations are off main thread
            Log.d("NewsRepository", "Clearing non-bookmarked articles")
            articleDao.deleteNonBookmarkedArticles()
        }
    }

    // --- Private Helper Functions ---

    @RequiresApi(Build.VERSION_CODES.O) // Still needed here
    private fun ApiArticle.toArticle(category: String): Article? {
        if (title.isBlank() || url.isBlank()) {
            Log.w("NewsRepository", "Skipping article with blank title or URL: '$title', '$url'")
            return null
        }
        val parsedDate: Date = parseApiDate(publishedAt, title)

        return Article(
            id = UUID.randomUUID().toString(), // Consider using URL or a hash as ID if possible
            title = title,
            description = description ?: "",
            content = content ?: description ?: "",
            urlToImage = urlToImage,
            url = url,
            publishedAt = parsedDate,
            author = author,
            sourceId = source.id ?: source.name, // Use name as fallback
            sourceName = source.name, // Name is non-nullable in definition
            category = category,
            isBookmarked = false // Default bookmark status on fetch
        )
    }

    private fun parseApiDate(dateString: String, articleTitle: String): Date {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && apiDateFormatter != null) {
            try {
                return OffsetDateTime.parse(dateString, apiDateFormatter)
                    .toInstant()
                    .let { Date.from(it) }
            } catch (e: DateTimeParseException) {
                Log.w("NewsRepository", "Modern date parse failed for '$dateString' in '$articleTitle': ${e.message}. Trying fallback.")
            } catch (e: Exception) {
                Log.e("NewsRepository", "Unexpected error during modern date parse for '$dateString' in '$articleTitle'", e)
            }
        }
        try {
            synchronized(fallbackDateFormatter) {
                return fallbackDateFormatter.parse(dateString) ?: Date()
            }
        } catch (e: Exception) {
            Log.e("NewsRepository", "Fallback date parse failed for '$dateString' in '$articleTitle'. Using current time.", e)
            return Date()
        }
    }
}