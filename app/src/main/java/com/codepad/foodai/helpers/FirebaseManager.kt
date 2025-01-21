package com.codepad.foodai.helpers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val remoteConfig: FirebaseRemoteConfig
) {
    val isSpecialEventDay: Boolean
        get() = remoteConfig.getBoolean("is_special_event_day")

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(name, params)
    }

    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}