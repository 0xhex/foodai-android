package com.codepad.foodai.domain.api

import java.io.ObjectStreamException
import java.io.Serializable

sealed class APIError : Throwable(), Serializable {
    @Throws(ObjectStreamException::class)
    private fun readResolve(): Any {
        return when (this) {
            is ServerError -> ServerError(this.errorMessage, this.code)
            is NetworkError -> NetworkError(this.exception)
            is DecodingError -> DecodingError(this.exception)
            is UnknownError -> UnknownError
        }
    }

    data class ServerError(val errorMessage: String, val code: String?) : APIError()
    data class NetworkError(val exception: Exception) : APIError()
    data class DecodingError(val exception: Exception) : APIError()
    object UnknownError : APIError()

    val errorCode: String?
        get() = when (this) {
            is ServerError -> this.code
            else -> null
        }

    override val message: String?
        get() = when (this) {
            is ServerError -> this.errorMessage
            is NetworkError -> this.exception.localizedMessage
            is DecodingError -> this.exception.localizedMessage
            is UnknownError -> "An unknown error occurred."
        }
}