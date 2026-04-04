package com.example.appfood.domain.usecase

import com.example.appfood.domain.repository.CartRepository
import javax.inject.Inject

data class CartSummary(
    val totalItems: Int,
    val totalPrice: Double
)

class GetCartSummaryUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(): CartSummary {
        val totalItems = cartRepository.getCartItemsCount()
        val totalPrice = cartRepository.getCartTotal()
        return CartSummary(totalItems, totalPrice)
    }
}
