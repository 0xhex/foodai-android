package com.codepad.foodai.domain.repositories

import com.codepad.foodai.domain.api.APIError

sealed class RepositoryResult<T> {
    data class Success<T>(val message: String, val code: Int, val data: T) : RepositoryResult<T>()
    data class Error<T>(val message: String, val code: Int, val exception: APIError?) : RepositoryResult<T>()
}