package com.codepad.foodai.domain.models.exercise

import com.google.gson.annotations.SerializedName

data class UpdateExerciseDescriptionRequest(
    @SerializedName("exerciseID") val exerciseID: String,
    @SerializedName("exerciseDescription") val exerciseDescription: String
) 