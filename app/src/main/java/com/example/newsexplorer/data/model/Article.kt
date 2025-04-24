package com.example.newsexplorer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val urlToImage: String?,
    val url: String,
    val publishedAt: Date,
    val author: String?,
    val sourceId: String,
    val sourceName: String,
    val category: String,
    val isBookmarked: Boolean = false
)