package com.codepad.foodai.helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceHelper @Inject constructor(@ApplicationContext context: Context) {

    private val contextRef = WeakReference(context)

    fun getDimensionPixelSize(@DimenRes id: Int): Int {
        return getResources().getDimensionPixelSize(id)
    }

    fun getInteger(@IntegerRes id: Int): Int {
        return getResources().getInteger(id)
    }

    fun getString(@StringRes id: Int): String {
        return getResources().getString(id)
    }

    @Suppress("SpreadOperator")
    fun getString(@StringRes id: Int, vararg formatArgs: Any): String {
        return getResources().getString(id, *formatArgs)
    }

    @ColorInt
    fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(getContext(), id)
    }

    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(getContext(), id)
    }

    private fun getResources(): Resources {
        return getContext().resources
    }

    private fun getContext(): Context {
        val context = contextRef.get()
        // check if there is a memory leak
        require(context != null) {
            "context reference is collected by gc"
        }
        return ContextCompat.getContextForLanguage(context)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceHelper) return false

        if (contextRef.get() != other.contextRef.get()) return false

        return true
    }

    override fun hashCode(): Int {
        return contextRef.get().hashCode()
    }
}
