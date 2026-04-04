package com.example.appfood.data.repository

import com.example.appfood.domain.model.Food
import com.example.appfood.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor() : FoodRepository {
    
    private val foodList = mutableListOf<Food>()
    
    init {
        initMockData()
    }
    
    private fun initMockData() {
        foodList.addAll(
            listOf(
                Food("1", "Pizza Hải Sản", "Pizza hải sản cao cấp với tôm và mực tươi.", 150000.0, "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500&auto=format", 1, false, "Thêm phô mai, Đế dày"),
                Food("2", "Burger Bò", "Burger bò Mỹ với phô mai tan chảy.", 55000.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format", 2, false, "Thêm thịt, Ít rau"),
                Food("3", "Coca Cola", "Nước giải khát Coca Cola mát lạnh.", 15000.0, "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500&auto=format", 3, true, ""),
                Food("4", "Gà Rán", "Gà rán giòn tan chuẩn vị KFC.", 35000.0, "https://images.unsplash.com/photo-1562967914-608f82629710?w=500&auto=format", 4, false, "Thêm tương ớt"),
                Food("5", "Pizza Phô Mai", "Nhiều phô mai kéo sợi cực hấp dẫn.", 120000.0, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format", 1, false, "")
            )
        )
    }
    
    override fun getAllFoods(): Flow<List<Food>> = flow {
        emit(foodList)
    }
    
    override fun getFoodById(id: String): Flow<Food?> = flow {
        emit(foodList.find { it.id == id })
    }
    
    override fun getFoodsByCategory(categoryId: Int): Flow<List<Food>> = flow {
        emit(foodList.filter { it.categoryId == categoryId })
    }
    
    override fun searchFoods(query: String): Flow<List<Food>> = flow {
        emit(foodList.filter { it.title.contains(query, ignoreCase = true) })
    }
}
