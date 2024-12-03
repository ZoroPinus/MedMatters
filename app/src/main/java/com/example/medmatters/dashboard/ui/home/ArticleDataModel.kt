package com.example.medmatters.dashboard.ui.home

data class ArticleDataModel(
    val author: String = "", // Provide default values for all properties
    val articleTitle: String = "",
    val articleDescription: String = "",
    val profileImageUrl: String = "",
    val articleImageUrl: String = "",
    val createdAt: String =""
) {
    // No-argument constructor
    constructor() : this("", "", "", "", "", "")
}