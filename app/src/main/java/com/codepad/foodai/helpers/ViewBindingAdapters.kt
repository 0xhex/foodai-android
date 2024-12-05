package com.codepad.foodai.helpers

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.progressindicator.LinearProgressIndicator


@BindingAdapter("onFocused")
fun onFocused(view: View, doOnFocus: (() -> Unit)?) {
    view.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus && doOnFocus != null) {
            doOnFocus()
        }
    }
}

@BindingAdapter("android:onLongClick")
fun onLongClick(view: View, onClick: (() -> Unit)?) {
    if (onClick != null) {
        view.setOnLongClickListener {
            onClick()
            return@setOnLongClickListener true
        }
    } else {
        view.setOnLongClickListener(null)
    }
}

@BindingAdapter("backgroundCompat")
fun backgroundCompat(view: View, @DrawableRes drawableRes: Int) {
    if (drawableRes != 0) {
        view.background = AppCompatResources.getDrawable(view.context, drawableRes)
    }
}

@BindingAdapter("backgroundTint")
fun backgroundTint(view: View, @ColorRes color: Int) {
    if (color != 0) {
        view.backgroundTintList = ContextCompat.getColorStateList(view.context, color)
    }
}

@BindingAdapter("backgroundTintColor")
fun backgroundTintColor(view: View, @ColorInt color: Int) {
    if (color != 0) {
        view.backgroundTintList = ColorStateList.valueOf(color)
    }
}

@BindingAdapter("cardBackgroundColor")
fun cardBackgroundColor(view: CardView, @ColorRes color: Int) {
    if (color != 0) {
        view.setCardBackgroundColor(ContextCompat.getColorStateList(view.context, color))
    }
}

@BindingAdapter("cardBackgroundColorHex")
fun cardBackgroundColorHex(view: CardView, hex: String?) {
    if (!hex.isNullOrEmpty()) {
        view.setCardBackgroundColor(Color.parseColor(hex))
    }
}

@BindingAdapter("backgroundColor")
fun setBackgroundColor(view: View, @ColorRes color: Int) {
    if (color != 0) {
        view.setBackgroundColor(ContextCompat.getColor(view.context, color))
    }
}

@BindingAdapter("invisibleUnless")
fun invisibleUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) VISIBLE else INVISIBLE
}

@BindingAdapter("goneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) VISIBLE else GONE
}

@BindingAdapter("textDynamicColor")
fun textDynamicColor(textView: TextView, color: Int) {
    textView.setTextColor(ContextCompat.getColor(textView.context, color))
}

@BindingAdapter("android:layout_height")
fun setLayoutHeight(view: View, height: Int) {
    val layoutParams = view.layoutParams
    layoutParams.height = height
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_width")
fun setLayoutWidth(view: View, width: Int) {
    val layoutParams = view.layoutParams
    layoutParams.width = width
    view.layoutParams = layoutParams
}


@BindingAdapter(
    "android:layout_marginRight",
    "android:layout_marginLeft",
    "android:layout_marginTop",
    "android:layout_marginBottom",
    requireAll = false
)
fun setMargin(
    view: View,
    marginRight: Float?,
    marginLeft: Float?,
    marginTop: Float?,
    marginBottom: Float?,
) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    val leftMargin = marginLeft?.toInt() ?: layoutParams.leftMargin
    val topMargin = marginTop?.toInt() ?: layoutParams.topMargin
    val rightMargin = marginRight?.toInt() ?: layoutParams.rightMargin
    val bottomMargin = marginBottom?.toInt() ?: layoutParams.bottomMargin
    layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
    view.layoutParams = layoutParams
}

@BindingAdapter(
    "android:paddingLeft",
    "android:paddingTop",
    "android:paddingRight",
    "android:paddingBottom",
    requireAll = false
)
fun setPadding(
    view: View,
    paddingLeft: Float?,
    paddingTop: Float?,
    paddingRight: Float?,
    paddingBottom: Float?,
) {
    view.setPadding(
        paddingLeft?.toInt() ?: view.paddingLeft,
        paddingTop?.toInt() ?: view.paddingTop,
        paddingRight?.toInt() ?: view.paddingRight,
        paddingBottom?.toInt() ?: view.paddingBottom
    )
}

@BindingAdapter("scrollToHere")
fun scrollToHere(view: View, scroll: Boolean?) {
    if (scroll == null || scroll == false) {
        return
    }
    val rect = Rect(0, 0, view.width, view.height)
    view.requestRectangleOnScreen(rect, false)
}

@BindingAdapter("verticalBias")
fun verticalBias(view: View, bias: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.verticalBias = bias
}

@BindingAdapter("horizontalBias")
fun horizontalBias(view: View, bias: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.horizontalBias = bias
}

@BindingAdapter(value = ["constraintPercent"])
fun setConstraintPercent(view: Guideline, percent: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.guidePercent = percent
    view.layoutParams = params
}

@BindingAdapter("linearProgressIndicatorColor")
fun backgroundTint(view: LinearProgressIndicator, @ColorRes color: Int) {
    if (color != 0) {
        view.trackColor = ContextCompat.getColor(view.context, color)
        view.setIndicatorColor(ContextCompat.getColor(view.context, color))
    }
}


@BindingAdapter("hexColor")
fun setHexColor(view: View, hexColor: String?) {
    if (!hexColor.isNullOrEmpty()) {
        try {
            val colorInt = Color.parseColor(hexColor)
            view.setBackgroundColor(colorInt)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}

@BindingAdapter("setHorizontalWeight")
fun setHorizontalWeight(view: View, weight: Int) {
    val param = LinearLayout.LayoutParams(
        view.layoutParams.width,
        view.layoutParams.height,
        weight.toFloat()
    )
    view.layoutParams = param
    view.invalidate()
}

@BindingAdapter("app:backgroundTint")
fun setBackgroundTint(view: ConstraintLayout, color: String?) {
    if (color.isNullOrEmpty()) {
        // Log or handle the case where color is null or empty
        return
    }

    try {
        val colorValue = Color.parseColor(color)
        val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            colorValue,
            BlendModeCompat.SRC_ATOP
        )
        view.background?.colorFilter = colorFilter
    } catch (e: IllegalArgumentException) {
        // Log the error or handle the invalid color format gracefully
    }
}