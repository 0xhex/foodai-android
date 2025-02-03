package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class User(
    @SerializedName("_id") val id: String,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("createdAt") val createdAt: Date?,
    @SerializedName("updatedAt") val updatedAt: Date?,
    @SerializedName("isMetric") val isMetric: Boolean?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("height") val height: Double?,
    @SerializedName("goalWeight") val goalWeight: Double?,
    @SerializedName("birthDate") val birthDate: Date?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("deviceID") val deviceID: String,
    @SerializedName("deviceLang") val deviceLang: String,
    @SerializedName("userPlatform") val userPlatform: String,
    @SerializedName("isPremium") val isPremium: Boolean,
    @SerializedName("revenueCatID") val revenueCatID: String?,
    @SerializedName("workoutsPerWeek") val workoutsPerWeek: String?,
    @SerializedName("goal") val goal: String?,
    @SerializedName("accomplishments") val accomplishments: List<String>?,
    @SerializedName("targetWeight") val targetWeight: Int?,
    @SerializedName("targetPerWeek") val targetPerWeek: Double?,
    @SerializedName("dailyCalories") val dailyCalories: Int?,
    @SerializedName("dailyCarb") val dailyCarb: Int?,
    @SerializedName("dailyProtein") val dailyProtein: Int?,
    @SerializedName("dailyFat") val dailyFat: Int?,
    @SerializedName("dateOfBirth") val dateOfBirth: String? // Use String for date, or a custom Date adapter
) : Parcelable {
    fun calculateCompletedRatio(startingWeight: Double, currentWeight: Double, targetWeight: Double): Double {
        val totalChange = targetWeight - startingWeight
        if (totalChange == 0.0) {
            return 1.0 // Goal already achieved
        }
        return (currentWeight - startingWeight) / totalChange
    }

    fun getParsedDate(): String {
        val isoDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return try {
            val parsedDate = dateOfBirth?.let { isoDateFormat.parse(it) }
            parsedDate?.let { dateFormatter.format(it) } ?: "Invalid Date"
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    val dateAsObject: Date?
        get() = try {
            val isoDateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateOfBirth?.let { isoDateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
}