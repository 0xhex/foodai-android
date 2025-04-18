package com.codepad.foodai.domain.use_cases.exercise

import com.codepad.foodai.domain.models.exercise.ExerciseData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class SubmitExerciseDescriptionUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun submitDescription(
        userID: String,
        exerciseDescription: String,
        localDate: String
    ): UseCaseResult<ExerciseData> {
        return when (val result = userRepository.submitExerciseDescription(userID, exerciseDescription, localDate)) {
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