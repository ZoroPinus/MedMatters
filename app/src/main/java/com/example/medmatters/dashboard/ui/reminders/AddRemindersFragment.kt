package com.example.medmatters.dashboard.ui.reminders

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.ColorStateList
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.medmatters.R
import com.example.medmatters.databinding.FragmentAddRemindersBinding
import com.example.medmatters.utils.DateTimeUtils
import com.example.medmatters.utils.ReminderWorker
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.concurrent.TimeUnit


class AddRemindersFragment : DialogFragment() {

    private var _binding: FragmentAddRemindersBinding? = null
    private val binding get() = _binding!!
    private var isPinned = false
    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null
    data class Category(val id: Int, val name: String)
    inner class CategorySpinnerAdapter(context: Context, categories: List<Category>) : ArrayAdapter<Category>(context, 0, categories) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.category_text_layout, parent, false)

            val category = getItem(position)
            val textView = view.findViewById<TextView>(R.id.category_text)
            textView.text = category?.name

            // Set background tint based on category
            when (category?.name) {
                "Meds" -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.meds))
                "Appointments" -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.appointments))
                "Exercise" -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.exercise))
                // Add more cases for other categories
                else -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            }
            binding.pinStatus.setOnClickListener {
                isPinned = !isPinned
                updatePinImage()
            }

            binding.datePickerButton.setOnClickListener {
                val currentCalendar = Calendar.getInstance()
                val year = currentCalendar.get(Calendar.YEAR)
                val month = currentCalendar.get(Calendar.MONTH)
                val day = currentCalendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    requireContext(), // or 'this' if in an Activity
                    { _, year, month, dayOfMonth ->
                        selectedDate = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }
                        // Update the button text or display the selected date
                        binding.datePickerButton.text = "${month + 1}/${dayOfMonth}/${year}"
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }

            binding.timePickerButton.setOnClickListener {
                val currentCalendar = Calendar.getInstance()
                val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
                val minute = currentCalendar.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    requireContext(), // or 'this' if in an Activity
                    { _, hourOfDay, minute ->
                        selectedTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }
                        // Update the button text or display the selected time
                        binding.timePickerButton.text = String.format("%02d:%02d", hourOfDay, minute)
                    },
                    hour,
                    minute,
                    true // is24HourView
                )
                timePickerDialog.show()
            }
            return view
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRemindersBinding.inflate(inflater, container, false)
        val categorySelected = arguments?.getString("categorySelected")
        val categories = when (categorySelected) {
            "Meds" -> listOf(Category(1, "Meds"), Category(2, "Appointments"), Category(3, "Exercise"))
            "Appointments" -> listOf(Category(2, "Appointments"), Category(1, "Meds"), Category(3, "Exercise"))
            "Exercise" -> listOf(Category(3, "Exercise"), Category(1, "Meds"), Category(2, "Appointments"))
            else -> listOf(Category(1, "Meds"), Category(2, "Appointments"), Category(3, "Exercise")) // Default order
        }
        val adapter = CategorySpinnerAdapter(requireContext(), categories)
        binding.categoryDropdown.adapter = adapter
        binding.categoryDropdown.setSelection(0)

        val reminderTitleTextView: TextView = binding.reminderTitle
        reminderTitleTextView.text = when (categorySelected) {
            "Meds" -> "Name of Meds"
            "Appointments" -> "Name of Appointments"
            "Exercise" -> "Name of Exercise"
            else -> "Name of Meds" // Default text
        }

        binding.categoryDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as Category
                val categoryName = selectedCategory.name

                // Update the TextView text based on the selected category
                reminderTitleTextView.text = when (categoryName) {
                    "Meds" -> "Name of Meds"
                    "Appointments" -> "Name of Appointments"
                    "Exercise" -> "Name of Exercise"
                    else -> "Name of Meds" // Default text
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.saveButton.setOnClickListener {
            addReminder()
        }
        return binding.root
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.requestFeature(FEATURE_NO_TITLE)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun updatePinImage() {
        if (isPinned) {
            binding.pinStatus.setImageResource(R.drawable.ic_pin) // Set image for pinned state
        } else {
            binding.pinStatus.setImageResource(R.drawable.ic_pin_off) // Set image for unpinned state
        }
    }
    private fun addReminder() {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val selectedCategory = binding.categoryDropdown.selectedItem as Category
        val currentTime = DateTimeUtils.getCurrentDateTimeInPhilippines()


        val reminderDateTime = Calendar.getInstance().apply {
            if (selectedDate != null) {
                set(Calendar.YEAR, selectedDate!!.get(Calendar.YEAR))
                set(Calendar.MONTH, selectedDate!!.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, selectedDate!!.get(Calendar.DAY_OF_MONTH))
            }
            if (selectedTime != null) {
                set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0) // Set seconds to 0
                set(Calendar.MILLISECOND, 0) // Set milliseconds to 0
            }
        }.timeInMillis

        if (currentUser != null) {
            val reminderData = hashMapOf(
                "title" to binding.titleInput.text.toString(),
                "description" to binding.descriptionInput.text.toString(),
                "category" to selectedCategory.name,
                "userId" to currentUser.uid,
                "isPinned" to isPinned,
                "createdAt" to currentTime,
                "reminderDateTime" to reminderDateTime
            )

            db.collection("reminders")
                .add(reminderData)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Reminder added with ID: ${documentReference.id}")

                    scheduleNotification(documentReference.id, reminderDateTime)

                    Toast.makeText(requireContext(), "Reminder added successfully", Toast.LENGTH_SHORT).show()
                    // You might want to close the dialog or navigate back here
                    dismiss() // Close the dialog
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding reminder", e)
                    Toast.makeText(requireContext(), "Error adding reminder", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User not logged in, handle accordingly
            Log.w(TAG, "User not logged in")
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
    private fun scheduleNotification(reminderId: String, reminderDateTime: Long) {
        val triggerTime = reminderDateTime

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workDataOf("reminderId" to reminderId))
            .setInitialDelay(triggerTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            reminderId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}