package com.example.appfood.di

import com.example.appfood.domain.usecase.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // All use cases use @Inject constructor, 
    // so they don't need to be provided manually here.
}
