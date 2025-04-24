package com.example.newsexplorer.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("category") category: String?,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("apiKey") apiKey: String
    ): NewsResponse
}