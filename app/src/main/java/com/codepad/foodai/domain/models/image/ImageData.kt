package com.codepad.foodai.domain.models.image

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ImageData(
    @SerializedName("_id")
    val id: String,
    val user: String,
    val url: String,
    val status: String,
    var calories: Int?,
    var protein: Int?,
    var carbs: Int?,
    var fats: Int?,
    val healthScore: Int?,
    val ingredients: List<Ingredient>?,
    val mealName: String?,
    val description: String?,
    val totalResponse: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val localCreatedAt: Date?,
    val recommendationId: String?,
) : Parcelable

@Parcelize
data class Ingredient(
    val name: String?,
    val calory: Int?,
) : Parcelable