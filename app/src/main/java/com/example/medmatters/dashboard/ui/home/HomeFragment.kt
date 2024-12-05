package com.example.medmatters.dashboard.ui.home

import ArticleAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.blogs.BlogDetailsActivity
import com.example.medmatters.databinding.FragmentHomeBinding
import com.example.medmatters.utils.DateTimeUtils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        db.collection("articles")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("HomeFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedArticles = mutableListOf<ArticleDataModel>()
                    for (document in snapshot.documents) {
                        val article = document.toObject(ArticleDataModel::class.java)
                        article?.id = document.id
                        article?.let { fetchedArticles.add(it) }
                    }
                    updateUIWithArticles(fetchedArticles)
                } else {
                    Log.d("HomeFragment", "Current data: null")
                }
            }
        binding.searchInput.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform search when the user submits the query
                if (query != null) {
                    searchArticles(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search as the user types (optional)
                if (newText != null) {
                    searchArticles(newText)
                }
                return true
            }
        })
        binding.addWasteButton.setOnClickListener {
            val intent = Intent(requireContext(), AddArticleActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
    private fun searchArticles(query: String) {
        db.collection("articles")
            .whereGreaterThanOrEqualTo("articleTitle", query) // Search by article title
            .whereLessThanOrEqualTo("articleTitle", query + "\uf8ff") // Range query for prefix search
            .orderBy("articleTitle") // Order by title for better results
            .get()
            .addOnSuccessListener { snapshot ->
                val fetchedArticles = mutableListOf<ArticleDataModel>()
                for (document in snapshot.documents) {
                    val article = document.toObject(ArticleDataModel::class.java)
                    article?.id = document.id
                    article?.let { fetchedArticles.add(it) }
                }
                updateUIWithArticles(fetchedArticles) // Update UI with search results
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
    private fun updateUIWithArticles(fetchedArticles: List<ArticleDataModel>) {
        val adapter = ArticleAdapter(fetchedArticles){ article ->
            val intent = Intent(requireContext(), BlogDetailsActivity::class.java)
            intent.putExtra("author", article.author)
            intent.putExtra("articleTitle", article.articleTitle)
            intent.putExtra("articleDescription", article.articleDescription)
            intent.putExtra("articleImageUrl", article.articleImageUrl)
            intent.putExtra("createdAt",
                article.createdAt?.let { DateTimeUtils.timestampToReadableString(article.createdAt) })
            intent.putExtra("articleId", article.id)
            startActivity(intent)
        }
        binding.articleList.adapter = adapter

        if (fetchedArticles.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.articleList.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.articleList.visibility = View.VISIBLE
        }

        binding.articleList.layoutManager = LinearLayoutManager(requireContext())
        binding.articleList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val firstVisibleItem = LinearLayoutManager(requireContext()).findFirstVisibleItemPosition()
                val lastVisibleItem = LinearLayoutManager(requireContext()).findLastVisibleItemPosition()

                for (i in firstVisibleItem..lastVisibleItem) {
                    val article = fetchedArticles.getOrNull(i)
                    article?.let {
                        Glide.with(recyclerView.context)
                            .load(it.profileImageUrl)
                            .into(
                                binding.articleList.findViewHolderForAdapterPosition(i)?.itemView?.findViewById<ImageView>(
                                    R.id.profile_img
                                )!!
                            )

                        Glide.with(recyclerView.context)
                            .load(it.articleImageUrl)
                            .into(
                                binding.articleList.findViewHolderForAdapterPosition(i)?.itemView?.findViewById<ImageView>(
                                    R.id.article_image
                                )!!
                            )
                    }
                }
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}