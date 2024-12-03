package com.example.medmatters.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    fun getCurrentDateTimeInPhilippines(): String {
        val currentTime = Date()
        val formatter = SimpleDateFormat("MMMM d, yyyy EEEE h:mm a z", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Manila")
        return formatter.format(currentTime)
    }
}