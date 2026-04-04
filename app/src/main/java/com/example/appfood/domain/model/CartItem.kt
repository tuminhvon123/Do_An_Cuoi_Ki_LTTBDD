package com.example.appfood.domain.model

import java.io.Serializable

data class CartItem(
    val id: String = "",
    val foodId: String = "",
    val foodTitle: String = "",
    val foodDescription: String = "",
    val foodPrice: Double = 0.0,
    val foodImageUrl: String = "",
    val quantity: Int = 1,
    val topping: String = "",
    val addedAt: Long = System.currentTimeMillis()
) : Serializable {
    fun getTotalPrice(): Double {
        return foodPrice * quantity
    }
}
