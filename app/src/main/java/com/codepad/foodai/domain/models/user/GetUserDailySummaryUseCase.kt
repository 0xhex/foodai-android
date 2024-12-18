package com.codepad.foodai.domain.models.user

import com.codepad.foodai.domain.repositories.RepositoryResult
import com.codepad.foodai.domain.repositories.UserRepository
import com.codepad.foodai.domain.use_cases.UseCaseResult
import com.codepad.foodai.domain.use_cases.user.DailySummaryResponseData
import javax.inject.Inject

class GetUserDailySummaryUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend fun getUserDailySummary(userID: String, date: String): UseCaseResult<DailySummaryResponseData> {
        return when (val result = userRepository.getUserDailySummary(userID, date)) {
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