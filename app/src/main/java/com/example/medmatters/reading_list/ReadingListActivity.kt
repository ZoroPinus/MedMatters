package com.example.medmatters.reading_list

import ArticleAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.blogs.BlogDetailsActivity
import com.example.medmatters.dashboard.ui.home.ArticleDataModel
import com.example.medmatters.databinding.ActivityReadingListBinding
import com.example.medmatters.utils.DateTimeUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReadingListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadingListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReadingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        binding.backButton.setOnClickListener {
            finish()
        }

        db.collection("users").document(currentUserId).collection("readingList")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val articleIds = querySnapshot.documents.mapNotNull { it.getString("articleId") }
                fetchArticlesByIds(articleIds)
            }
            .addOnFailureListener { exception ->
                Log.e("ReadingListActivity", "Error getting reading list", exception)
                // Handle error, e.g., show an error message
            }
    }
    private fun fetchArticlesByIds(articleIds: List<String>) {
        if (articleIds.isEmpty()) {
            // Handle empty reading list, e.g., show an empty view
            updateUIWithArticles(emptyList()) // Update UI with empty list
            return
        }

        val articles = mutableListOf<ArticleDataModel>()
        val fetchTasks = articleIds.map { articleId ->
            db.collection("articles").document(articleId).get() // Fetch by document ID
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val article = document.toObject(ArticleDataModel::class.java)
                        article?.let { articles.add(it) }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ReadingListActivity", "Error fetching article $articleId", exception)
                    // Handle error, e.g., show an error message for this specific article
                }
        }
        Tasks.whenAllSuccess<DocumentSnapshot>(fetchTasks)
            .addOnSuccessListener {
                updateUIWithArticles(articles)
            }
            .addOnFailureListener { exception ->
                Log.e("ReadingListActivity", "Error fetching articles", exception)
                // Handle overall error, e.g., show a general error message
            }
    }
    private fun updateUIWithArticles(fetchedArticles: List<ArticleDataModel>) {
        val adapter = ArticleAdapter(fetchedArticles){ article ->
            val intent = Intent(this, BlogDetailsActivity::class.java)
            intent.putExtra("author", article.author)
            intent.putExtra("articleTitle", article.articleTitle)
            intent.putExtra("articleDescription", article.articleDescription)
            intent.putExtra("articleImageUrl", article.articleImageUrl)
            intent.putExtra("createdAt",
                article.createdAt?.let { DateTimeUtils.timestampToReadableString(article.createdAt) })
            intent.putExtra("articleId", article.id)
            startActivity(intent)
        }
        binding.readingList.adapter = adapter

        if (fetchedArticles.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.readingList.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.readingList.visibility = View.VISIBLE
        }

        binding.readingList.layoutManager = LinearLayoutManager(this)
        binding.readingList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val firstVisibleItem = LinearLayoutManager(recyclerView.context).findFirstVisibleItemPosition()
                val lastVisibleItem = LinearLayoutManager(recyclerView.context).findLastVisibleItemPosition()

                for (i in firstVisibleItem..lastVisibleItem) {
                    val article = fetchedArticles.getOrNull(i)
                    article?.let {
                        Glide.with(recyclerView.context)
                            .load(it.profileImageUrl)
                            .into(
                                binding.readingList.findViewHolderForAdapterPosition(i)?.itemView?.findViewById<ImageView>(
                                    R.id.profile_img
                                )!!
                            )

                        Glide.with(recyclerView.context)
                            .load(it.articleImageUrl)
                            .into(
                                binding.readingList.findViewHolderForAdapterPosition(i)?.itemView?.findViewById<ImageView>(
                                    R.id.article_image
                                )!!
                            )
                    }
                }
            }
        })
    }
}