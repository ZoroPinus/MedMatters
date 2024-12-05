package com.example.medmatters.dashboard.ui.reminders

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.medmatters.R
import com.example.medmatters.databinding.FragmentAddRemindersBinding
import com.example.medmatters.utils.DateTimeUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class AddRemindersFragment : DialogFragment() {

    private var _binding: FragmentAddRemindersBinding? = null
    private val binding get() = _binding!!
    private var isPinned = false
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
            when (category?.id) {
                1 -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.appointments))
                2 -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.meds))
                3 -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.exercise))
                // Add more cases for other categories
                else -> view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            }
            binding.pinStatus.setOnClickListener {
                isPinned = !isPinned
                updatePinImage()
            }
            return view
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRemindersBinding.inflate(inflater, container, false)
        val categories = listOf(
            Category(1, "Appointments"),
            Category(2, "Meds"),
            Category(3, "Exercise"),
        )
        val adapter = CategorySpinnerAdapter(requireContext(), categories)
        binding.categoryDropdown.adapter = adapter
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
        if (currentUser != null) {
            val reminderData = hashMapOf(
                "title" to binding.titleInput.text.toString(),
                "description" to binding.descriptionInput.text.toString(),
                "category" to selectedCategory.name,
                "userId" to currentUser.uid,
                "isPinned" to isPinned,
                "createdAt" to currentTime
            )

            db.collection("reminders")
                .add(reminderData)
                .addOnSuccessListener { documentReference ->
                    // Reminder added successfully
                    Log.d(TAG, "Reminder added with ID: ${documentReference.id}")
                    Toast.makeText(requireContext(), "Reminder added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Log.w(TAG, "Error adding reminder", e)
                    Toast.makeText(requireContext(), "Error adding reminder", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User not logged in, handle accordingly
            Log.w(TAG, "User not logged in")
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}