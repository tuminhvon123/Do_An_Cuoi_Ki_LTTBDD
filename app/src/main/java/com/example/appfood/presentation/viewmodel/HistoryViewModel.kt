package com.example.appfood.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.Order
import com.example.appfood.domain.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val currentUserId = auth.currentUser?.uid

        // Nếu đã đăng nhập, dùng UID; nếu không thì kiểm tra guest ID
        val userId = currentUserId ?: getGuestUserId()

        if (userId == null) {
            android.util.Log.e("HISTORY", "Chưa đăng nhập và không có guest ID!")
            _orders.value = emptyList()
            return
        }

        android.util.Log.d("HISTORY", "Đang tải đơn cho user: $userId")

        viewModelScope.launch {
            try {
                orderRepository.getUserOrders(userId).collect { orderList ->
                    android.util.Log.d("HISTORY", "Tìm thấy ${orderList.size} đơn hàng")
                    _orders.value = orderList.sortedByDescending { it.createdAt }
                }
            } catch (e: Exception) {
                android.util.Log.e("HISTORY", "Lỗi tải đơn: ${e.message}")
            }
        }
    }

    private fun getGuestUserId(): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("guest_user_id", null)
    }

    // Gọi khi quay lại màn hình để refresh
    fun refreshOrders() {
        android.util.Log.d("HISTORY", "Refresh đơn hàng...")
        loadOrders()
    }

    fun updateRating(orderId: String, rating: Float, feedback: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderRating(orderId, rating, feedback)
            result.onSuccess {
                onSuccess()
            }
        }
    }
}