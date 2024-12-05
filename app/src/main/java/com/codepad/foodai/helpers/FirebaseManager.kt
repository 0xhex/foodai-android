package com.codepad.foodai.helpers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) {
    fun logEvent(eventName: String, params: Map<String, String>? = null) {
        val bundle = Bundle().apply {
            params?.forEach { (key, value) ->
                putString(key, value)
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}