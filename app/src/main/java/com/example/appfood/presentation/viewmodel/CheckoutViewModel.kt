package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.model.Order
import com.example.appfood.domain.repository.CartRepository
import com.example.appfood.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    private val _deliveryType = MutableStateFlow("dine_in")
    val deliveryType: StateFlow<String> = _deliveryType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _orderSuccess = MutableStateFlow(false)
    val orderSuccess: StateFlow<Boolean> = _orderSuccess.asStateFlow()

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            try {
                cartRepository.cartItems.collect { items ->
                    _cartItems.value = items
                    calculateTotal()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cart items: ${e.message}"
            }
        }
    }

    private fun calculateTotal() {
        _totalPrice.value = _cartItems.value.sumOf { it.getTotalPrice() }
    }

    fun setDeliveryType(type: String) {
        _deliveryType.value = type
    }

    fun createOrder(
        userId: String,
        customerName: String,
        customerPhone: String,
        notes: String,
        customerTable: String,
        customerAddress: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (customerName.isBlank()) {
                    _errorMessage.value = "Vui lòng nhập tên khách hàng"
                    _isLoading.value = false
                    return@launch
                }

                if (customerPhone.isBlank()) {
                    _errorMessage.value = "Vui lòng nhập số điện thoại"
                    _isLoading.value = false
                    return@launch
                }

                if (_cartItems.value.isEmpty()) {
                    _errorMessage.value = "Giỏ hàng trống"
                    _isLoading.value = false
                    return@launch
                }

                val order = Order(
                    userId = userId,
                    items = _cartItems.value,
                    totalPrice = _totalPrice.value,
                    deliveryType = _deliveryType.value,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    notes = notes,
                    tableNumber = if (deliveryType.value == "dine_in") customerTable else "",
                    address = if (deliveryType.value == "takeaway") customerAddress else "",
                    status = "pending"
                )

                // Call createOrder và await kết quả
                val result = orderRepository.createOrder(order)
                
                result.onSuccess { orderId ->
                    // Clear cart after successful order
                    cartRepository.clearCart()
                    _orderSuccess.value = true
                    _errorMessage.value = null
                }.onFailure { exception ->
                    _errorMessage.value = "Lỗi tạo đơn hàng: ${exception.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

