package com.codepad.foodai.domain.use_cases.recommendation

import com.codepad.foodai.domain.models.recommendation.RequestRecommendationResponse
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class RequestRecommendationsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun requestRecommendations(imageID: String): UseCaseResult<RequestRecommendationResponse> {
        return when (val result = userRepository.requestRecommendations(imageID)) {
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