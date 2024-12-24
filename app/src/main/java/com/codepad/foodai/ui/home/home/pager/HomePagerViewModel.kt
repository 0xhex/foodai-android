package com.codepad.foodai.ui.home.home.pager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.models.nutrition.NutritionResponseData
import com.codepad.foodai.domain.models.user.GetUserDailySummaryUseCase
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.user.DailySummaryResponseData
import com.codepad.foodai.domain.use_cases.user.NutritionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePagerViewModel @Inject constructor(
    private val getUserDailySummaryUseCase: GetUserDailySummaryUseCase,
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

    fun updateAchievedPercents(nutritionResponseData: NutritionResponseData) {
        resetAchievedPercents()
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
}