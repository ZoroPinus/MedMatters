package com.example.medmatters.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medmatters.dashboard.DashboardActivity
import com.example.medmatters.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
        if (firebaseUser != null) {
            startActivity(
                Intent(
                    this@LoginActivity,
                    DashboardActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        binding.loginButton.setOnClickListener {
            loginWithEmailAndPassword()
        }
        binding.signupButtonNav.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    private fun displayToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }
    private fun loginWithEmailAndPassword() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(
                            Intent(this@LoginActivity, DashboardActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        displayToast("Login successful")
                    } else {
                        // If sign in fails, display a message to the user.
                        displayToast("Login failed: ${task.exception?.message}")
                    }
                }
        } else {
            displayToast("Please enter email and password")
        }
    }

    private fun cacheUserInfo(user: FirebaseUser?) {
        if (user != null) {
            val uid = user.uid
            val email = user.email
            val db = Firebase.firestore
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name")
                    val sharedPrefs = getSharedPreferences("user_info", Context.MODE_PRIVATE)
                    val editor = sharedPrefs.edit()
                    editor.putString("email", email)
                    editor.putString("name", userName)
                    editor.apply()
                }
            }

        }
    }
}