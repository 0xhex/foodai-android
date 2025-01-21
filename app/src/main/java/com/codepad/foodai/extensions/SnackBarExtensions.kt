package com.codepad.foodai.extensions

import android.view.Gravity
import android.widget.FrameLayout
import com.codepad.foodai.R
import com.codepad.foodai.helpers.ResourceHelper
import com.google.android.material.snackbar.Snackbar

fun Snackbar.applyStyle() {
    val resourceHelper = ResourceHelper(this.context)
    this.apply {
        setTextColor(
            resourceHelper.getColor(R.color.custom_black)
        )
        view.apply {
            setBackgroundResource(R.drawable.bg_snackbar_rounded_two)
            val params = layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.width = FrameLayout.LayoutParams.WRAP_CONTENT
            layoutParams = params
        }
    }
}

fun Snackbar.applyPaywallStyle() {
    val resourceHelper = ResourceHelper(this.context)
    this.apply {
        setTextColor(
            resourceHelper.getColor(R.color.white)
        )
        view.apply {
            setBackgroundResource(R.drawable.snackbar_background)
            val params = layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.width = FrameLayout.LayoutParams.WRAP_CONTENT
            setTextMaxLines(10)
            layoutParams = params
        }
    }
}