package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
) : Parcelable