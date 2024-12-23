package com.codepad.foodai.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.image.ImageUploadResponse
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.image.UploadImageUseCase
import com.codepad.foodai.domain.use_cases.nutrition.GetUserNutritionUseCase
import com.codepad.foodai.domain.use_cases.user.GetUserDataUseCase
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.user_property.result.Nutrition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val nutritionsUseCase: GetUserNutritionUseCase,
    private val updateUserFieldUseCase: UpdateUserFieldUseCase,
    private val resourceHelper: ResourceHelper,
    private val uploadImageUseCase: UploadImageUseCase,
) : ViewModel() {
    private val _homeEvent = MutableLiveData<HomeEvent>()
    val homeEvent: LiveData<HomeEvent> get() = _homeEvent

    private val _userDataResponse = MutableLiveData<User?>()
    val userDataResponse: LiveData<User?> get() = _userDataResponse

    private val _calories = MutableLiveData<Nutrition>().apply {
        value = Nutrition("Calorie Goal", "0", R.drawable.kcal)
    }
    val calories: LiveData<Nutrition> get() = _calories

    private val _carbs = MutableLiveData<Nutrition>().apply {
        value = Nutrition("Carb Goal", "0", R.drawable.carbs)
    }
    val carbs: LiveData<Nutrition> get() = _carbs

    private val _protein = MutableLiveData<Nutrition>().apply {
        value = Nutrition("Protein Goal", "0", R.drawable.protein)
    }
    val protein: LiveData<Nutrition> get() = _protein

    private val _fats = MutableLiveData<Nutrition>().apply {
        value = Nutrition("Fat Goal", "0", R.drawable.fats)
    }
    val fats: LiveData<Nutrition> get() = _fats

    fun fetchUserData() {
        viewModelScope.launch {
            when (val result = getUserDataUseCase.getUserData(UserSession.user?.id.orEmpty())) {
                is UseCaseResult.Success -> {
                    _userDataResponse.value = result.data
                    UserSession.updateSession(result.data)
                }

                is UseCaseResult.Error -> {
                    _userDataResponse.value = null
                    UserSession.clearSession()
                }
            }
        }
    }

    fun fetchNutrition() {
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            when (val result = nutritionsUseCase.getUserNutrition(userID)) {
                is UseCaseResult.Success -> {
                    _calories.value = Nutrition(
                        "Calorie Goal", result.data.totalCalories.toString(), R.drawable.kcal
                    )
                    _carbs.value = Nutrition(
                        "Carb Goal", result.data.carbohydrates.toString(), R.drawable.carbs
                    )
                    _protein.value = Nutrition(
                        "Protein Goal", result.data.protein.toString(), R.drawable.protein
                    )
                    _fats.value = Nutrition(
                        "Fat Goal", result.data.fat.toString(), R.drawable.fats
                    )
                    fetchUserData()
                }

                is UseCaseResult.Error -> {
                }
            }
        }
    }

    fun saveGoals(calories: Int, carbs: Int, protein: Int, fats: Int) {
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "dailyCalories", calories.toString())
            updateUserFieldUseCase.updateUserFields(userID, "dailyCarb", carbs.toString())
            updateUserFieldUseCase.updateUserFields(userID, "dailyProtein", protein.toString())
            updateUserFieldUseCase.updateUserFields(userID, "dailyFat", fats.toString())
        }
    }

    fun uploadImage(userID: String, imageFile: File, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _homeEvent.value = HomeEvent.OnImageUploadStarted
            when (val result =
                uploadImageUseCase.uploadImage(userID, imageFile, fileName, mimeType)) {
                is UseCaseResult.Success -> {
                    _homeEvent.value = HomeEvent.OnImageUploadSuccess(result.data)
                }

                is UseCaseResult.Error -> {
                    _homeEvent.value = HomeEvent.OnImageUploadError(result.message)
                }
            }
        }
    }

    fun setOptionSelected(option: MenuOption) {
        _homeEvent.value = HomeEvent.OnMenuOptionSelected(option)
    }

    sealed class HomeEvent {
        data class OnMenuOptionSelected(val option: MenuOption) : HomeEvent()
        data object OnImageUploadStarted : HomeEvent()
        data class OnImageUploadSuccess(val response: ImageUploadResponse) : HomeEvent()
        data class OnImageUploadError(val errorMessage: String) : HomeEvent()

    }
}

enum class MenuOption {
    SCAN_FOOD, LOG_FOOD
}