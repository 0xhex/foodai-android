package com.codepad.foodai.domain.models.community

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityUser(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("profilePicUrl") val profilePicUrl: String?,
    @SerializedName("deviceLang") val deviceLang: String? = null,
    @SerializedName("countryCode") val countryCode: String?,
    @SerializedName("goal") val goal: String?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("targetWeight") val targetWeight: Double?,
    @SerializedName("isMetric") val isMetric: Boolean?,
    @SerializedName("workoutsPerWeek") val workoutsPerWeek: String?,
    @SerializedName("diet_type") val dietType: String?,
    @SerializedName("assignedEmoji") val assignedEmoji: String?
) : Parcelable