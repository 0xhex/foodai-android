package com.codepad.foodai.ui.user_property.result

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.codepad.foodai.R

class MacroCircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var title: String = ""
    private var value: String = ""
    private var color: Int = ContextCompat.getColor(context, R.color.black)
    private var isEditableIcon: Boolean = false
    private var isHomeScreen: Boolean = false

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = 48f
        color = ContextCompat.getColor(context, R.color.gray)
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MacroCircleView,
            0, 0
        ).apply {
            try {
                title = getString(R.styleable.MacroCircleView_title) ?: ""
                value = getString(R.styleable.MacroCircleView_value) ?: ""
                color = getColor(R.styleable.MacroCircleView_color, color)
                isEditableIcon = getBoolean(R.styleable.MacroCircleView_isEditableIcon, false)
                isHomeScreen = getBoolean(R.styleable.MacroCircleView_isHomeScreen, false)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) - 20

        // Draw background circle
        paint.color = ContextCompat.getColor(context, R.color.white)
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw progress circle
        paint.color = color
        val sweepAngle = 270f // 75% of 360 degrees
        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            -90f, sweepAngle, false, paint
        )

        // Draw value text
        canvas.drawText(value, centerX, centerY + textPaint.textSize / 4, textPaint)

        // Draw title text
        textPaint.textSize = 32f
        canvas.drawText(title, centerX, centerY - radius - 20, textPaint)

        // Draw editable icon if needed
        if (isEditableIcon) {
            val iconSize = 40
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_edit)
            icon?.setBounds(
                (width - iconSize - 20), (height - iconSize - 20),
                (width - 20), (height - 20)
            )
            icon?.draw(canvas)
        }
    }
}