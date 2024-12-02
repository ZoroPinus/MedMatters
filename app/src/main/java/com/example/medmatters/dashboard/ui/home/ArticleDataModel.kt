package com.example.medmatters.dashboard.ui.home

data class ArticleDataModel(
    val author: String,
    val articleTitle: String,
    val articleDescription: String,
    val profileImageUrl: String,
    val articleImageUrl: String,
    val createdAt: Long = 0L

)