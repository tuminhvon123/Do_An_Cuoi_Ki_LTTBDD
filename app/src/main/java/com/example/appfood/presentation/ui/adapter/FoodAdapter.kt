package com.example.appfood.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appfood.databinding.ItemFoodBinding
import com.example.appfood.domain.model.Food
import java.text.DecimalFormat

class FoodAdapter(
    private var items: List<Food>,
    private val onItemClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        holder.binding.tvFoodTitle.text = item.title
        
        val decimalFormat = DecimalFormat("#,###")
        holder.binding.tvFoodPrice.text = "${decimalFormat.format(item.price)} VNĐ"
        
        // Hiển thị nhãn Bán chạy
        if (item.isBestSeller) {
            holder.binding.tvBestSeller.visibility = View.VISIBLE
        } else {
            holder.binding.tvBestSeller.visibility = View.GONE
        }

        // Hiển thị trạng thái Hết hàng
        if (item.isSoldOut) {
            holder.binding.tvSoldOut.visibility = View.VISIBLE
            holder.binding.imgFood.alpha = 0.5f
        } else {
            holder.binding.tvSoldOut.visibility = View.GONE
            holder.binding.imgFood.alpha = 1.0f
        }

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.imgFood)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Food>) {
        items = newList
        notifyDataSetChanged()
    }
}