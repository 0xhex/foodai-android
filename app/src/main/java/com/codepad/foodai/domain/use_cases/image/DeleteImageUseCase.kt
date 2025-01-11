package com.codepad.foodai.domain.use_cases.image

import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun deleteImage(imageId: String): UseCaseResult<Unit> {
        return when (val result = userRepository.deleteImage(imageId)) {
            is RepositoryResult.Success -> {
                UseCaseResult.Success(result.message, result.code, result.data)
            }

            is RepositoryResult.Error -> {
                UseCaseResult.Error(
                    message = result.message, code = result.code, exception = result.exception
                )
            }
        }
    }
} 