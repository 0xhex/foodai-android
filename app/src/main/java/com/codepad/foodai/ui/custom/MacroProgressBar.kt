package com.codepad.foodai.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.codepad.foodai.R
import kotlin.math.min

class MacroProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var progress: Float = 0f
    private var overProgress: Float = 0f
    private var barColor: Int = ContextCompat.getColor(context, R.color.green)

    init {
        paint.style = Paint.Style.FILL
    }

    fun setProgress(consumed: Int, total: Int, color: Int) {
        if (total > 0) {
            val ratio = consumed.toFloat() / total.toFloat()
            progress = min(ratio, 1f)
            overProgress = if (ratio > 1f) min((ratio - 1f), 0.3f) else 0f
            barColor = color
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val cornerRadius = height / 2f
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())

        // Background
        paint.color = barColor
        paint.alpha = 51 // 20% opacity
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // Progress
        paint.alpha = 255
        rectF.right = width * progress
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // Over progress
        if (overProgress > 0) {
            paint.color = ContextCompat.getColor(context, R.color.red)
            rectF.left = width * progress
            rectF.right = width * (progress + overProgress)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        }
    }
} 