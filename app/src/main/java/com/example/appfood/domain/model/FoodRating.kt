package com.example.appfood.domain.model

data class FoodRating(
    var id: String = "",
    val foodId: String = "",
    val foodTitle: String = "",
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    var rating: Float = 0f,
    var feedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getMaskedEmail(): String {
        if (userEmail.isEmpty()) return "Người dùng"
        val atIndex = userEmail.indexOf('@')
        if (atIndex == -1) return userEmail
        
        val prefix = userEmail.substring(0, atIndex)
        val domain = userEmail.substring(atIndex)
        
        val maskedPrefix = if (prefix.length > 3) {
            prefix.substring(0, 2) + "***" + prefix.substring(prefix.length - 1)
        } else {
            "***"
        }
        
        return maskedPrefix + domain
    }
}
