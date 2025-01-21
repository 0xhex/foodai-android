package com.codepad.foodai.helpers

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val remoteConfig: FirebaseRemoteConfig
) {
    // StateFlow properties to match @Published in iOS
    private val _firstProduct = MutableStateFlow("")
    val firstProduct: StateFlow<String> = _firstProduct

    private val _secondProduct = MutableStateFlow("")
    val secondProduct: StateFlow<String> = _secondProduct

    private val _isPassedStore = MutableStateFlow(false)
    val isPassedStore: StateFlow<Boolean> = _isPassedStore

    private val _specialEventDay = MutableStateFlow(false)
    val specialEventDay: StateFlow<Boolean> = _specialEventDay

    private val _specialProduct = MutableStateFlow("")
    val specialProduct: StateFlow<String> = _specialProduct

    private val _finishDate = MutableStateFlow<Date?>(null)
    val finishDate: StateFlow<Date?> = _finishDate

    init {
        setupRemoteConfig()
        fetchRemoteConfig()
    }

    private fun setupRemoteConfig() {
        // Default values matching iOS implementation
        val defaults = hashMapOf(
            "firstProduct" to "foodai_weekly_tier1",
            "secondProduct" to "foodai_yearly_standart",
            "isPassedStore" to false,
            "specialEventDay" to false,
            "specialProduct" to "foodai_special_discount",
            "finishDate" to "2024-12-25T23:59:59Z"
        )
        remoteConfig.setDefaultsAsync(defaults)
    }

    fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateStateFlows()
                Log.d(TAG, "Remote config fetch successful")
            } else {
                Log.e(TAG, "Config fetch failed: ${task.exception?.message}")
            }
        }
    }

    private fun updateStateFlows() {
        _firstProduct.value = remoteConfig.getString("firstProduct")
        _secondProduct.value = remoteConfig.getString("secondProduct")
        _isPassedStore.value = remoteConfig.getBoolean("isPassedStore")
        _specialEventDay.value = remoteConfig.getBoolean("specialEventDay")
        _specialProduct.value = remoteConfig.getString("specialProduct")
        
        val finishDateString = remoteConfig.getString("finishDate")
        try {
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            _finishDate.value = isoFormatter.parse(finishDateString)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing finish date: ${e.message}")
            _finishDate.value = null
        }
    }

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(name, params)
        Log.d(TAG, "Firebase Event Logged: $name with parameters: $params")
    }

    fun setUserID(userID: String?) {
        firebaseAnalytics.setUserId(userID)
        Log.d(TAG, "Firebase User ID set to: $userID")
    }

    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
        Log.d(TAG, "Firebase User Property set: $name = $value")
    }

    companion object {
        private const val TAG = "FirebaseManager"
    }
}