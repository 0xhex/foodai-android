package com.codepad.foodai.ui.home.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.exercise.LogExerciseCustomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val logExerciseCustomUseCase: LogExerciseCustomUseCase
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _exerciseLogged = MutableLiveData<Boolean?>()
    val exerciseLogged: LiveData<Boolean?> = _exerciseLogged

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun logExercise(userID: String, type: String, intensity: String, duration: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _exerciseLogged.value = null
            
            // Get current date in required format
            val localDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            when (val result = logExerciseCustomUseCase.logExercise(
                userID = userID,
                type = type,
                intensity = intensity,
                duration = duration,
                localDate = localDate
            )) {
                is UseCaseResult.Success -> {
                    _exerciseLogged.value = true
                }
                is UseCaseResult.Error -> {
                    _error.value = result.message
                    _exerciseLogged.value = false
                }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearExerciseLogged() {
        _exerciseLogged.value = null
    }
} 