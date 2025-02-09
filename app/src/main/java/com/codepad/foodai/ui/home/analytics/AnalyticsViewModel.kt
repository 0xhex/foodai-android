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
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getUserWeightLogsUseCase: GetUserWeightLogsUseCase,
    private val updateUserFieldUseCase: UpdateUserFieldUseCase
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    private val _weightLogs = MutableLiveData<List<WeightLogData>>()
    val weightLogs: LiveData<List<WeightLogData>> = _weightLogs

    private val _selectedTimeRange = MutableLiveData<TimeRange>(TimeRange.LAST_30_DAYS)
    val selectedTimeRange: LiveData<TimeRange> = _selectedTimeRange

    private val _filteredWeightLogs = MutableLiveData<List<WeightLogData>>()
    val filteredWeightLogs: LiveData<List<WeightLogData>> = _filteredWeightLogs

    private val _completedRatio = MutableLiveData<Int>()
    val completedRatio: LiveData<Int> = _completedRatio

    private val _weightUpdateSuccess = MutableLiveData<Event<Boolean>>()
    val weightUpdateSuccess: LiveData<Event<Boolean>> = _weightUpdateSuccess

    fun fetchUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            UserSession.user?.id?.let { userId ->
                when (val result = getUserDataUseCase.getUserData(userId)) {
                    is UseCaseResult.Success -> {
                        _userData.value = result.data
                        fetchWeightLogs(userId)
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

    fun fetchWeightLogs(userId: String) {
        viewModelScope.launch {
            when (val result = getUserWeightLogsUseCase.getUserWeightLogs(userId)) {
                is UseCaseResult.Success -> {
                    _weightLogs.value = result.data
                    filterWeightLogs()
                    updateCompletedRatio()
                }
                is UseCaseResult.Error -> {
                    // Handle error
                }
            }
        }
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        filterWeightLogs()
        updateCompletedRatio()
    }

    private fun filterWeightLogs() {
        val logs = _weightLogs.value ?: return
        val now = Calendar.getInstance()
        val startDate = Calendar.getInstance()

        when (_selectedTimeRange.value) {
            TimeRange.LAST_30_DAYS -> {
                startDate.add(Calendar.DAY_OF_MONTH, -30)
                _filteredWeightLogs.value = logs.filter { it.date?.after(startDate.time) == true }
            }
            TimeRange.LAST_6_MONTHS -> {
                startDate.add(Calendar.MONTH, -6)
                _filteredWeightLogs.value = groupWeightLogsByWeek(logs.filter { it.date?.after(startDate.time) == true })
            }
            TimeRange.LAST_1_YEAR -> {
                startDate.add(Calendar.YEAR, -1)
                _filteredWeightLogs.value = groupWeightLogsByMonth(logs.filter { it.date?.after(startDate.time) == true })
            }
            else -> _filteredWeightLogs.value = logs
        }
    }

    private fun groupWeightLogsByWeek(logs: List<WeightLogData>): List<WeightLogData> {
        return logs.groupBy { calendar ->
            calendar.date?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.WEEK_OF_YEAR)
            }
        }.mapNotNull { (_, weekLogs) ->
            weekLogs.maxByOrNull { it.date ?: Date(0) }
        }.sortedBy { it.date }
    }

    private fun groupWeightLogsByMonth(logs: List<WeightLogData>): List<WeightLogData> {
        return logs.groupBy { calendar ->
            calendar.date?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.MONTH)
            }
        }.mapNotNull { (_, monthLogs) ->
            monthLogs.maxByOrNull { it.date ?: Date(0) }
        }.sortedBy { it.date }
    }

    private fun calculateCompletedRatio(startingWeight: Double, currentWeight: Double, targetWeight: Double): Int {
        // Calculate progress ratio using inverse lerp
        val progressRatio = if (startingWeight != targetWeight) {
            (currentWeight - startingWeight) / (targetWeight - startingWeight)
        } else {
            0.0
        }

        // Clamp the ratio between 0 and 1, then convert to percentage
        val clampedRatio = progressRatio.coerceIn(0.0, 1.0)
        return (clampedRatio * 100).toInt()
    }

    private fun updateCompletedRatio() {
        val weightLogs = _filteredWeightLogs.value
        val userData = _userData.value

        if (weightLogs.isNullOrEmpty() || userData == null) {
            _completedRatio.value = 0
            return
        }

        val startingWeightRaw = weightLogs.lastOrNull()?.weight ?: return
        val currentWeightRaw = weightLogs.firstOrNull()?.weight ?: return
        val targetWeightRaw = userData.targetWeight?.toDouble() ?: return
        val isMetric = userData.isMetric ?: true

        // Adjust weights for unit system
        val startingWeight = if (isMetric) startingWeightRaw else startingWeightRaw / 2.20462
        val currentWeight = if (isMetric) currentWeightRaw else currentWeightRaw / 2.20462
        val targetWeight = if (isMetric) targetWeightRaw else targetWeightRaw / 2.20462

        _completedRatio.value = calculateCompletedRatio(startingWeight, currentWeight, targetWeight)
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
} 