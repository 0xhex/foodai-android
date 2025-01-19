package com.codepad.foodai.domain.models.recommendation

import com.google.gson.annotations.SerializedName

data class RequestRecommendationResponse(
    @SerializedName("recommendationId") val recommendationId: String
) 