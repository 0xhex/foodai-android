package com.codepad.foodai.ui.user_property

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.user_property.heightweight.MeasurementUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    private val _dateOfBirth = MutableLiveData<Date>()
    val dateOfBirth: LiveData<Date> get() = _dateOfBirth

    private val _selectedGoal = MutableLiveData<String?>()
    val selectedGoal: LiveData<String?> get() = _selectedGoal

    private val _selectedReachingGoal = MutableLiveData<String?>()
    val selectedReachingGoal: LiveData<String?> get() = _selectedReachingGoal

    private val _selectedDiet = MutableLiveData<String?>()
    val selectedDiet: LiveData<String?> get() = _selectedDiet

    private val _selectedAccomplishment = MutableLiveData<String?>()
    val selectedAccomplishment: LiveData<String?> get() = _selectedAccomplishment

    private val _desiredWeight = MutableLiveData<Int>()
    val desiredWeight: LiveData<Int> get() = _desiredWeight

    val goalNavigationParams = _selectedGoal.map {
        val requireWeightSelection = it != "maintain"
        val isGain = it == "gain_weight"
        Pair(requireWeightSelection, isGain)
    }

    init {
        _height.value = 160
        _weight.value = 60
        _heightFT.value = 5
        _heightIN.value = 3
        _weightLB.value = 132
        _measurementUnit.value = MeasurementUnit.METRIC
        _isHeightWeightSet.value = false
    }

    fun selectAccomplishment(accomplishment: String) {
        _selectedAccomplishment.value = accomplishment
        _isNextEnabled.value = true
    }

    fun setDesiredWeight(weight: Int) {
        _desiredWeight.value = weight
        _isNextEnabled.value = true
    }

    fun selectDiet(diet: String) {
        _selectedDiet.value = diet
        _isNextEnabled.value = true
    }

    fun selectReachingGoal(goal: String) {
        _selectedReachingGoal.value = goal
        _isNextEnabled.value = true
    }

    fun selectGender(gender: String) {
        _selectedGender.value = gender
        _isNextEnabled.value = true
    }

    fun selectGoal(goal: String) {
        _selectedGoal.value = goal
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

    fun setDateOfBirth(date: Date) {
        _dateOfBirth.value = date
        _isNextEnabled.value = true
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

            4 -> {
                if (_dateOfBirth.value == null) {
                    _showWarning.value = true
                } else {
                    updateUserBirthDate()
                }
            }

            5 -> {
                if (_selectedGoal.value == null) {
                    _showWarning.value = true
                } else {
                    updateUserGoal()
                }
            }

            6 -> {
                if (_desiredWeight.value == null) {
                    _showWarning.value = true
                } else {
                    updateDesiredWeight()
                }
            }

            7 -> { // TODO order will change
                if (_selectedReachingGoal.value == null) {
                    _showWarning.value = true
                } else {
                    updateUserReachingGoal()
                }
            }

            8 -> { // TODO order will change
                if (_selectedDiet.value == null) {
                    _showWarning.value = true
                } else {
                    updateUserDiet()
                }
            }

            9 -> { // TODO order will change
                if (_selectedAccomplishment.value == null) {
                    _showWarning.value = true
                } else {
                    updateUserAccomplishment()
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
        _dateOfBirth.value = null
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

    fun updateUserBirthDate() {
        val date = _dateOfBirth.value ?: return
        val formattedDate =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(date)
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "dateOfBirth", formattedDate)
        }
    }

    fun updateUserGoal() {
        val goal = _selectedGoal.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "goal", goal)
        }
    }

    fun updateUserReachingGoal() {
        val goal = _selectedReachingGoal.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "stopping", goal)
        }
    }

    fun updateUserDiet() {
        val diet = _selectedDiet.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "diet_type", diet)
        }
    }

    fun updateUserAccomplishment() {
        val accomplishment = _selectedAccomplishment.value ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(
                userID,
                "accomplishments",
                arrayFieldValue = listOf(accomplishment)
            )
        }
    }

    fun updateDesiredWeight() {
        val desiredWeight = _desiredWeight.value?.toString() ?: return
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(userID, "targetWeight", desiredWeight)
        }
    }

}