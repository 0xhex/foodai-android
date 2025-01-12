package com.codepad.foodai.ui.home.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.exercise.LogExerciseCustomUseCase
import com.codepad.foodai.utils.Event
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

    private val _exerciseLogged = MutableLiveData<Event<Boolean>>()
    val exerciseLogged: LiveData<Event<Boolean>> = _exerciseLogged

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun logExercise(userID: String, type: String, intensity: String, duration: Int) {
        if (userID.isEmpty()) {
            _error.value = Event("User ID is required")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
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
                        _exerciseLogged.value = Event(true)
                    }
                    is UseCaseResult.Error -> {
                        _error.value = Event(result.message)
                        _exerciseLogged.value = Event(false)
                    }
                }
            } catch (e: Exception) {
                _error.value = Event(e.message ?: "An unexpected error occurred")
                _exerciseLogged.value = Event(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 