package com.example.medmatters.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    fun getCurrentDateTimeInPhilippines(): Timestamp {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
        return Timestamp(calendar.time)
    }
    fun timestampToReadableString(timestamp: Timestamp): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a",
            Locale.getDefault())
        return dateFormat.format(timestamp.toDate())
    }
}