package com.example.appfood.domain.usecase

import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return cartRepository.cartItems
    }
}
