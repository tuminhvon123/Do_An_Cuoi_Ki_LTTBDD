package com.example.appfood.domain.repository

import com.example.appfood.domain.model.FoodRating
import kotlinx.coroutines.flow.Flow

interface FoodRatingRepository {
    suspend fun addRating(rating: FoodRating): Result<String>
    suspend fun getRatingsByFoodId(foodId: String): Flow<List<FoodRating>>
    suspend fun getAverageRating(foodId: String): Flow<Float>
    suspend fun getRatingCount(foodId: String): Flow<Int>
    suspend fun hasUserRatedFood(userId: String, orderId: String, foodId: String): Boolean
    suspend fun getRatingByFoodAndUser(foodId: String, userId: String): FoodRating?
}
