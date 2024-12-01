package com.example.medmatters.auth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medmatters.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import java.util.Date

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()


        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.signupButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val name = binding.nameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confPassword = binding.confirmPasswordInput.text.toString().trim()
            val created_at = FieldValue.serverTimestamp()
            if (email.isNotEmpty() && password.isNotEmpty() && confPassword.isNotEmpty() && name.isNotEmpty() ) {
                if(password !== confPassword){
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignUpActivity", "createUserWithEmail:success")
                            val user = auth.currentUser

                            // Create a new user document in Firestore
                            val userMap = hashMapOf(
                                "email" to email,
                                "name" to name,
                                "created_at" to created_at
                            )

                            db.collection("users").document(user!!.uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Log.d("SignUpActivity", "DocumentSnapshot added with ID: ${user.uid}")
                                    Toast.makeText(baseContext, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                    finish() // Close the SignUpActivity
                                }
                                .addOnFailureListener { e ->
                                    Log.w("SignUpActivity", "Error adding document", e)
                                    Toast.makeText(baseContext, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignUpActivity", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(baseContext, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}