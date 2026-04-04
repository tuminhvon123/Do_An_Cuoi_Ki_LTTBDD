package com.example.appfood.domain.usecase

import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartItemUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(cartItem: CartItem) {
        cartRepository.updateCartItem(cartItem)
    }
}
