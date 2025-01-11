package com.codepad.foodai.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.ErrorCode
import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.domain.models.image.ImageUploadResponse
import com.codepad.foodai.domain.models.nutrition.NutritionResponseData
import com.codepad.foodai.domain.models.recipe.Recipe
import com.codepad.foodai.domain.models.user.StreakResponseData
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.image.FetchImageUseCase
import com.codepad.foodai.domain.use_cases.image.UploadImageUseCase
import com.codepad.foodai.domain.use_cases.nutrition.GetUserNutritionUseCase
import com.codepad.foodai.domain.use_cases.recipe.GenerateRecipeUseCase
import com.codepad.foodai.domain.use_cases.recipe.GetRecipeStatusUseCase
import com.codepad.foodai.domain.use_cases.user.GetUserDataUseCase
import com.codepad.foodai.domain.use_cases.user.GetUserStreakUseCase
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.user_property.result.Nutrition
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val nutritionsUseCase: GetUserNutritionUseCase,
    private val updateUserFieldUseCase: UpdateUserFieldUseCase,
    private val resourceHelper: ResourceHelper,
    private val uploadImageUseCase: UploadImageUseCase,
    private val fetchImageUseCase: FetchImageUseCase,
    private val dailyStreakUseCase: GetUserStreakUseCase,
    private val generateRecipeUseCase: GenerateRecipeUseCase,
    private val getRecipeStatusUseCase: GetRecipeStatusUseCase,
) : ViewModel() {

    private val _homeEvent = MutableLiveData<HomeEvent>()
    val homeEvent: LiveData<HomeEvent> get() = _homeEvent

    private val _userDataResponse = MutableLiveData<User?>()
    val userDataResponse: LiveData<User?> get() = _userDataResponse

    private val _nutritions = MutableLiveData<NutritionResponseData>()
    val nutritions: LiveData<NutritionResponseData> get() = _nutritions

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

    private val _dailyStreak = MutableLiveData<StreakResponseData?>()
    val dailyStreak: LiveData<StreakResponseData?> get() = _dailyStreak

    private val _recipes = MutableLiveData<Map<String, Recipe>>(emptyMap())
    val recipes: LiveData<Map<String, Recipe>> = _recipes

    private val _isRecipeLoading = MutableLiveData<Boolean>()
    val isRecipeLoading: LiveData<Boolean> = _isRecipeLoading

    private val _recipeError = MutableLiveData<String?>()
    val recipeError: LiveData<String?> = _recipeError

    private val _isPremiumRequired = MutableLiveData<Boolean>()
    val isPremiumRequired: LiveData<Boolean> = _isPremiumRequired

    private var recipePollingJob: Job? = null

    private var shouldShowStreakView = false

    fun fetchUserData() {
        viewModelScope.launch {
            when (val result = getUserDataUseCase.getUserData(UserSession.user?.id.orEmpty())) {
                is UseCaseResult.Success -> {
                    _userDataResponse.value = result.data
                    UserSession.updateSession(result.data)
                    fetchDailyStreak()
                }

                is UseCaseResult.Error -> {
                    _userDataResponse.value = null
                    UserSession.clearSession()
                }
            }
        }
    }

    private fun fetchDailyStreak() {
        viewModelScope.launch {
            val userId = UserSession.user?.id ?: return@launch
            when (val result = dailyStreakUseCase.getUserStreak(userId)) {
                is UseCaseResult.Success -> {
                    _dailyStreak.value = result.data
                }

                is UseCaseResult.Error -> {
                    _dailyStreak.value = null
                }
            }
        }
    }

    fun shouldShowStreakView(): Boolean {
        return if (shouldShowStreakView) {
            shouldShowStreakView = false
            true
        } else {
            false
        }
    }

    fun fetchNutrition() {
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            when (val result = nutritionsUseCase.getUserNutrition(userID)) {
                is UseCaseResult.Success -> {
                    _nutritions.value = result.data
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
                    fetchImage(result.data.imageID, imageFile)
                }

                is UseCaseResult.Error -> {
                    _homeEvent.value = HomeEvent.OnImageUploadError(result.message)
                }
            }
        }
    }


    fun fetchImage(imageID: String, imageFile: File) {
        viewModelScope.launch {
            _homeEvent.value =
                HomeEvent.OnImageFetchStarted(BitmapFactory.decodeFile(imageFile.absolutePath))
            when (val result = fetchImageUseCase.fetchImage(imageID)) {
                is UseCaseResult.Success -> {
                    if (result.data.status == "completed") {
                        shouldShowStreakView = true
                        _homeEvent.value = HomeEvent.OnImageFetchSuccess(result.data)
                    } else if (result.data.status == "failed") {
                        _homeEvent.value = HomeEvent.OnImageFetchError("Image processing failed.")
                    } else {
                        delay(5000)
                        fetchImage(imageID, imageFile)
                    }
                }

                is UseCaseResult.Error -> {
                    _homeEvent.value = HomeEvent.OnImageFetchError(result.message)
                }
            }
        }
    }

    fun setOptionSelected(option: MenuOption) {
        _homeEvent.value = HomeEvent.OnMenuOptionSelected(option)
    }

    fun generateRecipe(mealType: String) {
        viewModelScope.launch {
            _isRecipeLoading.value = true
            _isPremiumRequired.value = false
            _recipeError.value = null

            val userId = UserSession.user?.id ?: return@launch

            when (val result = generateRecipeUseCase.generateRecipe(userId, mealType.lowercase())) {
                is UseCaseResult.Success -> {
                    startPollingRecipeStatus(result.data.recipeID, mealType)
                }
                is UseCaseResult.Error -> {
                    _isRecipeLoading.value = false
                    if (result.code.toString() == ErrorCode.PREMIUM_REQUIRED.toString()) {
                        _isPremiumRequired.value = true
                    } else {
                        _recipeError.value = result.message
                    }
                }
            }
        }
    }

    private fun startPollingRecipeStatus(recipeId: String, mealType: String) {
        recipePollingJob?.cancel()
        recipePollingJob = viewModelScope.launch {
            while (isActive) {
                when (val result = getRecipeStatusUseCase.getRecipeStatus(recipeId)) {
                    is UseCaseResult.Success -> {
                        when (result.data.status) {
                            "completed" -> {
                                updateRecipe(result.data)
                                saveRecipeToPrefs(result.data)
                                _isRecipeLoading.value = false
                                break
                            }
                            "failed" -> {
                                _isRecipeLoading.value = false
                                _recipeError.value = result.data.errorMessage ?: "Recipe generation failed"
                                break
                            }
                        }
                    }
                    is UseCaseResult.Error -> {
                        _isRecipeLoading.value = false
                        _recipeError.value = result.message
                        break
                    }
                }
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    private fun updateRecipe(recipe: Recipe) {
        val currentRecipes = _recipes.value?.toMutableMap() ?: mutableMapOf()
        currentRecipes[recipe.mealType] = recipe
        _recipes.value = currentRecipes
    }

    private fun saveRecipeToPrefs(recipe: Recipe) {
        viewModelScope.launch {
            try {
                val key = generateRecipeKey(recipe.mealType)
                val json = Gson().toJson(recipe)
                resourceHelper.getSharedPreferences().edit().putString(key, json).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkSavedRecipe(mealType: String) {
        viewModelScope.launch {
            try {
                val key = generateRecipeKey(mealType)
                val savedRecipe = resourceHelper.getSharedPreferences().getString(key, null)
                if (savedRecipe != null) {
                    val recipe = Gson().fromJson(savedRecipe, Recipe::class.java)
                    if (recipe.status == "completed") {
                        updateRecipe(recipe)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateRecipeKey(mealType: String): String {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormatter.format(Date())
        return "Recipe_${dateString}_$mealType"
    }

    override fun onCleared() {
        super.onCleared()
        recipePollingJob?.cancel()
    }

    sealed class HomeEvent {
        data class OnMenuOptionSelected(val option: MenuOption) : HomeEvent()
        data object OnImageUploadStarted : HomeEvent()
        data class OnImageUploadSuccess(val response: ImageUploadResponse) : HomeEvent()
        data class OnImageUploadError(val errorMessage: String) : HomeEvent()
        data class OnImageFetchStarted(val bitmap: Bitmap) : HomeEvent()
        data class OnImageFetchSuccess(val response: ImageData) : HomeEvent()
        data class OnImageFetchError(val errorMessage: String) : HomeEvent()
    }
}

enum class MenuOption {
    SCAN_FOOD, LOG_FOOD
}