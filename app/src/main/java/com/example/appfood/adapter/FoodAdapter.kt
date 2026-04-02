package com.example.appfood.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appfood.databinding.ItemFoodBinding
import com.example.appfood.model.Food
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
        
        // Định dạng giá tiền: 50,000 VNĐ
        val decimalFormat = DecimalFormat("#,###")
        holder.binding.tvFoodPrice.text = "${decimalFormat.format(item.price)} VNĐ"
        
        // Hiển thị trạng thái Hết hàng (Sold Out) - Một yêu cầu trong task của bạn
        if (item.isSoldOut) {
            holder.binding.tvSoldOut.visibility = View.VISIBLE
            holder.binding.imgFood.alpha = 0.5f // Làm mờ ảnh khi hết hàng
        } else {
            holder.binding.tvSoldOut.visibility = View.GONE
            holder.binding.imgFood.alpha = 1.0f
        }

        // Load ảnh món ăn
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.imgFood)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    // Hàm cập nhật danh sách (cho chức năng tìm kiếm hoặc lọc category)
    fun updateList(newList: List<Food>) {
        items = newList
        notifyDataSetChanged()
    }
}