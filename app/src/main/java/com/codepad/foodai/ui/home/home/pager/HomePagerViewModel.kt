package com.codepad.foodai.ui.home.home.pager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.models.user.GetUserDailySummaryUseCase
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.user.DailySummaryResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePagerViewModel @Inject constructor(
    private val getUserDailySummaryUseCase: GetUserDailySummaryUseCase,
) : ViewModel() {

    private val _dailySummary = MutableLiveData<DailySummaryResponseData>()
    val dailySummary: LiveData<DailySummaryResponseData> get() = _dailySummary

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