package com.codepad.foodai.domain.models.image

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FixImageResultsRequest(
    @SerializedName("prompt")
    val prompt: String,
) : Parcelable