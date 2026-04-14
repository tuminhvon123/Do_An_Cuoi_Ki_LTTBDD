package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfood.databinding.ActivityOrderHistoryBinding
import com.example.appfood.domain.model.Order
import com.example.appfood.presentation.adapter.OrderAdapter
import com.google.firebase.firestore.FirebaseFirestore

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var adapter: OrderAdapter

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter()

        binding.recyclerOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = this@OrderHistoryActivity.adapter
        }
    }

    private fun loadOrders() {

        firestore.collection("orders")
            .get()
            .addOnSuccessListener { result ->

                val orderList = mutableListOf<Order>()

                for (document in result) {

                    try {
                        val order = document.toObject(Order::class.java)

                        order.id = document.id

                        orderList.add(order)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (orderList.isEmpty()) {
                    Toast.makeText(this, "Chưa có đơn hàng nào", Toast.LENGTH_SHORT).show()
                }

                adapter.submitList(orderList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show()
            }
    }
}