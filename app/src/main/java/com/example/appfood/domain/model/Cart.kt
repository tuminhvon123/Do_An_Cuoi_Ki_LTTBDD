package com.example.appfood.domain.model

import java.io.Serializable

data class Cart(
    val id: String = "",
    val items: List<CartItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    fun getTotalItems(): Int {
        return items.sumOf { it.quantity }
    }
    
    fun getTotalPrice(): Double {
        return items.sumOf { it.getTotalPrice() }
    }
    
    fun isEmpty(): Boolean {
        return items.isEmpty()
    }
}
