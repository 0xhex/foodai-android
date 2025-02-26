package com.codepad.foodai.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Date.toFormattedString(pattern: String): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

fun String.getTimeAgo(): String {
    val date = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(this)
    } catch (e: Exception) {
        return "now"
    }

    return date?.getTimeAgo() ?: "now"
}

fun Date.getTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this.time

    return when {
        diff < 1000L * 60 -> "now"
        diff < 1000L * 60 * 60 -> "${diff / (1000 * 60)}m ago"
        diff < 1000L * 60 * 60 * 24 -> "${diff / (1000 * 60 * 60)}h ago"
        diff < 1000L * 60 * 60 * 24 * 30 -> "${diff / (1000 * 60 * 60 * 24)}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(this)
    }
} 