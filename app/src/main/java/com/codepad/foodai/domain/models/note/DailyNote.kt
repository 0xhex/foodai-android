package com.codepad.foodai.domain.models.note

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyNote(
    val keyDate: String,
    var noteText: String = "",
    var mood: String = ""
) {
    companion object {
        fun dateKeyString(date: Date): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(date)
        }
    }
} 