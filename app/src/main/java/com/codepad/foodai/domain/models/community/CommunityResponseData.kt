package com.codepad.foodai.domain.models.community

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityResponseData(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val posts: List<CommunityPost>
) : Parcelable