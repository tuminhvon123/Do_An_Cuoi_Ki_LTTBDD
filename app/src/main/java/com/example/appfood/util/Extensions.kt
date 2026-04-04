package com.example.appfood.util

import java.text.DecimalFormat

object Extensions {
    
    fun Double.formatCurrency(): String {
        val formatter = DecimalFormat("#,### VNĐ")
        return formatter.format(this)
    }
    
    fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
    
    fun String.isStrongPassword(): Boolean {
        return this.length >= 6
    }
}
