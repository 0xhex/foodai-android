package com.codepad.foodai.domain.models.community
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class CommunityPost(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: CommunityUser,
    @SerializedName("image") val image: CommunityImage,
    @SerializedName("likes") val likes: List<CommunityUser>,
    @SerializedName("comments") val comments: List<CommunityComment>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String? = null,
) : Parcelable

@Parcelize
data class CommunityImage(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: String,
    @SerializedName("url") val url: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalResponse") val totalResponse: String?,
    @SerializedName("localCreatedAt") val localCreatedAt: String,
    @SerializedName("localDate") val localDate: String,
    @SerializedName("ingredients") val ingredients: List<Ingredient>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int,
    @SerializedName("calories") val calories: Int,
    @SerializedName("carbs") val carbs: Int,
    @SerializedName("description") val description: String,
    @SerializedName("fats") val fats: Int,
    @SerializedName("healthScore") val healthScore: Int,
    @SerializedName("mealName") val mealName: String,
    @SerializedName("protein") val protein: Int,
    @SerializedName("id") val imageId: String? = null
) : Parcelable

@Parcelize
data class Ingredient(
    @SerializedName("name") val name: String,
    @SerializedName("calory") val calory: Int
) : Parcelable

data class ImageData(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: String,
    @SerializedName("url") val url: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalResponse") val totalResponse: String?,
    @SerializedName("localCreatedAt") val localCreatedAt: String,
    @SerializedName("localDate") val localDate: String,
    @SerializedName("ingredients") val ingredients: List<Ingredient>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int,
    @SerializedName("calories") val calories: Int,
    @SerializedName("carbs") val carbs: Int,
    @SerializedName("description") val description: String,
    @SerializedName("fats") val fats: Int,
    @SerializedName("healthScore") val healthScore: Int,
    @SerializedName("mealName") val mealName: String,
    @SerializedName("protein") val protein: Int
)