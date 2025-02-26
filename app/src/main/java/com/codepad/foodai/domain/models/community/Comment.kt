package com.codepad.foodai.domain.models.community

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Comment(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: CommunityUser,
    @SerializedName("text") val text: String,
    @SerializedName("createdAt") val createdAt: Date?
) : Parcelable 