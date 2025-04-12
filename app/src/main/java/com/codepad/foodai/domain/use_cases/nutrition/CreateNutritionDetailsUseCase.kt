package com.codepad.foodai.domain.use_cases.nutrition

import com.codepad.foodai.domain.models.nutrition.NutritionDetailsData
import com.codepad.foodai.domain.models.nutrition.NutritionDetailsRequest
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class CreateNutritionDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun createNutritionDetails(
        imageId: String,
        userId: String,
    ): UseCaseResult<NutritionDetailsData> {
        return when (val result =
            userRepository.createNutritionDetails(imageId, NutritionDetailsRequest(userId))) {
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