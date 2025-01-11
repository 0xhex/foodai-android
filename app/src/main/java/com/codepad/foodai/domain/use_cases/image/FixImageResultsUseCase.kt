package com.codepad.foodai.domain.use_cases.image

import com.codepad.foodai.domain.models.image.FixImageResultsRequest
import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class FixImageResultsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun fixImageResults(imageId: String, prompt: String): UseCaseResult<ImageData?> {
        return when (val result =
            userRepository.fixResult(imageId, FixImageResultsRequest(prompt))) {
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