package com.example.newsexplorer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsexplorer.data.model.Article

@Database(entities = [Article::class], version = 1)
@TypeConverters(Converters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
