package com.example.appfood.domain.model

import java.io.Serializable

data class Order(
    var id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val deliveryType: String = "dine_in", // "dine_in" or "takeaway"
    val customerName: String = "",
    val customerPhone: String = "",
    val status: String = "pending", // pending, confirmed, completed, cancelled
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val address: String = "",
    val tableNumber: String = "",
    var earnedPoints: Int = 0,
    var usedPoints: Int = 0,
    var isPointAdded: Boolean = false,
    var rating: Float = 0f,
    var feedback: String = ""
) : Serializable {
    fun isDineIn(): Boolean = deliveryType == "dine_in"
    fun isTakeaway(): Boolean = deliveryType == "takeaway"
}

