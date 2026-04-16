package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.text
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
    private val onRatingClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    // ViewHolder sử dụng ViewBinding
    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                // Hiển thị dữ liệu
                txtOrderId.text = "Mã đơn: #${order.id.takeLast(5)}"
                txtTotalPrice.text = "Tổng tiền: ${String.format("%,.0f", order.totalPrice)}đ"

                // Format status text
                val statusText = when (order.status.lowercase()) {
                    "pending" -> "Đang chờ"
                    "confirmed" -> "Đã xác nhận"
                    "completed", "done" -> "Đã hoàn thành"
                    "cancelled" -> "Đã hủy"
                    else -> order.status
                }
                txtStatus.text = statusText

                // Định dạng màu sắc (Dùng ContextCompat để an toàn hơn)
                val colorRes = when (order.status.uppercase()) {
                    "PENDING" -> android.R.color.holo_orange_dark
                    "CONFIRMED" -> android.R.color.holo_blue_dark
                    "DONE", "COMPLETED" -> android.R.color.holo_green_dark
                    "CANCELLED" -> android.R.color.holo_red_dark
                    else -> android.R.color.black
                }
                txtStatus.setTextColor(ContextCompat.getColor(root.context, colorRes))

                // Format thời gian
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                txtDate.text = sdf.format(Date(order.createdAt))

                // Logic hiển thị Rating/Feedback
                val isCompleted = order.status.lowercase() in listOf("completed", "done")
                if (isCompleted) {
                    if (order.rating > 0) {
                        btnRating.visibility = View.GONE
                        orderRatingBar.visibility = View.VISIBLE
                        orderRatingBar.rating = order.rating
                        txtFeedback.visibility = if (order.feedback.isNotEmpty()) View.VISIBLE else View.GONE
                        txtFeedback.text = "Nhận xét: ${order.feedback}"
                    } else {
                        btnRating.visibility = View.VISIBLE
                        orderRatingBar.visibility = View.GONE
                        txtFeedback.visibility = View.GONE
                        btnRating.setOnClickListener { onRatingClick(order) }
                    }
                } else {
                    btnRating.visibility = View.GONE
                    orderRatingBar.visibility = View.GONE
                    txtFeedback.visibility = View.GONE
                }
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

    // DiffUtil để tối ưu việc cập nhật danh sách
    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem // So sánh toàn bộ nội dung
        }
    }
}