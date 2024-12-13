package com.codepad.foodai.domain.models.nutrition

import com.google.gson.annotations.SerializedName

data class NutritionResponseData(
    @SerializedName("totalCalories") val totalCalories: Int,
    @SerializedName("protein") val protein: Int,
    @SerializedName("fat") val fat: Int,
    @SerializedName("carbohydrates") val carbohydrates: Int
)