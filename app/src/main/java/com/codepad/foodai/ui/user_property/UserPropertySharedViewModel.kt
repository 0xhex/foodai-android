package com.codepad.foodai.ui.user_property

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPropertySharedViewModel @Inject constructor(
    private val updateUserFieldUseCase: UpdateUserFieldUseCase,
) : ViewModel() {

    private val _selectedGender = MutableLiveData<String?>()
    val selectedGender: LiveData<String?> get() = _selectedGender

    private val _showWarning = MutableLiveData<Boolean>()
    val showWarning: LiveData<Boolean> get() = _showWarning

    private val _isNextEnabled = MutableLiveData<Boolean>()
    val isNextEnabled: LiveData<Boolean> get() = _isNextEnabled

    private val _selectedWorkout = MutableLiveData<String?>()
    val selectedWorkout: LiveData<String?> get() = _selectedWorkout

    fun selectGender(gender: String) {
        _selectedGender.value = gender
        _isNextEnabled.value = true
    }

    fun selectWorkout(workout: String) {
        _selectedWorkout.value = workout
        _isNextEnabled.value = true
    }

    fun onNextClicked(currentStep: Int) {
        when (currentStep) {
            1 -> {
                if (_selectedGender.value == null) {
                    _showWarning.value = true
                } else {
                    updateGender()
                }
            }
            2 -> {
                if (_selectedWorkout.value == null) {
                    _showWarning.value = true
                } else {
                    updateWorkout()
                }
            }
        }
    }

    private fun updateGender() {
        val gender = _selectedGender.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            val result = updateUserFieldUseCase.updateUserFields(userID, "gender", gender)
            // Handle the result (e.g., navigate to the next screen)
        }
    }

    private fun updateWorkout() {
        val workout = _selectedWorkout.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            val result = updateUserFieldUseCase.updateUserFields(userID, "workoutsPerWeek", workout)
            // Handle the result (e.g., navigate to the next screen)
        }
    }
}