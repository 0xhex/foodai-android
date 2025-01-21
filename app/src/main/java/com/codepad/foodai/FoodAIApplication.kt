package com.codepad.foodai

import android.app.Application
import android.content.Context
import android.util.Log
import com.codepad.foodai.helpers.FirebaseRemoteConfigManager
import com.codepad.foodai.helpers.LocaleHelper
import com.codepad.foodai.helpers.ModelPreferencesManager
import com.codepad.foodai.helpers.UserSession
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class FoodAIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initModelPrefManager()
        UserSession.init(this)
        FirebaseRemoteConfigManager.setUpRemoteConfig()
        initOneSignal()
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        } else {
            plant(ReleaseTree())
        }
    }

    private fun initModelPrefManager() {
        ModelPreferencesManager.with(this)
    }

    private fun initOneSignal() {
        // Verbose Logging set to help debug issues, remove before releasing your app.
        if (BuildConfig.DEBUG) {
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
        }

        // OneSignal Initialization
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base)))
    }
}

internal class ReleaseTree : Timber.Tree() {
    protected override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Log.println(priority, tag, message)
    }
}