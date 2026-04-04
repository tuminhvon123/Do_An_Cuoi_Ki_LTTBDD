package com.example.appfood.di

import com.example.appfood.data.repository.CategoryRepositoryImpl
import com.example.appfood.data.repository.FoodRepositoryImpl
import com.example.appfood.domain.repository.CategoryRepository
import com.example.appfood.domain.repository.FoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        foodRepositoryImpl: FoodRepositoryImpl
    ): FoodRepository
    
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository
}
