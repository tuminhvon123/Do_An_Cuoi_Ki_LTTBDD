package com.example.appfood.domain.repository

import com.example.appfood.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<List<Food>>
    fun getFoodById(id: String): Flow<Food?>
    fun getFoodsByCategory(categoryId: Int): Flow<List<Food>>
    fun searchFoods(query: String): Flow<List<Food>>
}
