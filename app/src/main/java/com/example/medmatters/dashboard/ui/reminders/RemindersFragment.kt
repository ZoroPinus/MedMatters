package com.example.medmatters.dashboard.ui.reminders

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medmatters.dashboard.ui.home.ArticleDataModel
import com.example.medmatters.databinding.FragmentRemindersBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RemindersFragment : Fragment() {
    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: RemindersAdapter
    private val reminderList = mutableListOf<ReminderDataModel>()
    private var categorySelected: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchReminders(category = null)
        binding.allButtonSort.setOnClickListener { fetchReminders(category = null) }
        binding.medsButtonSort.setOnClickListener { fetchReminders(category = "Meds") }
        binding.appointmentsButtonSort.setOnClickListener { fetchReminders(category = "Appointments") }
        binding.exerciseButtonSort.setOnClickListener { fetchReminders(category = "Exercise") }
        binding.addRemindersButton.setOnClickListener {
            val addRemindersDialogFragment = AddRemindersFragment()
            val bundle = Bundle()
            bundle.putString("categorySelected", categorySelected)
            addRemindersDialogFragment.arguments = bundle
            addRemindersDialogFragment.show(childFragmentManager, "add_reminders_dialog")
        }

        return binding.root

    }

    fun deleteReminder(reminderId: String) {
        db.collection("reminders")
            .document(reminderId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Reminder deleted successfully")
                Toast.makeText(requireContext(), "Reminder deleted", Toast.LENGTH_SHORT).show()
                // You might want to refresh the reminder list here
                fetchReminders()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting reminder", e)
                Toast.makeText(requireContext(), "Error deleting reminder", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupRecyclerView() {
        adapter = RemindersAdapter(requireContext(), reminderList, this)
        binding.remindersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RemindersFragment.adapter
        }
    }

    private fun fetchReminders(category: String? = null) {
        categorySelected = category
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val query = db.collection("reminders")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("isPinned", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("category", category)
            val queryAll = db.collection("reminders")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("isPinned", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (category != null) {
                query.addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        // Handle error
                        Log.e(TAG, "Failed to fetch reminders", exception)
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch reminders",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }
                    reminderList.clear()
                    for (document in querySnapshot!!) {
                        val reminder = document.toObject(ReminderDataModel::class.java)
                        reminder.reminderId = document.id
                        reminderList.add(reminder)
                    }

                    adapter.notifyDataSetChanged()

                    if (reminderList.isEmpty()) {
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.remindersRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyTextView.visibility = View.GONE
                        binding.remindersRecyclerView.visibility = View.VISIBLE
                    }
                }
            }else{
                queryAll.addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        // Handle error
                        Log.e(TAG, "Failed to fetch reminders", exception)
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch reminders",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }
                    reminderList.clear()
                    for (document in querySnapshot!!) {
                        val reminder = document.toObject(ReminderDataModel::class.java)
                        reminder.reminderId = document.id
                        reminderList.add(reminder)
                    }
                    adapter.notifyDataSetChanged()

                    if (reminderList.isEmpty()) {
                        binding.emptyTextView.visibility = View.VISIBLE
                        binding.remindersRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyTextView.visibility = View.GONE
                        binding.remindersRecyclerView.visibility = View.VISIBLE
                    }
                }
            }

        }else{
            Toast.makeText(
                requireContext(),
                "User not logged in",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}