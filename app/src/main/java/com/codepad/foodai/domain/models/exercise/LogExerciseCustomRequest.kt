package com.codepad.foodai.domain.models.exercise

import com.google.gson.annotations.SerializedName

data class LogExerciseCustomRequest(
    @SerializedName("userID") val userID: String,
    @SerializedName("type") val type: String,
    @SerializedName("intensity") val intensity: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("localDate") val localDate: String
) 