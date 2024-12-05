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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.blogs.BlogDetailsActivity
import com.example.medmatters.databinding.FragmentHomeBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var isLoading = false
    private var isLastPage = false
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val articlesPerPage = 6
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        db.collection("articles")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Order by creation time
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

                    // Update UI with fetched articles

                    updateUIWithArticles(fetchedArticles)
                } else {
                    Log.d("HomeFragment", "Current data: null")
                }
            }

        binding.addWasteButton.setOnClickListener {
            val intent = Intent(requireContext(), AddArticleActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    private fun updateUIWithArticles(fetchedArticles: List<ArticleDataModel>) {
        val adapter = ArticleAdapter(fetchedArticles){ article ->
            val intent = Intent(requireContext(), BlogDetailsActivity::class.java)
            intent.putExtra("author", article.author)
            intent.putExtra("articleTitle", article.articleTitle)
            intent.putExtra("articleDescription", article.articleDescription)
            intent.putExtra("articleImageUrl", article.articleImageUrl)
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

                // Load data for visible items
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
//    private fun fetchArticles() {
//        isLoading = true
//
//        val query = if (lastVisibleDocument == null) {
//            db.collection("articles")
//                .orderBy("createdAt", Query.Direction.DESCENDING)
//                .limit(articlesPerPage.toLong())
//        } else {
//            db.collection("articles")
//                .orderBy("createdAt", Query.Direction.DESCENDING)
//                .startAfter(lastVisibleDocument)
//                .limit(articlesPerPage.toLong())
//        }
//
//        query.get()
//            .addOnSuccessListener { snapshot ->
//                isLoading = false
//
//                if (snapshot.isEmpty) {
//                    isLastPage = true
//                    return@addOnSuccessListener
//                }
//
//                val fetchedArticles = mutableListOf<ArticleDataModel>()
//                for (document in snapshot.documents) {
//                    val article = document.toObject(ArticleDataModel::class.java)
//                    article?.let { fetchedArticles.add(it) }
//                }
//
//                lastVisibleDocument = snapshot.documents.lastOrNull() // Update for next page
//
//                updateUIWithArticles(fetchedArticles)
//            }
//            .addOnFailureListener { exception ->
//                isLoading = false
//                Log.w("HomeFragment", "Error fetching articles: ", exception)
//                // Handle error (e.g., show a message to the user)
//            }
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}