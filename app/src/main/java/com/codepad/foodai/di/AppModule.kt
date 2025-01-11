package com.codepad.foodai.di

import android.content.Context
import android.content.SharedPreferences
import androidx.health.connect.client.HealthConnectClient
import com.codepad.foodai.ui.home.settings.health.HealthConnectReader
import com.codepad.foodai.ui.home.settings.health.HealthConnectManager
import com.google.firebase.analytics.FirebaseAnalytics
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

    // TODO
    // @Provides
    // @Singleton
    // fun provideRevenueCatManager(
    //     @ApplicationContext context: Context,
    //     firebaseManager: FirebaseManager,
    // ): RevenueCatManager {
    //     return RevenueCatManager(context, firebaseManager)
    // }
}