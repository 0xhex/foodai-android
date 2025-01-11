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
    val calories: Int?,
    val protein: Int?,
    val carbs: Int?,
    val fats: Int?,
    val healthScore: Int?,
    val ingredients: List<Ingredient>?,
    val description: String?,
    val totalResponse: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val localCreatedAt: Date?,
) : Parcelable

@Parcelize
data class Ingredient(
    val name: String?,
    val calory: Int?,
) : Parcelable