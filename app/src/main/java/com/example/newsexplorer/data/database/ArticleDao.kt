package com.example.newsexplorer.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.newsexplorer.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)

    @Update
    suspend fun updateArticle(article: Article)

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): Article?

    @Query("DELETE FROM articles WHERE isBookmarked = 0")
    suspend fun deleteNonBookmarkedArticles()
}