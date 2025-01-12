package com.codepad.foodai.domain.use_cases.exercise

import com.codepad.foodai.domain.models.exercise.ExerciseData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class LogExerciseCustomUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun logExercise(
        userID: String,
        type: String,
        intensity: String,
        duration: Int,
        localDate: String
    ): UseCaseResult<ExerciseData> {
        return when (val result = userRepository.logExerciseCustom(userID, type, intensity, duration, localDate)) {
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