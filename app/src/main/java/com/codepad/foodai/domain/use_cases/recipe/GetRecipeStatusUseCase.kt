package com.codepad.foodai.domain.use_cases.recipe

import com.codepad.foodai.domain.models.recipe.Recipe
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class GetRecipeStatusUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun getRecipeStatus(recipeID: String): UseCaseResult<Recipe> {
        return when (val result = userRepository.getRecipeStatus(recipeID)) {
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