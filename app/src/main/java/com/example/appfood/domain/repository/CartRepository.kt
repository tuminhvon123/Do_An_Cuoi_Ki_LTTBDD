package com.example.appfood.domain.repository

import com.example.appfood.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItems: Flow<List<CartItem>>
    suspend fun addToCart(cartItem: CartItem)
    suspend fun updateCartItem(cartItem: CartItem)
    suspend fun removeCartItem(itemId: String)
    suspend fun clearCart()
    suspend fun getCartItemsCount(): Int
    suspend fun getCartTotal(): Double
    suspend fun getCartItemByFoodId(foodId: String, topping: String): CartItem?
}
