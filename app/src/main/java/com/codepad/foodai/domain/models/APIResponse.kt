package com.codepad.foodai.domain.models

import com.google.gson.annotations.SerializedName

data class APIResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("message") val message: String?,
    @SerializedName("errorCode") val errorCode: Int?,
)