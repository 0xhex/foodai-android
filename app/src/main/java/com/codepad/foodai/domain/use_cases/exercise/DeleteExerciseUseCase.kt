package com.codepad.foodai.domain.use_cases.exercise

import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class DeleteExerciseUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun deleteExercise(exerciseId: String): UseCaseResult<Unit> {
        return when (val result = userRepository.deleteExercise(exerciseId)) {
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