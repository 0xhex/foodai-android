package com.codepad.foodai.helpers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(SupportedLanguage.fromCode(language).code)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun getLanguage(context: Context): String {
        val config: Configuration = context.resources.configuration
        return config.locales.get(0).language
    }

    fun provideFormattingLocale(): Locale {
        return Locale.getDefault()
    }
}