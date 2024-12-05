package com.example.medmatters.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString("reminderId")

        if (reminderId != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val reminderDocRef = db.collection("reminders").document(reminderId)

                val reminderSnapshot = reminderDocRef.get().await()

                if (reminderSnapshot.exists()) {
                    val reminderTitle = reminderSnapshot.getString("title") ?: ""
                    val reminderDescription = reminderSnapshot.getString("description") ?: ""

                    NotificationUtils.showNotification(applicationContext, reminderTitle, reminderDescription)
                } else {
                    Log.e("ReminderWorker", "Reminder document not found: $reminderId")
                }
            } catch (e: Exception) {
                Log.e("ReminderWorker", "Error fetching reminder details: ${e.message}")
                return Result.failure() // Return failure if fetching fails
            }
        } else {
            Log.e("ReminderWorker", "Reminder ID is null")
            return Result.failure() // Return failure if reminderId is null
        }

        return Result.success()
    }
}