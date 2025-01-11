package com.codepad.foodai.ui.streak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codepad.foodai.domain.models.user.StreakResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DailyStreakViewModel @Inject constructor() : ViewModel() {
    private val _currentStreak = MutableLiveData<Int>()
    val currentStreak: LiveData<Int> = _currentStreak

    private val _selectedDays = MutableLiveData<List<Boolean>>()
    val selectedDays: LiveData<List<Boolean>> = _selectedDays

    private val daySymbols = listOf("S", "M", "T", "W", "T", "F", "S")

    fun setStreakData(streakData: StreakResponseData) {
        _currentStreak.value = streakData.currentStreak
        _selectedDays.value = streakData.dailyStatus.values.toList()
    }

    fun getDaySymbol(index: Int): String = daySymbols[index]

    fun onContinueClicked() {
        // Handle continue button click
    }
} 