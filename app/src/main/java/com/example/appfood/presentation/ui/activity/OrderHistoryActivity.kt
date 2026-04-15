package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfood.R
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
        adapter = OrderAdapter { order ->
            showRatingDialog(order)
        }

        binding.recyclerOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = this@OrderHistoryActivity.adapter
        }
    }

    private fun loadOrders() {
        firestore.collection("orders")
            .get()
            .addOnSuccessListener { result ->

                val orderList = result.documents.mapNotNull { document ->
                    try {
                        val order = document.toObject(Order::class.java)
                        order?.id = document.id
                        order
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()

                if (orderList.isEmpty()) {
                    Toast.makeText(
                        this@OrderHistoryActivity,
                        "Chưa có đơn hàng nào",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                orderList.sortByDescending { it.createdAt }
                adapter.submitList(orderList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@OrderHistoryActivity,
                    "Lỗi tải đơn hàng: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showRatingDialog(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialogRatingBar)
        val edtFeedback = dialogView.findViewById<EditText>(R.id.edtFeedback)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Đánh giá đơn hàng")
            .setCancelable(true)
            .create()
            .apply {
                dialogView.findViewById<android.view.View>(R.id.btnSubmitRating).setOnClickListener {
                    val ratingValue = ratingBar.rating
                    val feedbackText = edtFeedback.text.toString().trim()

                    if (ratingValue == 0f) {
                        Toast.makeText(context, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    updateRatingToFirestore(order.id, ratingValue, feedbackText)
                    dismiss()
                }
                show()
            }
    }

    private fun updateRatingToFirestore(orderId: String, rating: Float, feedback: String) {
        val updateData = mapOf(
            "rating" to rating,
            "feedback" to feedback
        )

        firestore.collection("orders").document(orderId)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()
                loadOrders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi đánh giá: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}