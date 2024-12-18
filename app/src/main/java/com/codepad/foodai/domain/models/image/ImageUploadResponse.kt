package com.codepad.foodai.domain.models.image

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageUploadResponse(
    val status: String,
    val imageID: String,
) : Parcelable