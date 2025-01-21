package com.codepad.foodai.domain.models.recommendation

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestRecommendationRequest(
    @SerializedName("imageID") val imageID: String,
) : Parcelable