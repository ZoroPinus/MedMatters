package com.example.medmatters.dashboard.ui.reminders

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.medmatters.databinding.FragmentAddRemindersBinding


class AddRemindersFragment : DialogFragment() {

    private var _binding: FragmentAddRemindersBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRemindersBinding.inflate(inflater, container, false)
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}