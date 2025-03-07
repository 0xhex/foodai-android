package com.codepad.foodai.ui.user_property

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.domain.use_cases.user.UpdateUserFieldUseCase
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.user_property.heightweight.MeasurementUnit
import com.codepad.foodai.ui.user_property.loading.LoadingType
import com.codepad.foodai.ui.user_property.rating.Gender
import com.codepad.foodai.ui.user_property.rating.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UserPropertySharedViewModel @Inject constructor(
    private val updateUserFieldUseCase: UpdateUserFieldUseCase,
    private val resourceHelper: ResourceHelper,
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

    private val _dateOfBirth = MutableLiveData<Date?>()
    val dateOfBirth: MutableLiveData<Date?> get() = _dateOfBirth

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

    private val _weightSpeed = MutableLiveData<Double>()
    val weightSpeed: LiveData<Double> get() = _weightSpeed

    val goalNavigationParams = MutableLiveData<Pair<Boolean, Boolean>>()

    var loadingType: LoadingType = LoadingType.USER_CUSTOMIZATION
    var settingUpItems: List<String> = emptyList()

    val reviews = listOf(
        Review("Marley Bryle", 5, resourceHelper.getString(R.string.lost_15_lbs), Gender.MALE),
        Review(
            "Jane Doe",
            5,
            resourceHelper.getString(R.string.gaining_muscle_breeze),
            Gender.FEMALE
        ),
    )

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

    fun setWeightSpeed(speed: Double) {
        _weightSpeed.value = speed
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
        val requireWeightSelection = goal != "maintain"
        val isGain = goal == "gain_weight"
        goalNavigationParams.value = Pair(requireWeightSelection, isGain)
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
        val requireDesiredWeight = goalNavigationParams.value?.first == true

        when (currentStep) {
            1 -> handleStep(_selectedGender.value, ::updateGender)
            2 -> handleStep(_selectedWorkout.value, ::updateWorkout)
            3 -> handleStep(_height.value != null && _weight.value != null, ::updateHeightWeight)
            4 -> handleStep(_dateOfBirth.value, ::updateUserBirthDate)
            5 -> handleStep(_selectedGoal.value, ::updateUserGoal)
            6 -> if (requireDesiredWeight) {
                handleStep(_desiredWeight.value, ::updateDesiredWeight)
            } else {
                handleStep(_selectedReachingGoal.value, ::updateUserReachingGoal)
            }

            7 -> if (requireDesiredWeight) {
                handleStep(_weightSpeed.value, ::updateWeightSpeed)
            } else {
                handleStep(_selectedDiet.value, ::updateUserDiet)
            }

            8 -> if (requireDesiredWeight) {
                handleStep(_selectedReachingGoal.value, ::updateUserReachingGoal)
            } else {
                handleStep(_selectedAccomplishment.value, ::updateUserAccomplishment)
            }

            9 -> if (requireDesiredWeight) {
                handleStep(_selectedDiet.value, ::updateUserDiet)
            }

            10 -> if (requireDesiredWeight) {
                handleStep(_selectedAccomplishment.value, ::updateUserAccomplishment, false)
            }
        }
    }

    private fun <T> handleStep(value: T?, updateFunction: () -> Unit, showWarning: Boolean = true) {
        if (value == null) {
            _showWarning.value = showWarning
        } else {
            updateFunction()
        }
    }

    fun updateGender() {
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

    fun updateHeightWeight() {
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

    fun updateWeightSpeed() {
        val userID = UserSession.user?.id ?: return

        viewModelScope.launch {
            updateUserFieldUseCase.updateUserFields(
                userID,
                "targetPerWeek",
                weightSpeed.value.toString()
            )
        }
    }

}