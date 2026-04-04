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
class DetailViewModel @Inject constructor(
    private val getFoodUseCase: GetFoodUseCase
) : ViewModel() {
    
    private val _food = MutableStateFlow<Food?>(null)
    val food: StateFlow<Food?> = _food.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadFood(foodId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getFoodUseCase.getFoodById(foodId).collect { foodItem ->
                    _food.value = foodItem
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
