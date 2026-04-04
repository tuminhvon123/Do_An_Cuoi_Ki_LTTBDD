package com.example.appfood.domain.usecase

import com.example.appfood.domain.model.Category
import com.example.appfood.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    fun getAllCategories(): Flow<List<Category>> = categoryRepository.getAllCategories()
    
    fun getCategoryById(id: Int): Flow<Category?> = categoryRepository.getCategoryById(id)
}
