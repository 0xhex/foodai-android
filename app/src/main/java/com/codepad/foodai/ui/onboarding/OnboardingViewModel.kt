package com.codepad.foodai.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.domain.models.user.RegisterRequest
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.user.RegisterUserUseCase
import com.codepad.foodai.helpers.MediaDrmIdProvider
import com.codepad.foodai.helpers.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase,
) : ViewModel() {
    private val _registerUserResponse = MutableLiveData<User?>()
    val registerUserResponse: LiveData<User?> get() = _registerUserResponse

    fun registerUser() {
        val request = RegisterRequest(
            deviceID =  "0a59e224f66293ad350612a244507ab420ee61ba580b27e2352c609208496200" ,// MediaDrmIdProvider.getDeviceUniqueId(),
            deviceLang = Locale.getDefault().language,
            userPlatform = "Android"
        )

        viewModelScope.launch {
            when (val result = registerUserUseCase.registerUser(request)) {
                is UseCaseResult.Success -> {
                    _registerUserResponse.value = result.data
                    UserSession.updateSession(result.data)
                }

                is UseCaseResult.Error -> {
                    _registerUserResponse.value = null
                    UserSession.clearSession()
                }
            }
        }
    }
}