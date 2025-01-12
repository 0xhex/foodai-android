package com.codepad.foodai.ui.home.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.exercise.DeleteExerciseUseCase
import com.codepad.foodai.domain.use_cases.exercise.LogExerciseCustomUseCase
import com.codepad.foodai.domain.use_cases.exercise.SubmitExerciseDescriptionUseCase
import com.codepad.foodai.domain.use_cases.exercise.UpdateCustomExerciseUseCase
import com.codepad.foodai.domain.use_cases.exercise.UpdateExerciseDescriptionUseCase
import com.codepad.foodai.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val logExerciseCustomUseCase: LogExerciseCustomUseCase,
    private val submitExerciseDescriptionUseCase: SubmitExerciseDescriptionUseCase,
    private val updateCustomExerciseUseCase: UpdateCustomExerciseUseCase,
    private val updateExerciseDescriptionUseCase: UpdateExerciseDescriptionUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    private val _exerciseLogged = MutableLiveData<Event<Boolean>>()
    val exerciseLogged: LiveData<Event<Boolean>> = _exerciseLogged

    fun logExercise(userID: String, type: String, intensity: String, duration: Int) {
        if (userID.isEmpty()) {
            _error.value = Event("User ID is required")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
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

    fun submitExerciseDescription(userID: String, description: String) {
        if (userID.isEmpty()) {
            _error.value = Event("User ID is required")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val localDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                when (val result = submitExerciseDescriptionUseCase.submitDescription(
                    userID = userID,
                    exerciseDescription = description,
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

    fun updateCustomExercise(exerciseID: String, type: String, intensity: String, duration: Int) {
        if (exerciseID.isEmpty()) {
            _error.value = Event("Invalid exercise ID")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = updateCustomExerciseUseCase.updateCustomExercise(exerciseID, type, intensity, duration)) {
                    is UseCaseResult.Success -> {
                        android.util.Log.d("ExerciseViewModel", "updateCustomExercise: Success")
                        _exerciseLogged.value = Event(true)
                    }
                    is UseCaseResult.Error -> {
                        android.util.Log.d("ExerciseViewModel", "updateCustomExercise: Error - ${result.message}")
                        _error.value = Event(result.message)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("ExerciseViewModel", "updateCustomExercise: Exception - ${e.message}")
                _error.value = Event(e.message ?: "An error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExerciseDescription(exerciseID: String, description: String) {
        if (exerciseID.isEmpty()) {
            _error.value = Event("Invalid exercise ID")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = updateExerciseDescriptionUseCase.updateDescription(exerciseID, description)) {
                    is UseCaseResult.Success -> {
                        android.util.Log.d("ExerciseViewModel", "updateExerciseDescription: Success")
                        _exerciseLogged.value = Event(true)
                    }
                    is UseCaseResult.Error -> {
                        android.util.Log.d("ExerciseViewModel", "updateExerciseDescription: Error - ${result.message}")
                        _error.value = Event(result.message)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("ExerciseViewModel", "updateExerciseDescription: Exception - ${e.message}")
                _error.value = Event(e.message ?: "An error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExercise(exerciseID: String) {
        if (exerciseID.isEmpty()) {
            _error.value = Event("Invalid exercise ID")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = deleteExerciseUseCase.deleteExercise(exerciseID)) {
                    is UseCaseResult.Success -> {
                        android.util.Log.d("ExerciseViewModel", "deleteExercise: Success")
                        _exerciseLogged.value = Event(true)
                    }
                    is UseCaseResult.Error -> {
                        android.util.Log.d("ExerciseViewModel", "deleteExercise: Error - ${result.message}")
                        _error.value = Event(result.message)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("ExerciseViewModel", "deleteExercise: Exception - ${e.message}")
                _error.value = Event(e.message ?: "An error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 