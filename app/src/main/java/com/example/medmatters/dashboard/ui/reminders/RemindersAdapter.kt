package com.example.medmatters.dashboard.ui.reminders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medmatters.R

class RemindersAdapter(
    private val context: Context,
    private val reminderList: MutableList<ReminderDataModel>
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.reminder_title)
        val description: TextView = itemView.findViewById(R.id.reminder_description)
        val date: TextView = itemView.findViewById(R.id.reminder_date)
        val category: TextView = itemView.findViewById(R.id.category_text)
        val pinStatus: ImageView = itemView.findViewById(R.id.pin_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.reminder_item_layout, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminderList[position]
        holder.title.text = reminder.title
        holder.description.text = reminder.description
        holder.date.text = reminder.date
        holder.category.text = reminder.category
        holder.pinStatus.visibility = if (reminder.isPinned) View.VISIBLE else View.GONE


    }

    override fun getItemCount(): Int = reminderList.size
}
