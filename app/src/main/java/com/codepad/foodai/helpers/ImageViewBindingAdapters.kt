package com.codepad.foodai.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.codepad.foodai.extensions.orFalse

const val INVALID_RES_ID = 0

@BindingAdapter("srcCompat")
fun setSrcCompat(view: ImageView, @DrawableRes drawable: Int?) {
    if (drawable != null && drawable != INVALID_RES_ID) {
        view.setImageResource(drawable)
    }
}

@BindingAdapter("srcCompat")
fun setSrcCompat(view: ImageView, drawable: Drawable?) {
    view.setImageDrawable(drawable)
}

@BindingAdapter("setBitmap")
fun setBitmap(view: ImageView, bitmap: Bitmap?) {
    if (bitmap == null) {
        return
    }
    view.setImageBitmap(bitmap)
}

@BindingAdapter("tint")
fun setTint(view: ImageView, @ColorRes colorRes: Int?) {
    if (colorRes != null && colorRes != INVALID_RES_ID) {
        ImageViewCompat.setImageTintList(
            view,
            ColorStateList.valueOf(ContextCompat.getColor(view.context, colorRes))
        )
    }
}


@BindingAdapter(
    "imageUrl",
    "imageCornerRadius",
    "placeHolderDrawable",
    "onImageFail",
    "scaleTypeOnReady",
    "clearCache",
    requireAll = false
)
fun loadImage(
    view: ImageView,
    imageUrl: String?,
    imageCornerRadius: Int?,
    @DrawableRes placeHolderDrawable: Int?,
    onImageFail: (() -> Boolean)?,
    scaleTypeOnReady: ImageView.ScaleType?,
    clearCache: Boolean?
) {

    var convertedImageCornerRadius = 0F

    imageCornerRadius?.let {
        convertedImageCornerRadius = ViewUtils.dpToPx(view.context, it)
    }

    val context = view.context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val content = context.getActivity()
        if (content?.isDestroyed == true) {
            return
        }
    }
    val glide = try {
        Glide.with(context)
    } catch (t: Throwable) {
        return
    }

    var radius = convertedImageCornerRadius.toInt()

    var builder = if (imageUrl.isNullOrEmpty() && placeHolderDrawable != null) {
        glide.load(placeHolderDrawable)
    } else {
        glide.load(imageUrl)
    }

    if (radius > 0) {
        builder = builder.apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
    }

    if (clearCache.orFalse()) {
        builder.diskCacheStrategy(DiskCacheStrategy.NONE)
    }

    builder
        .apply {
            if (placeHolderDrawable != null && placeHolderDrawable != 0) {
                placeholder(placeHolderDrawable)
            }

        }
        .listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    return onImageFail?.invoke() ?: true
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (scaleTypeOnReady != null) {
                        (target as ImageViewTarget).view.scaleType = scaleTypeOnReady
                    }
                    return false
                }
            }
        )
        .into(view)
}

fun Context.getActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.getActivity()
        else -> null
    }
}

@BindingAdapter("setTint")
fun ImageView.setTint(color: Int) {
    setColorFilter(color, PorterDuff.Mode.SRC_IN)
}