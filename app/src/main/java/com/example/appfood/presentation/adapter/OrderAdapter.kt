package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfood.R
import com.example.appfood.domain.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val onRatingClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var orderList: List<Order> = listOf()

    // Hàm cập nhật dữ liệu
    fun submitList(list: List<Order>) {
        orderList = list
        notifyDataSetChanged()
    }

    // ViewHolder
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtOrderId: TextView = itemView.findViewById(R.id.txtOrderId)
        val txtTotalPrice: TextView = itemView.findViewById(R.id.txtTotalPrice)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val btnRating: Button = itemView.findViewById(R.id.btnRating)
        val orderRatingBar: RatingBar = itemView.findViewById(R.id.orderRatingBar)
        val txtFeedback: TextView = itemView.findViewById(R.id.txtFeedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        // Hiển thị dữ liệu
        holder.txtOrderId.text = "Mã đơn: #${order.id.takeLast(5)}"
        holder.txtTotalPrice.text = "Tổng tiền: ${String.format("%,.0f", order.totalPrice)}đ"
        
        // Format status text
        val statusText = when (order.status.lowercase()) {
            "pending" -> "Đang chờ"
            "confirmed" -> "Đã xác nhận"
            "completed", "done" -> "Đã hoàn thành"
            "cancelled" -> "Đã hủy"
            else -> order.status
        }
        holder.txtStatus.text = statusText

        // Format thời gian
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(order.createdAt)
        holder.txtDate.text = sdf.format(date)

        when (order.status.uppercase()) {
            "PENDING" -> holder.txtStatus.setTextColor(0xFFFFA000.toInt()) // vàng
            "CONFIRMED" -> holder.txtStatus.setTextColor(0xFF2196F3.toInt()) // xanh dương
            "DONE", "COMPLETED" -> holder.txtStatus.setTextColor(0xFF4CAF50.toInt()) // xanh lá
            "CANCELLED" -> holder.txtStatus.setTextColor(0xFFF44336.toInt()) // đỏ
        }

        // Xử lý hiển thị rating
        val isCompleted = order.status.lowercase() == "completed" || order.status.lowercase() == "done"
        if (isCompleted) {
            if (order.rating > 0) {
                holder.btnRating.visibility = View.GONE
                holder.orderRatingBar.visibility = View.VISIBLE
                holder.orderRatingBar.rating = order.rating
                if (order.feedback.isNotEmpty()) {
                    holder.txtFeedback.visibility = View.VISIBLE
                    holder.txtFeedback.text = "Nhận xét: ${order.feedback}"
                } else {
                    holder.txtFeedback.visibility = View.GONE
                }
            } else {
                holder.btnRating.visibility = View.VISIBLE
                holder.orderRatingBar.visibility = View.GONE
                holder.txtFeedback.visibility = View.GONE
                holder.btnRating.setOnClickListener { onRatingClick(order) }
            }
        } else {
            holder.btnRating.visibility = View.GONE
            holder.orderRatingBar.visibility = View.GONE
            holder.txtFeedback.visibility = View.GONE
        }
    }
}