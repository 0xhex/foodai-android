package com.codepad.foodai.ui.user_property.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.nutrition.GetUserNutritionUseCase
import com.codepad.foodai.domain.use_cases.user.GetUserDataUseCase
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.ModelPreferencesManager
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val nutritionsUseCase: GetUserNutritionUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val resourceHelper: ResourceHelper,
    private val updateUserFieldUseCase: UpdateUserFieldUseCase
) : ViewModel() {

    private val _calories =
        MutableLiveData<Nutrition>().apply { value = Nutrition("Calories", "0", R.color.black) }
    val calories: LiveData<Nutrition> get() = _calories

    private val _carbs =
        MutableLiveData<Nutrition>().apply { value = Nutrition("Carbs", "0", R.color.orange) }
    val carbs: LiveData<Nutrition> get() = _carbs

    private val _protein =
        MutableLiveData<Nutrition>().apply { value = Nutrition("Protein", "0", R.color.red) }
    val protein: LiveData<Nutrition> get() = _protein

    private val _fats =
        MutableLiveData<Nutrition>().apply { value = Nutrition("Fats", "0", R.color.blue_button) }
    val fats: LiveData<Nutrition> get() = _fats

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _updatedUser = MutableLiveData<Boolean>()
    val updatedUser: LiveData<Boolean> get() = _updatedUser

    fun fetchNutrition() {
        val userID = UserSession.user?.id ?: return
        _isLoading.value = true

        viewModelScope.launch {
            when (val result = nutritionsUseCase.getUserNutrition(userID)) {
                is UseCaseResult.Success -> {
                    _calories.value =
                        Nutrition(
                            "Calories",
                            result.data.totalCalories.toString(),
                            resourceHelper.getColor(R.color.black)
                        )
                    _carbs.value =
                        Nutrition(
                            "Carbs",
                            result.data.carbohydrates.toString(),
                            resourceHelper.getColor(R.color.orange)
                        )
                    _protein.value =
                        Nutrition(
                            "Protein",
                            result.data.protein.toString(),
                            resourceHelper.getColor(R.color.red)
                        )
                    _fats.value = Nutrition(
                        "Fats",
                        result.data.fat.toString(),
                        resourceHelper.getColor(R.color.blue_button)
                    )
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

    fun updateUserFields(userID: String, fieldName: String, fieldValue: String) = viewModelScope.launch {
        _isLoading.value = true
        when (val result = updateUserFieldUseCase.updateUserFields(userID, fieldName, fieldValue)) {
            is UseCaseResult.Success -> {
                _updatedUser.value = true
                _isLoading.value = false
            }
            is UseCaseResult.Error -> {
                _isLoading.value = false
                result.exception?.let { handleRegistrationError(it) }
            }
        }
    }

}