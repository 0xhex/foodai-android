package com.codepad.foodai.domain.models.exercise

import com.google.gson.annotations.SerializedName

data class SubmitExerciseDescriptionRequest(
    @SerializedName("userID") val userID: String,
    @SerializedName("exerciseDescription") val exerciseDescription: String,
    @SerializedName("localDate") val localDate: String
) 