package com.codepad.foodai.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.user.GetUserDataUseCase
import com.codepad.foodai.helpers.ModelPreferencesManager
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
) : ViewModel() {

    private val _userDataResponse = MutableLiveData<User?>()
    val userDataResponse: LiveData<User?> get() = _userDataResponse

    fun checkOnboardingAndFetchUserData() {
        if (isOnboardingCompleted()) {
            fetchUserData()
        } else {
            _userDataResponse.value = null
        }
    }

    private fun isOnboardingCompleted(): Boolean {
        return ModelPreferencesManager.get<Boolean>("isUserPropertySet") ?: false
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            when (val result = getUserDataUseCase.getUserData(UserSession.user?.id.orEmpty())) {
                is UseCaseResult.Success -> {
                    _userDataResponse.value = result.data
                    UserSession.updateSession(result.data)
                }

                is UseCaseResult.Error -> {
                    _userDataResponse.value = null
                    UserSession.clearSession()
                }
            }
        }
    }
}