package com.codepad.foodai.domain.use_cases.user

import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun getUserData(userId: String): UseCaseResult<User> {
        return when (val result = userRepository.getUserData(userId)) {
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