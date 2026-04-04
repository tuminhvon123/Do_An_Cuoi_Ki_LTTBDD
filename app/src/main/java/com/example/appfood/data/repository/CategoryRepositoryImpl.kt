package com.example.appfood.data.repository

import com.example.appfood.domain.model.Category
import com.example.appfood.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor() : CategoryRepository {
    
    private val categoryList = mutableListOf<Category>()
    
    init {
        initMockData()
    }
    
    private fun initMockData() {
        categoryList.addAll(
            listOf(
                Category(0, "Tất cả", "https://cdn-icons-png.flaticon.com/512/1046/1046771.png"),
                Category(1, "Pizza", "https://cdn-icons-png.flaticon.com/512/3595/3595455.png"),
                Category(2, "Burger", "https://cdn-icons-png.flaticon.com/512/706/706918.png"),
                Category(3, "Drink", "https://cdn-icons-png.flaticon.com/512/2405/2405479.png"),
                Category(4, "Chicken", "https://cdn-icons-png.flaticon.com/512/3143/3143640.png")
            )
        )
    }
    
    override fun getAllCategories(): Flow<List<Category>> = flow {
        emit(categoryList)
    }
    
    override fun getCategoryById(id: Int): Flow<Category?> = flow {
        emit(categoryList.find { it.id == id })
    }
}
