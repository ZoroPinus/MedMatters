import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medmatters.R
import com.example.medmatters.dashboard.ui.home.ArticleDataModel

class ArticleAdapter(private val articles: List<ArticleDataModel>, private val onItemClick: (ArticleDataModel) -> Unit) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_img)
        val author: TextView = itemView.findViewById(R.id.author)
        val articleTitle: TextView = itemView.findViewById(R.id.article_title)
        val articleDescription: TextView = itemView.findViewById(R.id.article_description)
        val articleImage: ImageView = itemView.findViewById(R.id.article_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item_layout, parent, false)
        return ArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = articles[position]

        holder.author.text = currentArticle.author
        holder.articleTitle.text = currentArticle.articleTitle
        holder.articleDescription.text = currentArticle.articleDescription

        // Load images using Glide
        Glide.with(holder.itemView.context)
            .load(currentArticle.profileImageUrl)
            .error(R.drawable.ic_camera)
            .into(holder.profileImage)

        Glide.with(holder.itemView.context)
            .load(currentArticle.articleImageUrl)
            .error(R.drawable.ic_camera)
            .into(holder.articleImage)

        holder.itemView.setOnClickListener {
            onItemClick(currentArticle)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}