package com.example.medmatters.blogs

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBlogDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val author = intent.getStringExtra("author")
        val articleTitle = intent.getStringExtra("articleTitle")
        val articleDescription = intent.getStringExtra("articleDescription")
        val articleImageUrl = intent.getStringExtra("articleImageUrl")
        val articleId = intent.getStringExtra("articleId")

        binding.blogTitle.text = articleTitle
        binding.blogDescription.text = articleDescription
        binding.author.text = author

        Glide.with(this)
            .load(R.drawable.ic_profile)
            .error(R.drawable.ic_camera)
            .into(binding.profileImg)

        Glide.with(this)
            .load(articleImageUrl)
            .error(R.drawable.ic_camera)
            .into(binding.blogImage)



        binding.backButton.setOnClickListener {
            finish()
        }
        binding.likeButton.setOnClickListener {
            likeButton(articleId=articleId.toString())
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun likeButton(articleId: String){
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
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