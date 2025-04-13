package com.codepad.foodai.domain.models.nutrition

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NutritionDetailsData(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("fiber")
    val fiber: Int,
    
    @SerializedName("fiberDailyPercentage")
    val fiberDailyPercentage: Int,
    
    @SerializedName("fiberEmoji")
    val fiberEmoji: String,
    
    @SerializedName("vitamins")
    val vitamins: List<VitaminInfo>,
    
    @SerializedName("minerals")
    val minerals: List<MineralInfo>,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("__v")
    val version: Int?
) : Parcelable

@Parcelize
data class VitaminInfo(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("unit")
    val unit: String,
    
    @SerializedName("dailyPercentage")
    val dailyPercentage: Int,
    
    @SerializedName("emoji")
    val emoji: String
) : Parcelable

@Parcelize
data class MineralInfo(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("unit")
    val unit: String,
    
    @SerializedName("dailyPercentage")
    val dailyPercentage: Int,
    
    @SerializedName("emoji")
    val emoji: String
) : Parcelable

data class NutritionDetailsRequest(
    @SerializedName("userID")
    val userId: String
) 