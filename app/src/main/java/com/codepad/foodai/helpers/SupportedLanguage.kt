package com.codepad.foodai.helpers

enum class SupportedLanguage(val code: String) {
    GERMAN("de"),
    ESPANOL("es"),
    FRENCH("fr"),
    ENGLISH("en");

    companion object {
        private val map = entries.associateBy(SupportedLanguage::code)
        fun fromCode(code: String) = map[code] ?: ENGLISH
    }
}