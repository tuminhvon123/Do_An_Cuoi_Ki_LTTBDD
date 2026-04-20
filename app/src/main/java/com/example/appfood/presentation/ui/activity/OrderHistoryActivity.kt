package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfood.R
import com.example.appfood.databinding.ActivityOrderHistoryBinding
import com.example.appfood.domain.model.Order
import com.example.appfood.presentation.adapter.OrderAdapter
import com.example.appfood.presentation.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var adapter: OrderAdapter
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        setupRecyclerView()
        observeData()

        // Log để debug
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        android.util.Log.d("HISTORY", "Current user: ${currentUser?.uid ?: "NULL - Chưa đăng nhập"}")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("HISTORY", "onResume - Refresh đơn hàng")
        viewModel.refreshOrders() // Refresh khi quay lại màn hình
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

    private fun observeData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE

        lifecycleScope.launch {
            viewModel.orders.collect { orderList ->
                binding.progressBar.visibility = View.GONE

                if (orderList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.recyclerOrders.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.recyclerOrders.visibility = View.VISIBLE
                    adapter.submitList(orderList)
                }
            }
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

                    viewModel.updateRating(order.id, ratingValue, feedbackText) {
                        Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                show()
            }
    }
}