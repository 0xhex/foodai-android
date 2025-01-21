package com.codepad.foodai.ui.streak

import android.icu.text.DateFormatSymbols
import android.icu.util.ULocale
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

    private val locale: ULocale = ULocale.getDefault()
    private val dayFormatter = DateFormatSymbols.getInstance(locale)
    private val _selectedDays = MutableLiveData<List<Boolean>>()
    val selectedDays: LiveData<List<Boolean>> = _selectedDays

    // Skip the first empty element and take only the weekdays (indices 1-7)
    private val daySymbols = dayFormatter.weekdays.drop(1).take(7).map { it.substring(0,1).uppercase() }

    fun setStreakData(streakData: StreakResponseData) {
        _currentStreak.value = streakData.currentStreak
        _selectedDays.value = streakData.dailyStatus.values.toList()
    }

    fun getDaySymbol(index: Int): String = daySymbols[index]

    fun onContinueClicked() {
        // Handle continue button click
    }
} 