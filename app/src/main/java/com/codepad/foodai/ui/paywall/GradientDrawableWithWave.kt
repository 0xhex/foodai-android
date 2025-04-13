package com.codepad.foodai.ui.paywall

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.sin

class GradientDrawableWithWave(
    private val startColor: Int,
    private val endColor: Int
) : Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val wavePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.argb(13, 255, 140, 0) // Orange with 5% opacity
    }

    private val path = Path()

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        // Draw gradient background
        paint.shader = LinearGradient(
            0f, 0f,
            0f, height,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(bounds, paint)

        // Draw wave pattern
        path.reset()
        path.moveTo(0f, height * 0.4f)

        // Create gentle wave pattern
        val amplitude = 15f
        val frequency = 2 * PI / width
        for (x in 0..width.toInt()) {
            val y = sin(x * frequency) * amplitude + height * 0.4f
            path.lineTo(x.toFloat(), y.toFloat())
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        canvas.drawPath(path, wavePaint)

        // Draw decorative circles
        val orangeCirclePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.argb(13, 255, 140, 0) // Orange with 5% opacity
        }

        val greenCirclePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.argb(13, 76, 175, 80) // Green with 5% opacity
        }

        // Draw orange circle
        canvas.drawCircle(
            width * 0.8f,
            height * 0.2f,
            width * 0.3f,
            orangeCirclePaint
        )

        // Draw green circle
        canvas.drawCircle(
            width * 0.2f,
            height * 0.3f,
            width * 0.25f,
            greenCirclePaint
        )
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        wavePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        wavePaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
} 