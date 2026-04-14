package com.example.appfood.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfood.R
import com.example.appfood.domain.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

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
        holder.txtOrderId.text = "Mã đơn: ${order.id}"
        holder.txtTotalPrice.text = "Tổng tiền: ${order.totalPrice} đ"
        holder.txtStatus.text = "Trạng thái: ${order.status}"

        // Format thời gian
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(order.createdAt)
        holder.txtDate.text = sdf.format(date)

        when (order.status) {
            "PENDING" -> holder.txtStatus.setTextColor(0xFFFFA000.toInt()) // vàng
            "CONFIRMED" -> holder.txtStatus.setTextColor(0xFF2196F3.toInt()) // xanh dương
            "DONE" -> holder.txtStatus.setTextColor(0xFF4CAF50.toInt()) // xanh lá
            "CANCELLED" -> holder.txtStatus.setTextColor(0xFFF44336.toInt()) // đỏ
        }
    }
}