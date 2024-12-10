package com.codepad.foodai.domain.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateUserFieldRequest(
    val fieldName: String,
    val fieldValue: String
) : Parcelable

@Parcelize
data class UpdateUserFieldResponseData(
    val id: String,
    val deviceID: String,
    val deviceLang: String,
    val userPlatform: String,
    val isPremium: Boolean,
    val revenueCatID: String?,
    val gender: String?,
    val workoutsPerWeek: String?,
    val height: Double?,
    val weight: Double?,
    val dateOfBirth: String?, // Use String for date to simplify
    val goal: String?,
    val accomplishments: List<String>?,
    val createdAt: String,
    val updatedAt: String
) : Parcelable