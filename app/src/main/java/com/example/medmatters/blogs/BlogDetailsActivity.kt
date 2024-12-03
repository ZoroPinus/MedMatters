package com.example.medmatters.blogs

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.databinding.ActivityBlogDetailsBinding


class BlogDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBlogDetailsBinding.inflate(layoutInflater) // Inflate the layout
        setContentView(binding.root)
        val author = intent.getStringExtra("author")
        val articleTitle = intent.getStringExtra("articleTitle")
        val articleDescription = intent.getStringExtra("articleDescription")
        val articleImageUrl = intent.getStringExtra("articleImageUrl")
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}