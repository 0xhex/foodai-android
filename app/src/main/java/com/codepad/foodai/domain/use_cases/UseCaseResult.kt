package com.codepad.foodai.domain.use_cases

import com.codepad.foodai.domain.api.APIError

sealed class UseCaseResult<out T> {
    data class Success<T>(val message: String, val code: Int, val data: T): UseCaseResult<T>()
    data class Error<T>(val message: String, val code: Int, val exception: APIError?): UseCaseResult<T>()
}