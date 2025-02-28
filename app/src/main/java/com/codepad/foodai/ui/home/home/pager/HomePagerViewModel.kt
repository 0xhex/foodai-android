package com.codepad.foodai.ui.home.home.pager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.domain.models.note.DailyNote
import com.codepad.foodai.domain.models.nutrition.NutritionResponseData
import com.codepad.foodai.domain.models.recommendation.Recommendation
import com.codepad.foodai.domain.models.user.GetUserDailySummaryUseCase
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.community.CreateCommunityPostUseCase
import com.codepad.foodai.domain.use_cases.image.DeleteImageUseCase
import com.codepad.foodai.domain.use_cases.image.FixImageResultsUseCase
import com.codepad.foodai.domain.use_cases.recommendation.GetRecommendationUseCase
import com.codepad.foodai.domain.use_cases.recommendation.RequestRecommendationsUseCase
import com.codepad.foodai.domain.use_cases.user.DailySummaryResponseData
import com.codepad.foodai.domain.use_cases.user.GetUserWeightLogsUseCase
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.extensions.toFormattedString
import com.codepad.foodai.helpers.HealthConnectStatus
import com.codepad.foodai.helpers.NotesManager
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.home.settings.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomePagerViewModel @Inject constructor(
    private val getUserDailySummaryUseCase: GetUserDailySummaryUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val fixImageResultsUseCase: FixImageResultsUseCase,
    private val requestRecommendationsUseCase: RequestRecommendationsUseCase,
    private val getRecommendationUseCase: GetRecommendationUseCase,
    private val getUserWeightLogsUseCase: GetUserWeightLogsUseCase,
    private val updateUserFieldUseCase: UpdateUserFieldUseCase,
    private val sharedPreferences: SharedPreferences,
    private val notesManager: NotesManager,
    private val createCommunityPostUseCase: CreateCommunityPostUseCase,
) : ViewModel() {

    private val _dailySummary = MutableLiveData<DailySummaryResponseData>()
    val dailySummary: LiveData<DailySummaryResponseData> get() = _dailySummary

    private val _calorieAchievedPercent = MutableLiveData(0)
    val calorieAchievedPercent: LiveData<Int> get() = _calorieAchievedPercent

    private val _proteinAchievedPercent = MutableLiveData(0)
    val proteinAchievedPercent: LiveData<Int> get() = _proteinAchievedPercent

    private val _carbsAchievedPercent = MutableLiveData(0)
    val carbsAchievedPercent: LiveData<Int> get() = _carbsAchievedPercent

    private val _fatsAchievedPercent = MutableLiveData(0)
    val fatsAchievedPercent: LiveData<Int> get() = _fatsAchievedPercent

    private val _foodDetail = MutableLiveData<ImageData>()
    val foodDetail: LiveData<ImageData> get() = _foodDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _deleteResult = MutableLiveData<Boolean?>()
    val deleteResult: LiveData<Boolean?> get() = _deleteResult

    private val _fixResult = MutableLiveData<Boolean?>()
    val fixResult: LiveData<Boolean?> = _fixResult

    private val _recommendationId = MutableLiveData<String?>()
    val recommendationId: MutableLiveData<String?> = _recommendationId

    private val _recommendation = MutableLiveData<Recommendation?>()
    val recommendation: MutableLiveData<Recommendation?> = _recommendation

    private val _recommendationError = MutableLiveData<APIError?>()
    val recommendationError: MutableLiveData<APIError?> = _recommendationError

    private val _waterIntake = MutableLiveData<Int>()
    val waterIntake: LiveData<Int> = _waterIntake

    private val _targetWaterIntake = MutableLiveData<Double>()
    val targetWaterIntake: LiveData<Double> = _targetWaterIntake

    private val _currentSteps = MutableLiveData<Int>()
    val currentSteps: LiveData<Int> = _currentSteps

    private val _targetSteps = MutableLiveData<Int>()
    val targetSteps: LiveData<Int> = _targetSteps

    private val _stepsDistance = MutableLiveData<Double>()
    val stepsDistance: LiveData<Double> = _stepsDistance

    private val _stepsBurnedCalories = MutableLiveData<Int>()
    val stepsBurnedCalories: LiveData<Int> = _stepsBurnedCalories

    private val _healthConnectStatus = MutableLiveData<HealthConnectStatus>()
    val healthConnectStatus: LiveData<HealthConnectStatus> = _healthConnectStatus

    private val _currentNote = MutableLiveData<DailyNote?>()
    val currentNote: LiveData<DailyNote?> = _currentNote

    private val _showWeightUpdateBanner = MutableLiveData<Boolean>()
    val showWeightUpdateBanner: LiveData<Boolean> = _showWeightUpdateBanner

    private val _createPostResult = MutableLiveData<CommunityPost>()
    val createPostResult: LiveData<CommunityPost> = _createPostResult

    private fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(dateString)
    }

    fun updateAchievedPercents(nutritionResponseData: NutritionResponseData) {
        val totalCalories = nutritionResponseData.totalCalories.toFloat()
        val totalProtein = nutritionResponseData.protein.toFloat()
        val totalCarbs = nutritionResponseData.carbohydrates.toFloat()
        val totalFats = nutritionResponseData.fat.toFloat()

        val remainingNutritionData = _dailySummary.value?.remainingNutrition
        val remainingCalories = remainingNutritionData?.calories ?: 0
        val remainingProtein = remainingNutritionData?.protein ?: 0
        val remainingCarbs = remainingNutritionData?.carbs ?: 0
        val remainingFats = remainingNutritionData?.fat ?: 0

        val calorieAchievedPercent = ((totalCalories - remainingCalories) / totalCalories) * 100
        val proteinAchievedPercent = ((totalProtein - remainingProtein) / totalProtein) * 100
        val carbsAchievedPercent = ((totalCarbs - remainingCarbs) / totalCarbs) * 100
        val fatsAchievedPercent = ((totalFats - remainingFats) / totalFats) * 100

        _calorieAchievedPercent.value = calorieAchievedPercent.toInt()
        _proteinAchievedPercent.value = proteinAchievedPercent.toInt()
        _carbsAchievedPercent.value = carbsAchievedPercent.toInt()
        _fatsAchievedPercent.value = fatsAchievedPercent.toInt()
    }

    fun resetAchievedPercents() {
        _calorieAchievedPercent.value = 0
        _proteinAchievedPercent.value = 0
        _carbsAchievedPercent.value = 0
        _fatsAchievedPercent.value = 0
    }

    fun fetchDailySummary(userId: String, date: String) {
        viewModelScope.launch {
            when (val result = getUserDailySummaryUseCase.getUserDailySummary(userId, date)) {
                is UseCaseResult.Success -> {
                    _dailySummary.value = result.data
                }

                is UseCaseResult.Error -> {
                    // Handle error
                }
            }
        }
    }

    fun clearRecommendationData() {
        _recommendationId.value = null
        _recommendation.value = null
        _recommendationError.value = null
    }

    fun setFoodDetail(foodDetail: ImageData) {
        // Clear previous recommendation data when food detail changes
        clearRecommendationData()
        _foodDetail.value = foodDetail
    }

    fun deleteImage(imageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (deleteImageUseCase.deleteImage(imageId)) {
                is UseCaseResult.Success -> {
                    _deleteResult.value = true
                    _isLoading.value = false
                }

                is UseCaseResult.Error -> {
                    _deleteResult.value = false
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    fun fixImageResults(imageId: String, prompt: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = fixImageResultsUseCase.fixImageResults(imageId, prompt)) {
                is UseCaseResult.Success -> {
                    _fixResult.value = true
                    _foodDetail.value = result.data
                }

                is UseCaseResult.Error -> {
                    _fixResult.value = false
                }
            }
            _isLoading.value = false
        }
    }

    fun clearFixResult() {
        _fixResult.value = null
    }

    fun requestRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            foodDetail.value?.id?.let { imageId ->
                when (val result = requestRecommendationsUseCase.requestRecommendations(imageId)) {
                    is UseCaseResult.Success -> {
                        _recommendationId.value = result.data.recommendationId
                    }

                    is UseCaseResult.Error -> {
                        _recommendationError.value = result.exception
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun getRecommendationdata() {
        viewModelScope.launch {
            recommendationId.value?.let { id ->
                when (val result = getRecommendationUseCase.getRecommendation(id)) {
                    is UseCaseResult.Success -> {
                        _recommendation.value = result.data
                    }

                    is UseCaseResult.Error -> {
                        _recommendationError.value = result.exception
                    }
                }
            }
        }
    }

    fun calculateTargetWaterIntake(user: User) {
        var baseTarget = 2.5

        user.weight?.let { weight ->
            val calculatedTarget = weight * 0.033
            baseTarget = maxOf(baseTarget, calculatedTarget)
        }

        when (user.goal?.lowercase()) {
            "lose_weight" -> baseTarget += 0.5
            "gain_weight" -> baseTarget += 0.2
        }

        user.dateOfBirth?.let { dobString ->
            parseDate(dobString)?.let { dob ->
                val age = calculateAge(dob)
                if (age > 50) baseTarget -= 0.2
            }
        }

        user.workoutsPerWeek?.toDoubleOrNull()?.let { workouts ->
            baseTarget += workouts * 0.15
        }

        _targetWaterIntake.value = baseTarget
    }

    fun getWaterIntakeForDate(date: Date) {
        val key = "waterIntake_${date.toFormattedString("yyyy-MM-dd")}"
        _waterIntake.value = sharedPreferences.getInt(key, 0)
    }

    fun incrementWaterIntake(date: Date) {
        val key = "waterIntake_${date.toFormattedString("yyyy-MM-dd")}"
        val current = _waterIntake.value ?: 0
        _waterIntake.value = current + 1
        sharedPreferences.edit().putInt(key, current + 1).apply()
    }

    fun calculateTargetSteps(user: User) {
        var baseGoal = 7000

        user.dateOfBirth?.let { dobString ->
            parseDate(dobString)?.let { birthDate ->
                val age = calculateAge(birthDate)
                when {
                    age < 30 -> baseGoal += 1000
                    age > 50 -> baseGoal -= 500
                }
            }
        }

        user.workoutsPerWeek?.toIntOrNull()?.let { workouts ->
            baseGoal += workouts * 500
        }

        when (user.goal?.lowercase()) {
            "lose_weight" -> baseGoal += 1000
            "gain_weight" -> baseGoal -= 500
        }

        _targetSteps.value = baseGoal
    }

    fun updateStepsData(steps: Int) {
        _currentSteps.value = steps
        _stepsDistance.value = steps * 0.0008 // Each step is approximately 0.8 meters
        _stepsBurnedCalories.value = (steps * 0.05).toInt()
    }

    fun initHealthConnect(healthConnectManager: HealthConnectManager) {
        healthConnectManager.onGoogleFitBodyDataRead = { stepData ->
            val (currentDaySteps, previousDaySteps) = stepData
            viewModelScope.launch {
                updateStepsData(currentDaySteps.firstOrNull() ?: 0)
            }
        }
    }

    fun isHealthConnectSupported(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
    }

    fun isHealthConnectSDKAvailable(context: Context): Boolean {
        val availabilityStatus = HealthConnectClient.getSdkStatus(
            context,
            "com.google.android.apps.healthdata"
        )
        return availabilityStatus == HealthConnectClient.SDK_AVAILABLE
    }

    fun requestHealthConnect(healthConnectManager: HealthConnectManager) {
        healthConnectManager.readData()
    }

    fun checkHealthConnectStatus(
        healthConnectManager: HealthConnectManager,
        onConnected: () -> Unit
    ) {
        healthConnectManager.hasUnlockedIntegration { isConnected ->
            if (isConnected) {
                _healthConnectStatus.value = HealthConnectStatus.CONNECTED
                healthConnectManager.readData()
                onConnected()
            } else {
                _healthConnectStatus.value = HealthConnectStatus.NOT_CONNECTED
            }
        }
    }

    private fun calculateAge(birthDate: Date): Int {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        val birthCalendar = Calendar.getInstance()
        birthCalendar.time = birthDate

        var age = calendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (calendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    fun getNote(date: Date): DailyNote? {
        return notesManager.fetchNote(date)
    }

    fun loadNoteForDate(date: Date) {
        viewModelScope.launch {
            try {
                val note = notesManager.fetchNote(date)
                _currentNote.value = note
            } catch (e: Exception) {
                _currentNote.value = null
            }
        }
    }

    fun saveNote(date: Date, noteText: String, mood: String) {
        val note = DailyNote(
            keyDate = DailyNote.dateKeyString(date),
            noteText = noteText,
            mood = mood
        )
        notesManager.saveNote(note)
        _currentNote.value = note
    }

    fun updateWeight(weight: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            UserSession.user?.id?.let { userId ->
                when (val result = updateUserFieldUseCase.updateUserFields(
                    userID = userId,
                    fieldName = "weight",
                    fieldValue = weight.toString()
                )) {
                    is UseCaseResult.Success -> {
                        // Update local user
                        val updatedUser = UserSession.user?.copy(weight = weight)
                        updatedUser?.let { UserSession.updateSession(it) }

                        // Check if we should show banner
                        checkWeightUpdateBanner()
                    }

                    is UseCaseResult.Error -> {
                        // Handle error
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun checkWeightUpdateBanner() {
        viewModelScope.launch {
            UserSession.user?.id?.let { userId ->
                when (val result = getUserWeightLogsUseCase.getUserWeightLogs(userId)) {
                    is UseCaseResult.Success -> {
                        val logs = result.data
                        val sevenDaysAgo = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -7)
                        }.time

                        _showWeightUpdateBanner.value = when {
                            logs.isEmpty() -> true
                            logs.firstOrNull()?.date!! < sevenDaysAgo -> true
                            else -> false
                        }
                    }

                    is UseCaseResult.Error -> {
                        // Handle error
                    }
                }
            }
        }
    }

    fun createCommunityPost(imageId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            UserSession.user?.id?.let { userId ->
                when (val result = createCommunityPostUseCase.createPost(userId, imageId)) {
                    is UseCaseResult.Success -> {
                        _createPostResult.value = result.data
                    }

                    is UseCaseResult.Error -> {

                    }
                }
            }

            _isLoading.value = false
        }
    }
}