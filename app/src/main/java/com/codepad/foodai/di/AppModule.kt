package com.codepad.foodai.di

import android.content.Context
import android.content.SharedPreferences
import androidx.health.connect.client.HealthConnectClient
import com.codepad.foodai.BuildConfig
import com.codepad.foodai.helpers.FirebaseManager
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.ui.home.settings.health.HealthConnectManager
import com.codepad.foodai.ui.home.settings.health.HealthConnectReader
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Match iOS 3600 seconds (1 hour)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Set default values matching iOS implementation
        val defaults = hashMapOf(
            "firstProduct" to "foodai_weekly_tier1",
            "secondProduct" to "foodai_yearly_standart",
            "isPassedStore" to false,
            "specialEventDay" to false,
            "specialProduct" to "foodai_special_discount",
            "finishDate" to "2024-12-25T23:59:59Z"
        )
        remoteConfig.setDefaultsAsync(defaults)
        
        return remoteConfig
    }

    @Singleton
    @Provides
    fun provideHealthConnectClient(@ApplicationContext context: Context): HealthConnectClient {
        return HealthConnectClient.getOrCreate(context)
    }

    @Singleton
    @Provides
    fun provideHealthConnectReader(healthConnectClient: HealthConnectClient): HealthConnectReader {
        return HealthConnectReader(healthConnectClient)
    }

    @Singleton
    @Provides
    fun provideHealthConnectManager(
        healthConnectClient: HealthConnectClient, healthConnectReader: HealthConnectReader,
    ): HealthConnectManager {
        return HealthConnectManager(healthConnectClient, healthConnectReader)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideFirebaseManager(
        firebaseAnalytics: FirebaseAnalytics,
        remoteConfig: FirebaseRemoteConfig
    ): FirebaseManager {
        return FirebaseManager(firebaseAnalytics, remoteConfig)
    }

    @Provides
    @Singleton
    fun provideRevenueCatManager(
        @ApplicationContext context: Context,
        firebaseManager: FirebaseManager,
    ): RevenueCatManager {
        return RevenueCatManager(context, firebaseManager)
    }
}