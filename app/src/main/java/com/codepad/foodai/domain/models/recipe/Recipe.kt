package com.codepad.foodai.domain.models.recipe

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Recipe(
    @SerializedName("_id")
    val id: String,
    val user: String,
    val title: String?,
    val description: String?,
    val ingredients: List<String>?,
    val instructions: List<String>?,
    val nutritionalInformation: List<String>?,
    val mealType: String,
    val status: String,
    val errorMessage: String?,
    val cookingTips: List<String>?,
    val personalizedExplanation: String?,
    val createdAt: Date?,
    val updatedAt: Date?
)

data class GenerateRecipeResponseData(
    val recipeID: String
)

data class GenerateRecipeRequest(
    val userID: String,
    val mealType: String
) 