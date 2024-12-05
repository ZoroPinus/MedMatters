package com.example.medmatters.dashboard.ui.home

data class ArticleDataModel(
    var id: String = "",
    val author: String = "",
    val articleTitle: String = "",
    val articleDescription: String = "",
    val profileImageUrl: String = "",
    val articleImageUrl: String = "",
    val createdAt: String =""
)