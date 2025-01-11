package com.codepad.foodai.domain.models.recipe

enum class MealType(val displayName: String, val codeName: String) {
    BREAKFAST("Breakfast", "breakfast"),
    LUNCH("Lunch", "lunch"),
    DINNER("Dinner", "dinner");

    companion object {
        fun fromCodeName(codeName: String): MealType? = 
            values().find { it.codeName == codeName.lowercase() }
    }
} 