package com.codepad.foodai.domain.use_cases.user

import android.os.Parcelable
import com.codepad.foodai.domain.models.image.ImageData
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class DailySummaryResponseData(
    val remainingNutrition: NutritionData?,
    val meals: List<ImageData>?,
    val exercises: List<ExerciseData>?,
    val healthScore: Double?,
) : Parcelable

@Parcelize
data class ExerciseData(
    val id: String?,
    val caloriesBurned: Int?,
    val exerciseType: String?,
    val duration: Int?, // in minutes
    val intensity: String?, // e.g., "low", "moderate", "high"
    val description: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val localCreatedAt: Date?,
) : Parcelable

@Parcelize
data class NutritionData(
    val calories: Int?,
    val carbs: Int?,
    val protein: Int?,
    val fat: Int?,
) : Parcelable