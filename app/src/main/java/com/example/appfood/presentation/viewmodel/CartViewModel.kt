package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _cartSummary = MutableStateFlow(Pair(0, 0.0))
    val cartSummary: StateFlow<Pair<Int, Double>> = _cartSummary.asStateFlow()

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cartRepository.cartItems.collect { items ->
                    _cartItems.value = items
                    updateCartSummary()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cart items: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateCartSummary() {
        try {
            val totalItems = cartRepository.getCartItemsCount()
            val totalPrice = cartRepository.getCartTotal()
            _cartSummary.value = Pair(totalItems, totalPrice)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update cart summary: ${e.message}"
        }
    }

    fun addToCart(food: com.example.appfood.domain.model.Food, quantity: Int = 1, topping: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existingItem = cartRepository.getCartItemByFoodId(food.id, topping)
                val cartItem = if (existingItem != null) {
                    existingItem.copy(quantity = existingItem.quantity + quantity)
                } else {
                    CartItem(
                        foodId = food.id,
                        foodTitle = food.title,
                        foodDescription = food.description,
                        foodPrice = food.price,
                        foodImageUrl = food.imageUrl,
                        quantity = quantity,
                        topping = topping
                    )
                }
                cartRepository.addToCart(cartItem)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add item to cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem.id)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedItem = cartItem.copy(quantity = newQuantity)
                cartRepository.updateCartItem(updatedItem)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update quantity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cartRepository.removeCartItem(itemId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove item from cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cartRepository.clearCart()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getTotalItems(): Int {
        return _cartSummary.value.first
    }

    fun getTotalPrice(): Double {
        return _cartSummary.value.second
    }
}
