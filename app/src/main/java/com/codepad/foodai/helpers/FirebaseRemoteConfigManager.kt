package com.codepad.foodai.helpers

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.GsonBuilder

const val REMOTE_CONFIG_FETCH_INTERVAL: Long = 1

class FirebaseRemoteConfigManager {
    companion object {
        fun setUpRemoteConfig() {
            val remoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = REMOTE_CONFIG_FETCH_INTERVAL
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
        }

        fun fetchRemoteConfigurations(
            activity: AppCompatActivity, completion: (success: Boolean) -> Unit
        ) {
            Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener(activity) { task ->
                completion(task.isSuccessful)
            }
        }

        fun getString(key: String): String {
            return Firebase.remoteConfig.getString(key)
        }

        fun getLong(key: String): Long {
            return Firebase.remoteConfig.getLong(key)
        }

        fun getDouble(key: String): Double {
            return Firebase.remoteConfig.getDouble(key)
        }

        fun getBoolean(key: String): Boolean {
            return Firebase.remoteConfig.getBoolean(key)
        }

        fun <T> getJson(key: String, returnType: Class<T>): T? {
            return Firebase.remoteConfig.getString(key).takeIf { it.isNotBlank() }?.let {
                GsonBuilder().create().fromJson(it, returnType)
            }
        }
    }
}