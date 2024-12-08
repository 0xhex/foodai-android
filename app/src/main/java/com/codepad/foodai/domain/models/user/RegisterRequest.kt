package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegisterRequest(
    val deviceID: String,
    val revenueCatID: String? = null,
    val deviceLang: String,
    val userPlatform: String? = null,
    val isPremium: Boolean? = null,
) : Parcelable