package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.CartItem
import com.example.appfood.domain.model.FoodRating
import com.example.appfood.domain.repository.FoodRatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val foodRatingRepository: FoodRatingRepository
) : ViewModel() {

    suspend fun addFoodRating(rating: FoodRating, cartItem: CartItem): Result<String> {
        return try {
            val result = foodRatingRepository.addRating(rating)
            if (result.isSuccess) {
                // Update the cart item's rating locally
                cartItem.rating = rating.rating
                cartItem.feedback = rating.feedback
                cartItem.isRated = true
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadRatingsForOrder(items: List<CartItem>, userId: String, orderId: String) {
        items.forEach { item ->
            if (!item.isRated) {
                val hasRated = foodRatingRepository.hasUserRatedFood(userId, orderId, item.foodId)
                if (hasRated) {
                    val rating = foodRatingRepository.getRatingByFoodAndUser(item.foodId, userId)
                    rating?.let {
                        item.rating = it.rating
                        item.feedback = it.feedback
                        item.isRated = true
                    }
                }
            }
        }
    }
}
