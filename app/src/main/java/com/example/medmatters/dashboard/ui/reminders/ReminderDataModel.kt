package com.example.medmatters.dashboard.ui.reminders

import com.google.firebase.Timestamp

data class ReminderDataModel(
    var reminderId: String = "",
    val userId: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Timestamp ,
    val isPinned: Boolean,
    val data: Map<String, Any>? = null
){
    constructor() : this("","", "", "", "", Timestamp.now(), false, null)

    fun getIsPinned(): Boolean {
        return data?.get("isPinned") as? Boolean ?: isPinned // Default to false if not found
    }
}