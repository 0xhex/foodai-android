package com.codepad.foodai.domain.use_cases.user

import com.codepad.foodai.domain.models.user.UpdateUserFieldRequest
import com.codepad.foodai.domain.models.user.UpdateUserFieldRequestArray
import com.codepad.foodai.domain.models.user.UpdateUserFieldResponseData
import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import javax.inject.Inject

class UpdateUserFieldUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun updateUserFields(
        userID: String,
        fieldName: String,
        fieldValue: String? = null,
        arrayFieldValue: List<String>? = null,
    ): UseCaseResult<UpdateUserFieldResponseData> {
        return if (arrayFieldValue == null) {
            val request = UpdateUserFieldRequest(fieldName, fieldValue.orEmpty())
            when (val result = userRepository.updateUserFields(userID, request)) {
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
        } else {
            val request = UpdateUserFieldRequestArray(fieldName, arrayFieldValue)
            when (val result = userRepository.updateUserFieldsArray(userID, request)) {
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
}