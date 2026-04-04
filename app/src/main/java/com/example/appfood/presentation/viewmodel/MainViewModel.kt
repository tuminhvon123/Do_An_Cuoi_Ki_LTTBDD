package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.Food
import com.example.appfood.domain.usecase.GetFoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getFoodUseCase: GetFoodUseCase
) : ViewModel() {
    
    private val _foods = MutableStateFlow<List<Food>>(emptyList())
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()
    
    private val _filteredFoods = MutableStateFlow<List<Food>>(emptyList())
    val filteredFoods: StateFlow<List<Food>> = _filteredFoods.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadFoods()
    }
    
    private fun loadFoods() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getFoodUseCase.getAllFoods().collect { foodList ->
                    _foods.value = foodList
                    _filteredFoods.value = foodList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun searchFoods(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _filteredFoods.value = _foods.value
                } else {
                    getFoodUseCase.searchFoods(query).collect { result ->
                        _filteredFoods.value = result
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun filterByCategory(categoryId: Int) {
        viewModelScope.launch {
            try {
                if (categoryId == 0) { // "Tất cả" category
                    _filteredFoods.value = _foods.value
                } else {
                    getFoodUseCase.getFoodsByCategory(categoryId).collect { result ->
                        _filteredFoods.value = result
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
