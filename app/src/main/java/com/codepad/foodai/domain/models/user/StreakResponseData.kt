package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StreakResponseData(
    @SerializedName("currentStreak") val currentStreak: Int,
    @SerializedName("weekStart") val weekStart: String,
    @SerializedName("weekEnd") val weekEnd: String,
    @SerializedName("dailyStatus") val dailyStatus: Map<String, Boolean>,
) : Parcelable