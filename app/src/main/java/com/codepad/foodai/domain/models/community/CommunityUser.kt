package com.codepad.foodai.domain.models.community

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityUser(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("assignedEmoji") val assignedEmoji: String,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("deviceLang") val deviceLang: String? = null,
    @SerializedName("profilePicUrl") val profilePicUrl: String? = null,
    @SerializedName("goal") val goal: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("targetWeight") val targetWeight: Double? = null,
    @SerializedName("workoutsPerWeek") val workoutsPerWeek: String? = null,
    @SerializedName("diet_type") val dietType: String? = null,
    @SerializedName("isMetric") val isMetric: Boolean? = null
) : Parcelable