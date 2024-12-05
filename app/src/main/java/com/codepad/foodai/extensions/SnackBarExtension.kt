package com.codepad.foodai.extensions

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.snackbar.Snackbar

fun Snackbar.addIcon(
    drawable: Int,
    padding: Int,
    horizontalPadding: Int = 0,
    applyTint: Boolean = false,
    tintColor: Int? = null,
) {
    val snackTextView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    val drawableIcon = context.getDrawable(drawable)?.let {
        DrawableCompat.wrap(it)
    }
    drawableIcon?.let {
        if (applyTint) {
            if (tintColor != null) {
                DrawableCompat.setTint(it, tintColor)
            }
        }
        it.setBounds(
            horizontalPadding,
            0,
            it.intrinsicWidth + horizontalPadding,
            it.intrinsicHeight
        )
    }
    with(snackTextView) {
        setCompoundDrawablesWithIntrinsicBounds(drawableIcon, null, null, null)
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        gravity = Gravity.CENTER_VERTICAL
        setTypeface(typeface, Typeface.BOLD)
        compoundDrawablePadding = padding
    }
}