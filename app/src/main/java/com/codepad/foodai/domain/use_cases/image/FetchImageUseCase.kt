package com.codepad.foodai.domain.use_cases.image

import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class FetchImageUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun fetchImage(imageID: String): UseCaseResult<ImageData> {
        return when (val result = userRepository.fetchImage(imageID)) {
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