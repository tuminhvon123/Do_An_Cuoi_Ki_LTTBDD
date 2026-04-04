package com.example.appfood.domain.usecase

import com.example.appfood.domain.model.Food
import com.example.appfood.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    fun getAllFoods(): Flow<List<Food>> = foodRepository.getAllFoods()
    
    fun getFoodById(id: String): Flow<Food?> = foodRepository.getFoodById(id)
    
    fun getFoodsByCategory(categoryId: Int): Flow<List<Food>> = foodRepository.getFoodsByCategory(categoryId)
    
    fun searchFoods(query: String): Flow<List<Food>> = foodRepository.searchFoods(query)
}
