package com.codepad.foodai.domain.use_cases.exercise

import com.codepad.foodai.domain.models.exercise.ExerciseData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class UpdateCustomExerciseUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun updateCustomExercise(
        exerciseID: String,
        type: String,
        intensity: String,
        duration: Int
    ): UseCaseResult<ExerciseData> {
        return when (val result = userRepository.updateCustomExercise(exerciseID, type, intensity, duration)) {
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