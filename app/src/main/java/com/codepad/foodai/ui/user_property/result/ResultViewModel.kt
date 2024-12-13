// ResultViewModel.kt
package com.codepad.foodai.ui.user_property.result

import android.os.UserManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.nutrition.GetUserNutritionUseCase
import com.codepad.foodai.domain.use_cases.user.GetUserDataUseCase
import com.codepad.foodai.helpers.ModelPreferencesManager
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val nutritionsUseCase: GetUserNutritionUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
) : ViewModel() {

    private val _calories = MutableLiveData(0)
    val calories: LiveData<Int> get() = _calories

    private val _carbs = MutableLiveData(0)
    val carbs: LiveData<Int> get() = _carbs

    private val _protein = MutableLiveData(0)
    val protein: LiveData<Int> get() = _protein

    private val _fats = MutableLiveData(0)
    val fats: LiveData<Int> get() = _fats

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchNutrition() {
        val userID = UserSession.user?.id ?: return
        _isLoading.value = true

        viewModelScope.launch {
            when (val result = nutritionsUseCase.getUserNutrition(userID)) {
                is UseCaseResult.Success -> {
                    _calories.value = result.data.totalCalories
                    _carbs.value = result.data.carbohydrates
                    _protein.value = result.data.protein
                    _fats.value = result.data.fat
                    fetchUserData()
                    _isLoading.value = false
                }

                is UseCaseResult.Error -> {
                    _isLoading.value = false
                    result.exception?.let { handleRegistrationError(it) }
                }
            }
        }
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            when (val result = getUserDataUseCase.getUserData(UserSession.user?.id.orEmpty())) {
                is UseCaseResult.Success -> {
                    UserSession.updateSession(result.data)
                }

                is UseCaseResult.Error -> {
                    UserSession.clearSession()
                }
            }
        }
    }

    private fun handleRegistrationError(error: APIError) {
        _errorMessage.value = when (error) {
            is APIError.ServerError -> "${error.code}: ${error.message}"
            is APIError.UnknownError -> "An unknown error occurred. Please try again."
            is APIError.NetworkError -> "Network error: ${error.localizedMessage}"
            else -> "Failed to register. Please try again."
        }
    }

    fun navigateToNextScreen() {
        ModelPreferencesManager.put(true, "isUserPropertySet")
    }

}