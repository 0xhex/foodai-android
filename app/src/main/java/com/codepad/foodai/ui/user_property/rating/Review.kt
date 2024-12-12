package com.codepad.foodai.ui.user_property.rating

data class Review(
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val gender: Gender
)

enum class Gender {
    MALE, FEMALE
}