package com.codepad.foodai.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * Extension function to observe LiveData events only once
 */
fun <T> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner) { event ->
        event.getContentIfNotHandled()?.let { value ->
            observer(value)
        }
    }
}