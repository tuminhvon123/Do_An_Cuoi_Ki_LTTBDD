package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.Order
import com.example.appfood.domain.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository, // Sử dụng Repository thay vì trực tiếp Firestore
    private val auth: FirebaseAuth // Thêm cái này để hết lỗi Unresolved reference 'auth'
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val currentUserId = auth.currentUser?.uid // Bây giờ 'auth' đã được nhận diện

        if (currentUserId == null) {
            android.util.Log.e("HISTORY", "Chưa đăng nhập!")
            return
        }

        viewModelScope.launch {
            // Sử dụng hàm getUserOrders từ Repository (kết nối Realtime Database)
            orderRepository.getUserOrders(currentUserId).collect { orderList ->
                // Sắp xếp đơn hàng mới nhất lên đầu
                _orders.value = orderList.sortedByDescending { it.createdAt }
            }
        }
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