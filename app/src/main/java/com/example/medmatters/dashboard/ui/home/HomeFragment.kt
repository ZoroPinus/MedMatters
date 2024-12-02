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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val database = FirebaseDatabase.getInstance()
        val articlesRef = database.getReference("articles") // Replace "articles" with your Firebase node

        articlesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fetchedArticles = mutableListOf<ArticleDataModel>()
                for (articleSnapshot in dataSnapshot.children) {
                    val article = articleSnapshot.getValue(ArticleDataModel::class.java)
                    article?.let { fetchedArticles.add(it) }
                }

                val adapter = ArticleAdapter(fetchedArticles)
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

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("HomeFragment", "loadPost:onCancelled", databaseError.toException())
            }
        })

        binding.addWasteButton.setOnClickListener {
            val intent = Intent(requireContext(), AddArticleActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}