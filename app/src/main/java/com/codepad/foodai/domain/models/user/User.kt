package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("_id") val id: String,
    val deviceID: String,
    val deviceLang: String,
    val userPlatform: String,
    val isPremium: Boolean,
    val revenueCatID: String?,
    val gender: String?,
    val workoutsPerWeek: String?,
    val height: Double?,
    val weight: Double?,
    val dateOfBirth: String?, // Use String for date, or a custom Date adapter
    val goal: String?,
    val accomplishments: List<String>?,
    val targetWeight: Int?,
    val targetPerWeek: Double?,
    val dailyCalories: Int?,
    val dailyCarb: Int?,
    val dailyProtein: Int?,
    val dailyFat: Int?,
    val isMetric: Boolean?
) : Parcelable {
    fun calculateCompletedRatio(startingWeight: Double, currentWeight: Double, targetWeight: Double): Double {
        val totalChange = targetWeight - startingWeight
        if (totalChange == 0.0) {
            return 1.0 // Goal already achieved
        }
        return (currentWeight - startingWeight) / totalChange
    }
}