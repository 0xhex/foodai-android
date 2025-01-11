package com.codepad.foodai.domain.use_cases.recipe

import com.codepad.foodai.domain.models.recipe.GenerateRecipeResponseData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class GenerateRecipeUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun generateRecipe(userID: String, mealType: String): UseCaseResult<GenerateRecipeResponseData> {
        return when (val result = userRepository.generateRecipe(userID, mealType)) {
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