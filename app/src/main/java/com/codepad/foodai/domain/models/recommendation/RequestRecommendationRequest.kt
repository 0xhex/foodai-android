package com.codepad.foodai.domain.models.recommendation

import com.google.gson.annotations.SerializedName

data class RequestRecommendationRequest(
    @SerializedName("imageID") val imageID: String
) 