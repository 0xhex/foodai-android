package com.codepad.foodai.helpers

import timber.log.Timber

object Logger {
    fun logInformation(message: String) {
        Timber.i(message)
    }

    fun logError(exception: Exception, message: String) {
        Timber.e(exception, message)
    }
}