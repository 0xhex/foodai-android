package com.codepad.foodai.di

import android.content.Context
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