package com.example.appfood.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appfood.databinding.ItemCategoryBinding
import com.example.appfood.model.Category

class CategoryAdapter(
    private val items: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCategoryName.text = item.name
        
        // Load ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(item.imagePath)
            .into(holder.binding.imgCategory)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}