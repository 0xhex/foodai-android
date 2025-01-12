package com.codepad.foodai.domain.models.exercise

import com.google.gson.annotations.SerializedName

data class UpdateCustomExerciseRequest(
    @SerializedName("exerciseID") val exerciseID: String,
    @SerializedName("type") val type: String,
    @SerializedName("intensity") val intensity: String,
    @SerializedName("duration") val duration: Int
) 