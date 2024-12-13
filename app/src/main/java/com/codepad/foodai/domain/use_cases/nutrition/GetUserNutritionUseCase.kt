package com.codepad.foodai.domain.use_cases.nutrition

import com.codepad.foodai.domain.models.nutrition.NutritionResponseData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class GetUserNutritionUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun getUserNutrition(userId: String): UseCaseResult<NutritionResponseData> {
        return when (val result = userRepository.getUserNutrition(userId)) {
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