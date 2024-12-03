package com.example.medmatters.dashboard.ui.home

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.medmatters.databinding.ActivityAddArticleBinding
import com.example.medmatters.utils.DateTimeUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class AddArticleActivity : AppCompatActivity() {
    companion object {
        private const val YOUR_REQUEST_CODE = 123
    }
    private lateinit var binding: ActivityAddArticleBinding
    private var selectedImageUri: Uri? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    selectedImageUri = data.data
                    binding.imageInput.setImageURI(selectedImageUri)
                }
            }
        }
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    selectedImageUri = getImageUriFromBitmap(imageBitmap) // Get Uri from Bitmap
                    binding.imageInput.setImageBitmap(imageBitmap)
                }
            }
        }
    // Helper function to get Uri from Bitmap
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        binding.submitButton.setOnClickListener {
            createArticle()
        }
        binding.backButton.setOnClickListener {
            finish()
        }
        // Set imageInput click listener
        binding.imageInput.setOnClickListener {
            showImagePickerDialog()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == YOUR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.imageInput.setImageURI(selectedImageUri)
        }
    }
    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val pickImageIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickImageLauncher.launch(pickImageIntent)
                    }
                    1 -> {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(takePictureIntent)
                    }
                }
            }
        builder.create().show()
    }
    private fun createArticle() {
        val db = Firebase.firestore;
        val storage =  Firebase.storage
        val storageRef = storage.reference
        val title = binding.titleInput.text.toString()
        val description = binding.descriptionInput.text.toString()
        val user = Firebase.auth.currentUser
        val userId = user?.uid
        val currentTime = DateTimeUtils.getCurrentDateTimeInPhilippines()
        if (title.isEmpty() ) {
            Toast.makeText(this, "Please enter a blog title", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty() ) {
            Toast.makeText(this, "Please enter a blog description", Toast.LENGTH_SHORT).show()
            return
        }
        if ( selectedImageUri == null ) {
            Toast.makeText(this, "Please enter a blog image ${selectedImageUri}", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId!!).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userName = document.getString("name")
                val articleData = hashMapOf(
                    "articleTitle" to title,
                    "articleDescription" to description,
                    "author" to userName,
                    "authorId" to userId,
                    "createdAt" to currentTime,
                    "articleImageUrl" to "",
                    "profileImageUrl" to ""
                )
                // Upload image first
                val imageRef = storageRef.child("images/${selectedImageUri!!.lastPathSegment}")
                val uploadTask = imageRef.putFile(selectedImageUri!!)
                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Get download URL
                        task.result.storage.downloadUrl.addOnSuccessListener { uri ->
                            articleData["articleImageUrl"] = uri.toString()
                            // Add article document to Firestore
                            db.collection("articles").add(articleData)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                                    Toast.makeText(this, "Article added successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding document", e)
                                    Toast.makeText(this, "Error adding document", Toast.LENGTH_SHORT).show()
                                }
                        }.addOnFailureListener { e ->
                            Log.w(TAG, "Error getting download URL", e)
                            Toast.makeText(this, "Error getting download URL", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.w(TAG, "Image upload failed", task.exception)
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
            }

        }
        }
    }
    private fun retrieveCachedUserInfo(): Map<String, String?> {
        val sharedPrefs = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val uid = sharedPrefs.getString("uid", null)
        val email = sharedPrefs.getString("email", null)
        val name = sharedPrefs.getString("name", null)
        return mapOf("uid" to uid, "email" to email, "name" to name)
    }


}