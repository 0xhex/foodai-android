package com.codepad.foodai.domain.use_cases.user

import com.codepad.foodai.domain.models.user.RegisterRequest
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun registerUser(request: RegisterRequest): UseCaseResult<User> {
        return when (val result = userRepository.registerUser(request)) {
            is RepositoryResult.Success -> {
                UseCaseResult.Success(
                    message = result.message,
                    code = result.code,
                    data = result.data
                )
            }
            is RepositoryResult.Error -> {
                UseCaseResult.Error(
                    message = result.message,
                    code = result.code,
                    exception = result.exception
                )
            }
        }
    }
}