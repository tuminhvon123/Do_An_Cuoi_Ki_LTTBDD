package com.example.appfood.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // GetFoodUseCase and GetCategoryUseCase use @Inject constructor, 
    // so they don't need to be provided manually here.
}
