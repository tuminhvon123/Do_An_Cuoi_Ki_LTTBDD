package com.example.appfood.presentation.ui.activity

import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfood.databinding.ActivityOrderHistoryBinding
import com.example.appfood.domain.model.Order
import com.example.appfood.presentation.adapter.OrderAdapter
import com.example.appfood.presentation.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var adapter: OrderAdapter
    private val viewModel: HistoryViewModel by viewModels()

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
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

        val currentUser = FirebaseAuth.getInstance().currentUser
        val isAdmin = currentUser?.email == "admin@gmail.com"

        adapter = OrderAdapter(
            isAdmin = isAdmin,
            onItemClick = { order ->
                openOrderDetail(order)
            },
            onUpdateStatus = { order ->
                viewModel.updateOrderStatus(order.id, "completed") {
                    Toast.makeText(this, "Đã chuyển sang hoàn thành", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = this@OrderHistoryActivity.adapter
        }
    }
    private fun openOrderDetail(order: Order) {
        val intent = Intent(this, OrderDetailActivity::class.java)
        intent.putExtra("order_data", order)
        startActivity(intent)
    }

    private fun observeData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE

        lifecycleScope.launch {
            delay(500) // Thêm delay = progress bar
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
}