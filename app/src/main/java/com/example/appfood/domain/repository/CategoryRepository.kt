package com.example.appfood.domain.repository

import com.example.appfood.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(id: Int): Flow<Category?>
}
