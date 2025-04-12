package com.codepad.foodai.ui.home.home.fooddetail

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.codepad.foodai.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#33CCCCCC") // 20% opacity gray
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    private val rectF = RectF()
    private var progress = 0f // 0 to 100
    private var progressColor = ContextCompat.getColor(context, R.color.orange)

    fun setProgress(value: Int, color: Int? = null) {
        progress = value.coerceIn(0, 100).toFloat() / 100f
        color?.let { progressColor = it }
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val strokeWidth = backgroundPaint.strokeWidth
        
        // Draw background circle
        rectF.set(
            strokeWidth / 2,
            strokeWidth / 2,
            width - strokeWidth / 2,
            height - strokeWidth / 2
        )
        canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)
        
        // Draw progress
        progressPaint.color = progressColor
        canvas.drawArc(rectF, -90f, 360f * progress, false, progressPaint)
    }
} 