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
    fun updateOrderStatus(orderId: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, status)
            result.onSuccess {
                onSuccess()
            }.onFailure {
                android.util.Log.e("HISTORY", "Lỗi cập nhật trạng thái: ${it.message}")
            }
        }
    }
    fun loadOrders() {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid
        val isAdmin = currentUser?.email == "admin@gmail.com"

        // Nếu không phải admin mới cần userId
        val userId = if (!isAdmin) {
            currentUserId ?: getGuestUserId()
        } else {
            "ADMIN"
        }

        if (!isAdmin && userId == null) {
            android.util.Log.e("HISTORY", "Chưa đăng nhập và không có guest ID!")
            _orders.value = emptyList()
            return
        }

        android.util.Log.d("HISTORY", "isAdmin: $isAdmin")

        viewModelScope.launch {
            try {
                orderRepository.getUserOrders(userId!!).collect { orderList ->

                    val result = if (isAdmin) {
                        orderList
                    } else {
                        orderList
                    }

                    android.util.Log.d("HISTORY", "Hiển thị ${result.size} đơn hàng")
                    _orders.value = result.sortedByDescending { it.createdAt }
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