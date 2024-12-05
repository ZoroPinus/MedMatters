package com.example.medmatters.utils

import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.TimeZone
object DateTimeUtils {

    fun getCurrentDateTimeInPhilippines(): Timestamp {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
        return Timestamp(calendar.time)
    }
}