package com.example.medmatters.dashboard.ui.reminders

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medmatters.R
import java.text.SimpleDateFormat
import java.util.Locale

class RemindersAdapter(
    private val context: Context,
    private val reminderList: MutableList<ReminderDataModel>,
    private val fragment: RemindersFragment
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.reminder_title)
        val description: TextView = itemView.findViewById(R.id.reminder_description)
        val date: TextView = itemView.findViewById(R.id.reminder_date)
        val category: TextView = itemView.findViewById(R.id.category_text)
        val pinStatus: ImageView = itemView.findViewById(R.id.pin_status)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.reminder_item_layout, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminderList[position]
        val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reminder.createdAt.toDate())
        holder.title.text = reminder.title
        holder.description.text = reminder.description
        holder.date.text = dateString
        holder.category.text = reminder.category
        holder.pinStatus.visibility = if (reminder.getIsPinned()) View.VISIBLE else View.GONE
        holder.deleteButton.setOnClickListener {
            fragment.deleteReminder(reminder.reminderId)
        }
        val backgroundTint = when (reminder.category) {
            "Appointments" -> R.color.appointments
            "Meds" -> R.color.meds
            "Exercise" -> R.color.exercise
            else -> R.color.white
        }
        holder.category.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, backgroundTint))
    }

    override fun getItemCount(): Int = reminderList.size

}