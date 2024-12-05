package com.example.medmatters.blogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.databinding.ActivityBlogDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BlogDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isArticleSaved = false
    private var isArticleLiked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBlogDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val author = intent.getStringExtra("author")
        val articleTitle = intent.getStringExtra("articleTitle")
        val articleDescription = intent.getStringExtra("articleDescription")
        val articleImageUrl = intent.getStringExtra("articleImageUrl")
        val createdAt = intent.getStringExtra("createdAt")
        val articleId = intent.getStringExtra("articleId")
        binding.blogTitle.text = articleTitle
        binding.blogDescription.text = articleDescription
        binding.author.text = author
        binding.dateText.text = createdAt
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        checkArticleStatus(articleId!!)
        showDeleteButtonIfCreatedByUser(articleId)
        Glide.with(this)
            .load(R.drawable.ic_profile)
            .error(R.drawable.ic_camera)
            .into(binding.profileImg)

        Glide.with(this)
            .load(articleImageUrl)
            .error(R.drawable.ic_camera)
            .into(binding.blogImage)

        binding.shareButton.setOnClickListener {
            val blogUrl = Uri.Builder()
                .scheme("your-app-scheme")
                .authority("blog-details")
                .appendQueryParameter("articleId", articleId) // Add other parameters as needed
                .appendQueryParameter("author", author)
                .appendQueryParameter("articleTitle", articleTitle)
                .appendQueryParameter("articleDescription", articleDescription)
                .appendQueryParameter("articleImageUrl", articleImageUrl)
                .appendQueryParameter("createdAt", createdAt)
                .build()
                .toString()

            // Copy the deep link URL to the clipboard
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("blog_url", blogUrl)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(this, "Blog link copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.saveButton.setOnClickListener {
            saveToReadingList(articleId)
        }

        binding.backButton.setOnClickListener {
            finish()
        }
        binding.likeButton.setOnClickListener {
            likeButton(articleId=articleId)
        }
        binding.deleteButton.setOnClickListener {
            deleteArticle(articleId=articleId)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun deleteArticle(articleId: String) {
        db.collection("articles").document(articleId)
            .delete()
            .addOnSuccessListener {
                Log.d("BlogDetailsActivity", "Article deleted successfully")
                Toast.makeText(this, "Article deleted", Toast.LENGTH_SHORT).show()
                finish() // Finish the activity after deleting the article
            }
            .addOnFailureListener { e ->
                Log.w("BlogDetailsActivity", "Error deleting article", e)
                Toast.makeText(this, "Error deleting article", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showDeleteButtonIfCreatedByUser(articleId: String) {
        val currentUserId = auth.currentUser?.uid ?: ""

        if (articleId.isNotEmpty() && currentUserId.isNotEmpty()) {
            db.collection("articles").document(articleId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val articleAuthorId = document.getString("authorId")
                        if (articleAuthorId == currentUserId) {
                            // Show the delete button
                            binding.deleteButton.visibility = View.VISIBLE
                        } else {
                            // Hide the delete button
                            binding.deleteButton.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("BlogDetailsActivity", "Error checking article author", exception)
                    // Handle error, e.g., hide the delete button
                    binding.deleteButton.visibility = View.GONE
                }
        } else {
            // Hide the delete button if articleId or currentUserId is empty
            binding.deleteButton.visibility = View.GONE
        }
    }
    private fun checkArticleStatus(articleId: String) {

        val currentUserId = auth.currentUser?.uid ?: ""

        if (articleId.isNotEmpty() && currentUserId.isNotEmpty()) {
            // Check if the user has liked the article
            db.collection("articles").document(articleId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val likers = document.get("likers") as? List<*>
                        if (likers?.contains(currentUserId) == true) {
                            binding.likeButton.setImageResource(R.drawable.ic_heart_filled)
                            isArticleLiked = true
                        } else {
                            binding.likeButton.setImageResource(R.drawable.ic_heart)
                            isArticleLiked = false
                        }
                    }
                }

            // Check if the user has saved the article
            db.collection("users").document(currentUserId).collection("readingList").document(articleId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        isArticleSaved = true
                        binding.saveButton.setImageResource(R.drawable.ic_save_filled) // Assuming you have this drawable
                    } else {
                        isArticleSaved = false
                        binding.saveButton.setImageResource(R.drawable.ic_save) // Assuming you have this drawable
                    }
                }
        }
    }
    private fun saveToReadingList(articleId: String) {
        val currentUserId = auth.currentUser?.uid ?: ""

        if (articleId.isNotEmpty() && currentUserId.isNotEmpty()) {
            val readingListRef = db.collection("users").document(currentUserId).collection("readingList")
            isArticleSaved = !isArticleSaved
            // Check if the article already exists in the reading list
            readingListRef.document(articleId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Article exists, remove it
                        readingListRef.document(articleId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Article removed from reading list", Toast.LENGTH_SHORT).show()
                                binding.saveButton.setImageResource(R.drawable.ic_save)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("BlogDetailsActivity", "Error removing from reading list", exception)
                                Toast.makeText(this, "Failed to remove from reading list", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Article doesn't exist, add it
                        val articleData = hashMapOf(
                            "articleId" to articleId,
                            "timestamp" to FieldValue.serverTimestamp()
                        )
                        readingListRef.document(articleId).set(articleData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Article saved to reading list", Toast.LENGTH_SHORT).show()
                                binding.saveButton.setImageResource(R.drawable.ic_save_filled)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("BlogDetailsActivity", "Error saving to reading list", exception)
                                Toast.makeText(this, "Failed to save to reading list", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("BlogDetailsActivity", "Error checking reading list", exception)
                    Toast.makeText(this, "Failed to check reading list", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun likeButton(articleId: String){
        val currentUserId = auth.currentUser?.uid ?: ""
        if (articleId.isNotEmpty() && currentUserId.isNotEmpty()) {
            val articleRef = db.collection("articles").document(articleId)

            articleRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likers = document.get("likers") as? List<*>

                    if (likers!!.contains(currentUserId)) {
                        articleRef.update("likers", FieldValue.arrayRemove(currentUserId))
                        binding.likeButton.setImageResource(R.drawable.ic_heart)
                    } else {
                        articleRef.update("likers", FieldValue.arrayUnion(currentUserId))
                        binding.likeButton.setImageResource(R.drawable.ic_heart_filled)
                    }
                }
            }
        }
    }
}