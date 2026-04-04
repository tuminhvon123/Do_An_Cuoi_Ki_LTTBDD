package com.example.appfood.util

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    fun formatPrice(price: Double): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        return "${formatter.format(price)}đ"
    }
    
    fun formatPrice(price: Int): String {
        return formatPrice(price.toDouble())
    }
}
