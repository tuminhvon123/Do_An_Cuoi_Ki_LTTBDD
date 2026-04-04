package com.example.appfood.domain.usecase

import com.example.appfood.domain.repository.CartRepository
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(itemId: String) {
        cartRepository.removeCartItem(itemId)
    }
}
