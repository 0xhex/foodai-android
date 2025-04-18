package com.codepad.foodai.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.codepad.foodai.R
import kotlin.math.min

class CalorieProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val caloriesTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val regularTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var progress: Float = 0f
    private var overProgress: Float = 0f
    private var remainingCalories: String = "0"

    private val startAngle = 135f
    private val sweepAngle = 270f
    private val strokeWidth = context.resources.getDimensionPixelSize(R.dimen.dimen_8dp).toFloat()

    private val boldTypeface = ResourcesCompat.getFont(context, R.font.euro_stile_bold)
    private val regularTypeface = ResourcesCompat.getFont(context, R.font.euro_stile_regular)

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND

        caloriesTextPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = boldTypeface
        }

        regularTextPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = regularTypeface
        }
    }

    fun setProgress(consumed: Int, total: Int) {
        if (total > 0) {
            val ratio = consumed.toFloat() / total.toFloat()
            progress = min(ratio, 1f)
            overProgress = if (ratio > 1f) min((ratio - 1f), 0.2f) else 0f
            remainingCalories = (total - consumed).toString()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val size = min(width, height).toFloat()
        val padding = strokeWidth / 2
        rectF.set(padding, padding, size - padding, size - padding)

        // Background arc
        paint.color = ContextCompat.getColor(context, R.color.orange_start)
        paint.alpha = 51 // 20% opacity
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

        // Progress arc
        paint.color = ContextCompat.getColor(context, R.color.orange)
        paint.alpha = 255
        val progressSweep = progress * sweepAngle
        canvas.drawArc(rectF, startAngle, progressSweep, false, paint)

        // Over progress arc (red)
        if (overProgress > 0) {
            paint.color = ContextCompat.getColor(context, R.color.red)
            val overProgressSweep = overProgress * sweepAngle
            canvas.drawArc(rectF, startAngle + progressSweep, overProgressSweep, false, paint)
        }

        // Draw remaining calories text
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw calories number with bold font
        caloriesTextPaint.textSize = size * 0.15f
        canvas.drawText(remainingCalories, centerX, centerY, caloriesTextPaint)

        // Draw "kcal" text with regular font
        regularTextPaint.textSize = size * 0.08f
        canvas.drawText("kcal", centerX, centerY + regularTextPaint.textSize * 1.2f, regularTextPaint)

        // Draw "Remain" text with regular font
        regularTextPaint.textSize = size * 0.1f
        canvas.drawText(context.getString(R.string.remain), centerX, centerY + regularTextPaint.textSize * 2.4f, regularTextPaint)
    }
} 