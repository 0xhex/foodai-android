package com.codepad.foodai.ui.home.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.models.user.WeightLogData
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.user.GetUserDataUseCase
import com.codepad.foodai.domain.use_cases.user.GetUserWeightLogsUseCase
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getUserWeightLogsUseCase: GetUserWeightLogsUseCase,
    private val updateUserFieldUseCase: UpdateUserFieldUseCase,
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    private val _weightLogs = MutableLiveData<List<WeightLogData>>()
    val weightLogs: LiveData<List<WeightLogData>> = _weightLogs

    private val _weightUpdateSuccess = MutableLiveData<Event<Boolean>>()
    val weightUpdateSuccess: LiveData<Event<Boolean>> = _weightUpdateSuccess

    fun fetchUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            UserSession.user?.id?.let { userId ->
                when (val result = getUserDataUseCase.getUserData(userId)) {
                    is UseCaseResult.Success -> {
                        _userData.value = result.data
                    }

                    is UseCaseResult.Error -> {
                        // Handle error
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun fetchWeightLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            UserSession.user?.id?.let { userId ->
                when (val result = getUserWeightLogsUseCase.getUserWeightLogs(userId)) {
                    is UseCaseResult.Success -> {
                        _weightLogs.value = result.data
                    }

                    is UseCaseResult.Error -> {
                        // Handle error
                    }
                }
            }
            _isLoading.value = false
        }
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
                        _weightUpdateSuccess.value = Event(true)
                        fetchUserData()
                        fetchWeightLogs()
                    }

                    is UseCaseResult.Error -> {
                        _weightUpdateSuccess.value = Event(false)
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun updateTargetWeight(weight: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            UserSession.user?.id?.let { userId ->
                when (val result = updateUserFieldUseCase.updateUserFields(
                    userID = userId,
                    fieldName = "targetWeight",
                    fieldValue = weight.toString()
                )) {
                    is UseCaseResult.Success -> {
                        _weightUpdateSuccess.value = Event(true)
                        fetchUserData()
                    }

                    is UseCaseResult.Error -> {
                        _weightUpdateSuccess.value = Event(false)
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun calculateBMI(): Double {
        val user = userData.value ?: return 0.0
        val weight = user.weight ?: return 0.0
        val height = user.height ?: return 0.0

        if (height <= 0) return 0.0

        val heightInMeters = height / 100
        val weightInKg = if (user.isMetric == true) weight else weight * 0.45359237
        return weightInKg / (heightInMeters * heightInMeters)
    }

    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Healthy"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }

    fun getWeightUnit(): String {
        return if (userData.value?.isMetric == true) "kg" else "lb"
    }

    fun calculateCompletedRatio(): Double {
        val user = userData.value ?: return 0.0
        val startingWeight = weightLogs.value?.lastOrNull()?.weight ?: return 0.0
        val currentWeight = user.weight ?: return 0.0
        val targetWeight = user.targetWeight?.toDouble() ?: return 0.0

        // Convert weights to kg if using imperial
        val startingWeightKg =
            if (user.isMetric == true) startingWeight else startingWeight * 0.45359237
        val currentWeightKg =
            if (user.isMetric == true) currentWeight else currentWeight * 0.45359237
        val targetWeightKg = if (user.isMetric == true) targetWeight else targetWeight * 0.45359237

        // Calculate progress ratio
        val totalChange = targetWeightKg - startingWeightKg
        val currentChange = currentWeightKg - startingWeightKg

        return if (totalChange != 0.0) {
            (currentChange / totalChange).coerceIn(0.0, 1.0) * 100
        } else {
            0.0
        }
    }
} 