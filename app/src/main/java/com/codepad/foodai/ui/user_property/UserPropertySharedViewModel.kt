package com.codepad.foodai.ui.user_property

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.user_property.heightweight.MeasurementUnit
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

    private val _height = MutableLiveData<Int>()
    val height: LiveData<Int> get() = _height

    private val _weight = MutableLiveData<Int>()
    val weight: LiveData<Int> get() = _weight

    private val _heightFT = MutableLiveData<Int>()
    val heightFT: LiveData<Int> get() = _heightFT

    private val _heightIN = MutableLiveData<Int>()
    val heightIN: LiveData<Int> get() = _heightIN

    private val _weightLB = MutableLiveData<Int>()
    val weightLB: LiveData<Int> get() = _weightLB

    private val _isHeightWeightSet = MutableLiveData<Boolean>()
    val isHeightWeightSet: LiveData<Boolean> get() = _isHeightWeightSet

    private val _measurementUnit = MutableLiveData<MeasurementUnit>()
    val measurementUnit: LiveData<MeasurementUnit> get() = _measurementUnit

    init {
        _height.value = 160
        _weight.value = 60
        _heightFT.value = 5
        _heightIN.value = 3
        _weightLB.value = 132
        _measurementUnit.value = MeasurementUnit.METRIC
        _isHeightWeightSet.value = false
    }

    fun selectGender(gender: String) {
        _selectedGender.value = gender
        _isNextEnabled.value = true
    }

    fun selectWorkout(workout: String) {
        _selectedWorkout.value = workout
        _isNextEnabled.value = true
    }

    fun setHeight(height: Int) {
        _height.value = height
        checkHeightWeightSet()
    }

    fun setWeight(weight: Int) {
        _weight.value = weight
        checkHeightWeightSet()
    }

    fun setHeightFT(heightFT: Int) {
        _heightFT.value = heightFT
        checkHeightWeightSet()
    }

    fun setHeightIN(heightIN: Int) {
        _heightIN.value = heightIN
        checkHeightWeightSet()
    }

    fun setWeightLB(weightLB: Int) {
        _weightLB.value = weightLB
        checkHeightWeightSet()
    }

    private fun checkHeightWeightSet() {
        _isHeightWeightSet.value = when (_measurementUnit.value) {
            MeasurementUnit.METRIC -> _height.value != 160 && _weight.value != 60
            MeasurementUnit.IMPERIAL -> _heightFT.value != 5 && _heightIN.value != 3 && _weightLB.value != 132
            else -> false
        }
        if (_isHeightWeightSet.value == true) {
            _isNextEnabled.value = true
        }
    }

    fun setMeasurementUnit(unit: MeasurementUnit) {
        _measurementUnit.value = unit
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

            3 -> {
                if (_height.value == null || _weight.value == null) {
                    _showWarning.value = true
                } else {
                    updateHeightWeight()
                }
            }
        }
    }

    private fun updateGender() {
        val gender = _selectedGender.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "gender", gender)
        }
    }

    private fun updateWorkout() {
        val workout = _selectedWorkout.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "workoutsPerWeek", workout)
        }
    }

    fun invalidateShowWarning() {
        _showWarning.value = false
    }

    fun invalidateAll() {
        _selectedGender.value = null
        _selectedWorkout.value = null
        _height.value = 160
        _weight.value = 60
        _heightFT.value = 5
        _heightIN.value = 3
        _weightLB.value = 132
        _measurementUnit.value = MeasurementUnit.METRIC
        _isNextEnabled.value = false
        _isHeightWeightSet.value = false
    }

    private fun updateHeightWeight() {
        val height = if (_measurementUnit.value == MeasurementUnit.METRIC) {
            _height.value ?: return
        } else {
            val feet = _heightFT.value ?: return
            val inches = _heightIN.value ?: return
            (feet * 30.48 + inches * 2.54).toInt()
        }
        val weight = if (_measurementUnit.value == MeasurementUnit.METRIC) {
            _weight.value ?: return
        } else {
            _weightLB.value ?: return
        }
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "height", height.toString())
            updateUserFieldUseCase.updateUserFields(userID, "weight", weight.toString())
            updateUserFieldUseCase.updateUserFields(
                userID,
                "isMetric",
                (_measurementUnit.value == MeasurementUnit.METRIC).toString()
            )
        }
    }
}