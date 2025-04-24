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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Removed unused UUID import
// import java.util.UUID

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

    // --- Public Functions ---

    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()
    fun getArticlesByCategory(category: String): Flow<List<Article>> = articleDao.getArticlesByCategory(category)
    fun getBookmarkedArticles(): Flow<List<Article>> = articleDao.getBookmarkedArticles()
    suspend fun getArticleById(articleId: String): Article? { // articleId is URL
        Log.d("NewsRepository", "Getting article by ID '$articleId' from DAO (single)")
        return articleDao.getArticleById(articleId)
    }

    // *** ADD THIS FUNCTION ***
    fun getArticleFlowById(articleId: String): Flow<Article?> { // articleId is URL
        Log.d("NewsRepository", "Getting article FLOW by ID '$articleId' from DAO")
        return articleDao.getArticleFlowById(articleId) // Call the new DAO method
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshNewsForCategory(category: String, country: String = "us"): Result<Unit> {
        return updateLocalNews(category) { // Use helper function
            newsApiService.getTopHeadlines(country, category, apiKey)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun searchNews(query: String, sortBy: String = "publishedAt"): Result<Unit> {
        return updateLocalNews("search") { // Use helper function, assign "search" category
            newsApiService.searchNews(query, sortBy, apiKey)
        }
    }

    // --- Helper for Fetching, Merging, and Inserting ---
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateLocalNews(
        categoryForNewArticles: String,
        fetchAction: suspend () -> com.example.newsexplorer.data.api.NewsResponse // Use correct NewsResponse import
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NewsRepository", "Updating local news for category/search: $categoryForNewArticles")
                val response = fetchAction() // Execute the provided API call

                if (response.status == "ok") {
                    Log.d("NewsRepository", "Fetched ${response.articles.size} articles from API.")

                    if (response.articles.isEmpty()) {
                        Log.d("NewsRepository", "No articles fetched, nothing to insert.")
                        return@withContext Result.success(Unit) // Nothing to do
                    }

                    // Get existing articles from DB to check for bookmarks (using URL as ID)
                    // Fetch only the IDs (URLs) and bookmark status for efficiency
                    val existingArticlesMap = articleDao.getAllArticles() // Fetch all needed to check bookmarks
                        .firstOrNull() // Get the current list once
                        ?.associate { it.id to it.isBookmarked } // Map URL to bookmark status
                        ?: emptyMap()

                    val articlesToInsert = response.articles.mapNotNull { apiArticle ->
                        // Map ApiArticle to domain Article, using URL as ID
                        apiArticle.toArticle(categoryForNewArticles)?.let { mappedArticle ->
                            // Preserve bookmark status if article already exists in DB
                            mappedArticle.copy(isBookmarked = existingArticlesMap[mappedArticle.id] ?: false)
                        }
                    }

                    if (articlesToInsert.isNotEmpty()) {
                        articleDao.insertArticles(articlesToInsert) // Insert/Replace based on URL (ID)
                        Log.d("NewsRepository", "Inserted/Replaced ${articlesToInsert.size} articles.")
                    } else {
                        Log.d("NewsRepository", "No valid articles mapped from API response.")
                    }
                    Result.success(Unit)
                } else {
                    Log.e("NewsRepository", "API error: ${response.status}")
                    Result.failure(Exception("API Error: ${response.status}"))
                }
            } catch (e: Exception) {
                Log.e("NewsRepository", "Network/DB error during update: ${e.message}", e)
                Result.failure(e)
            }
        }
    }


    suspend fun toggleBookmark(articleId: String) { // articleId is now URL
        withContext(Dispatchers.IO) {
            val article = articleDao.getArticleById(articleId) // Find by URL
            article?.let {
                val updatedArticle = it.copy(isBookmarked = !it.isBookmarked)
                articleDao.updateArticle(updatedArticle)
                Log.d("NewsRepository", "Toggled bookmark for article URL: $articleId to ${updatedArticle.isBookmarked}")
            } ?: run {
                Log.w("NewsRepository", "Article not found for bookmark toggle: $articleId")
            }
        }
    }

    suspend fun clearNonBookmarkedArticles() {
        withContext(Dispatchers.IO) {
            Log.d("NewsRepository", "Clearing non-bookmarked articles")
            articleDao.deleteNonBookmarkedArticles()
        }
    }

    // --- Private Helper Functions ---

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ApiArticle.toArticle(category: String): Article? {
        // Validate essential fields - URL is now critical as it's the ID
        if (url.isBlank() || title.isBlank()) {
            Log.w("NewsRepository", "Skipping article with blank URL or Title: '$title', '$url'")
            return null
        }
        val parsedDate: Date = parseApiDate(publishedAt, title)

        return Article(
            id = url, // *** USE URL AS THE PRIMARY KEY ***
            title = title,
            description = description ?: "",
            content = content ?: description ?: "",
            urlToImage = urlToImage,
            url = url,
            publishedAt = parsedDate,
            author = author,
            sourceId = source.id ?: source.name,
            sourceName = source.name, // name is non-nullable
            category = category,
            // isBookmarked is handled by the merge logic in updateLocalNews now
            isBookmarked = false // Default to false during initial mapping
        )
    }

    private fun parseApiDate(dateString: String, articleTitle: String): Date {
        // ... (parsing logic remains the same) ...
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