package com.example.appfood.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.repository.CartRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleCartRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : CartRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: Flow<List<CartItem>> = _cartItems.asStateFlow()
    
    init {
        loadCartItems()
    }
    
    private fun loadCartItems() {
        val cartJson = prefs.getString("cart_items", null)
        if (cartJson != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            val items = gson.fromJson<List<CartItem>>(cartJson, type) ?: emptyList()
            _cartItems.value = items
        }
    }
    
    private fun saveCartItems() {
        val cartJson = gson.toJson(_cartItems.value)
        prefs.edit().putString("cart_items", cartJson).apply()
    }
    
    override suspend fun addToCart(cartItem: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { 
            it.foodId == cartItem.foodId && it.topping == cartItem.topping 
        }
        
        if (existingIndex >= 0) {
            currentItems[existingIndex] = currentItems[existingIndex].copy(
                quantity = currentItems[existingIndex].quantity + cartItem.quantity
            )
        } else {
            currentItems.add(cartItem)
        }
        
        _cartItems.value = currentItems
        saveCartItems()
    }
    
    override suspend fun updateCartItem(cartItem: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == cartItem.id }
        
        if (index >= 0) {
            if (cartItem.quantity <= 0) {
                currentItems.removeAt(index)
            } else {
                currentItems[index] = cartItem
            }
            _cartItems.value = currentItems
            saveCartItems()
        }
    }
    
    override suspend fun removeCartItem(itemId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.id == itemId }
        _cartItems.value = currentItems
        saveCartItems()
    }
    
    override suspend fun clearCart() {
        _cartItems.value = emptyList()
        saveCartItems()
    }
    
    override suspend fun getCartItemsCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }
    
    override suspend fun getCartTotal(): Double {
        return _cartItems.value.sumOf { it.foodPrice * it.quantity }
    }
    
    override suspend fun getCartItemByFoodId(foodId: String, topping: String): CartItem? {
        return _cartItems.value.find { it.foodId == foodId && it.topping == topping }
    }
    
    fun getCartSummary(): Pair<Int, Double> {
        val items = _cartItems.value
        val totalItems = items.sumOf { it.quantity }
        val totalPrice = items.sumOf { it.foodPrice * it.quantity }
        return Pair(totalItems, totalPrice)
    }
}
