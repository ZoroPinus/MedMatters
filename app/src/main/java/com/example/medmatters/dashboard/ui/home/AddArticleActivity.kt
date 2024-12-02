package com.example.medmatters.dashboard.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.medmatters.databinding.ActivityAddArticleBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    companion object {
        private const val YOUR_REQUEST_CODE = 123
    }
    private lateinit var binding: ActivityAddArticleBinding
    private val storageRef = Firebase.database.reference
    private var selectedImageUri: Uri? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri = data.data
                    binding.imageInput.setImageURI(selectedImageUri)
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    binding.imageInput.setImageBitmap(imageBitmap)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.submitButton.setOnClickListener {

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
        val title = binding.titleInput.text.toString()
        val description = binding.descriptionInput.text.toString()

        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: "Anonymous"
        val imageUrl = user?.photoUrl?.toString() ?: ""


        val currentTime = Date()
        if (title.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create Article object
        val article = ArticleDataModel(
            articleTitle = title,
            articleDescription = description,
            author = userName,
            createdAt = currentTime.time,
            articleImageUrl = "",
            profileImageUrl = imageUrl
        )

        val database = FirebaseDatabase.getInstance()
        val articlesRef = database.getReference("articles")
        val newArticleRef = articlesRef.push()
        newArticleRef.setValue(article)
            .addOnSuccessListener {
               Toast.makeText(this, "Article created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to create article: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


}