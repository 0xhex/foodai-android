package com.codepad.foodai.domain.models.recommendation

import com.google.gson.annotations.SerializedName

data class Recommendation(
    @SerializedName("status") val status: String,
    @SerializedName("recommendations") val recommendations: List<String>?,
    @SerializedName("whatToChange") val whatToChange: String?,
    @SerializedName("whatToBeCarefulAbout") val whatToBeCarefulAbout: String?
) 