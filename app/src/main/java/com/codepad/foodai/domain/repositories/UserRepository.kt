package com.codepad.foodai.domain.repositories

import com.codepad.foodai.domain.api.APIError
import com.codepad.foodai.domain.api.RestApi
import com.codepad.foodai.domain.models.user.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val restApi: RestApi,
) {
    suspend fun getUserData(userId: String): RepositoryResult<User> {
        return try {
            val response = restApi.getUserData(userId)
            if (response.success && response.data != null) {
                RepositoryResult.Success(
                    message = response.message ?: "Success",
                    code = response.errorCode ?: 0,
                    data = response.data
                )
            } else {
                RepositoryResult.Error(
                    message = response.message ?: "Unknown error",
                    code = response.errorCode ?: -1,
                    exception = APIError.ServerError(
                        response.message ?: "Unknown error", response.errorCode?.toString()
                    )
                )
            }
        } catch (e: Exception) {
            RepositoryResult.Error(
                message = e.message ?: "Network error",
                code = -1,
                exception = APIError.NetworkError(e)
            )
        }
    }


}