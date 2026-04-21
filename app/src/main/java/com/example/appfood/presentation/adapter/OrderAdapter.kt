package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appfood.R
import com.example.appfood.databinding.ItemOrderBinding
import com.example.appfood.domain.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {

                // Mã đơn
                txtOrderId.text = "Mã đơn: #${order.id.takeLast(5)}"

                txtItems.text = if (order.items.isNotEmpty()) {
                    order.items
                        .take(3) // lấy tối đa 3 món cho tối ưu UI
                        .joinToString("\n") { "${it.foodTitle} (x${it.quantity})" } +
                            if (order.items.size > 3) "\n..." else ""
                } else {
                    "Không có món"
                }

                // Tổng tiền
                txtTotalPrice.text = "Tổng tiền: ${String.format("%,.0f", order.totalPrice)}đ"

                // Trạng thái
                val statusText = when (order.status.lowercase()) {
                    "pending" -> "Đang chờ"
                    "confirmed" -> "Đã xác nhận"
                    "completed", "done" -> "Đã hoàn thành"
                    "cancelled" -> "Đã hủy"
                    else -> order.status
                }
                txtStatus.text = statusText

                val colorRes = when (order.status.uppercase()) {
                    "PENDING" -> android.R.color.holo_orange_dark
                    "CONFIRMED" -> android.R.color.holo_blue_dark
                    "DONE", "COMPLETED" -> android.R.color.holo_green_dark
                    "CANCELLED" -> android.R.color.holo_red_dark
                    else -> android.R.color.black
                }
                txtStatus.setTextColor(ContextCompat.getColor(root.context, colorRes))

                // Ngày
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                txtDate.text = sdf.format(Date(order.createdAt))

                // Ẩn rating bar và feedback
                orderRatingBar.visibility = View.GONE
                txtFeedback.visibility = View.GONE

            }
            binding.root.setOnClickListener {
                onItemClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean = oldItem == newItem
    }
}