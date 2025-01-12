package com.codepad.foodai.domain.models.exercise

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseData(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: String,
    @SerializedName("type") val type: String?,
    @SerializedName("intensity") val intensity: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("exerciseDescription") val exerciseDescription: String?,
    @SerializedName("localDate") val localDate: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) : Parcelable 