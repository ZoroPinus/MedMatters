package com.example.medmatters.dashboard.ui.home

import com.google.firebase.Timestamp

data class ArticleDataModel(
    var id: String = "",
    val author: String = "",
    val articleTitle: String = "",
    val articleDescription: String = "",
    val profileImageUrl: String = "",
    val articleImageUrl: String = "",
    val createdAt: Timestamp? = null
){
    constructor() : this("", "", "", "", "", "", null)
}