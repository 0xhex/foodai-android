package com.codepad.foodai.domain.use_cases.user

import com.codepad.foodai.domain.models.user.RegisterRequest
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun registerUser(request: RegisterRequest): RepositoryResult<User> {
        return userRepository.registerUser(request)
    }
}