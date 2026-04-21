package com.example.appfood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfood.domain.model.Food
import com.example.appfood.domain.model.FoodRating
import com.example.appfood.domain.repository.FoodRatingRepository
import com.example.appfood.domain.usecase.GetFoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getFoodUseCase: GetFoodUseCase,
    private val foodRatingRepository: FoodRatingRepository
) : ViewModel() {

    private val _food = MutableStateFlow<Food?>(null)
    val food: StateFlow<Food?> = _food.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ratings = MutableStateFlow<List<FoodRating>>(emptyList())
    val ratings: StateFlow<List<FoodRating>> = _ratings.asStateFlow()

    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    private val _ratingCount = MutableStateFlow(0)
    val ratingCount: StateFlow<Int> = _ratingCount.asStateFlow()

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

    fun loadFoodRatings(foodId: String) {
        viewModelScope.launch {
            try {
                foodRatingRepository.getRatingsByFoodId(foodId).collect { ratingList ->
                    _ratings.value = ratingList
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadAverageRating(foodId: String) {
        viewModelScope.launch {
            try {
                foodRatingRepository.getAverageRating(foodId).collect { avg ->
                    _averageRating.value = avg
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadRatingCount(foodId: String) {
        viewModelScope.launch {
            try {
                foodRatingRepository.getRatingCount(foodId).collect { count ->
                    _ratingCount.value = count
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
