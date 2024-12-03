package com.example.medmatters.dashboard.ui.reminders

data class ReminderDataModel(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val isPinned: Boolean = false
){
    constructor() : this("", "", "", "", "", false)
}