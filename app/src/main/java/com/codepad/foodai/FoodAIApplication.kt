package com.codepad.foodai

import android.app.Application
import android.util.Log
import com.codepad.foodai.helpers.FirebaseRemoteConfigManager
import com.codepad.foodai.helpers.ModelPreferencesManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class FoodAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initModelPrefManager()
        FirebaseRemoteConfigManager.setUpRemoteConfig()
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        } else {
            plant(ReleaseTree())
        }
    }


    private fun initModelPrefManager() {
        ModelPreferencesManager.with(this)
    }
}

internal class ReleaseTree : Timber.Tree() {
    protected override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Log.println(priority, tag, message)
    }
}