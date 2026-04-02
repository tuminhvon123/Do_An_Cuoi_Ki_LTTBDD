package com.example.appfood.model

import java.io.Serializable

data class Food(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var imageUrl: String = "",
    var categoryId: Int = 0,
    var isSoldOut: Boolean = false,
    var topping: String = ""
) : Serializable