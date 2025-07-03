package com.sandip.skinglance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sandip.skinglance.ArticlesAdapter.ArticleViewHolder

class ArticlesAdapter(private val list: List<Article>) : RecyclerView.Adapter<ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.article_view, parent, false)

        return ArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val ItemsViewModel = list[position]
        holder.articleTitle.text = ItemsViewModel.title
        holder.articleDescription.text = ItemsViewModel.content
        holder.articleImage.setImageResource(ItemsViewModel.image)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val articleTitle: TextView = view.findViewById(R.id.article_title)
        val articleDescription: TextView = view.findViewById(R.id.article_desc)
        val articleImage: ImageView = view.findViewById(R.id.article_image)
    }
}
