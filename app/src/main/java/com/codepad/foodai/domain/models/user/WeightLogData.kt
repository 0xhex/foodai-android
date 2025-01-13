package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WeightLogData(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("user") val user: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("date") val date: Date? = null,
    @SerializedName("createdAt") val createdAt: Date? = null,
    @SerializedName("updatedAt") val updatedAt: Date? = null
) : Parcelable 