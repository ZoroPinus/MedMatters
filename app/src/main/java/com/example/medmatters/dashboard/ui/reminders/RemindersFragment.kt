package com.example.medmatters.dashboard.ui.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medmatters.databinding.FragmentRemindersBinding
import com.google.firebase.firestore.FirebaseFirestore

class RemindersFragment : Fragment() {
    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: RemindersAdapter
    private val reminderList = mutableListOf<ReminderDataModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        setupRecyclerView()

        fetchReminders()

        binding.addRemindersButton.setOnClickListener {
            val addRemindersDialogFragment = AddRemindersFragment()
            addRemindersDialogFragment.show(childFragmentManager, "add_reminders_dialog")
        }
        return binding.root

    }
    private fun setupRecyclerView() {
        adapter = RemindersAdapter(requireContext(), reminderList)
        binding.remindersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RemindersFragment.adapter
        }
    }

    private fun fetchReminders() {
        db.collection("reminders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                reminderList.clear()
                for (document in querySnapshot) {
                    val reminder = document.toObject(ReminderDataModel::class.java)
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
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch reminders", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}