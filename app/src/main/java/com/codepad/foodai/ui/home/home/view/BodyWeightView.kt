package com.codepad.foodai.ui.home.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.codepad.foodai.R

class BodyWeightView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var txtWeightTarget: TextView
    private lateinit var txtCurrentWeight: TextView
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_body_weight_content, this, true)
        initializeViews()
    }

    private fun initializeViews() {
        txtWeightTarget = findViewById(R.id.txt_weight_target)
        txtCurrentWeight = findViewById(R.id.txt_current_weight)
        btnIncrease = findViewById(R.id.btn_increase)
        btnDecrease = findViewById(R.id.btn_decrease)
    }

    fun updateWeight(currentWeight: Double, targetWeight: Int, isMetric: Boolean) {
        val unit = if (isMetric) "kg" else "lb"
        txtWeightTarget.text = context.getString(R.string.weight_target, targetWeight, unit)
        txtCurrentWeight.text = "${currentWeight.toInt()} $unit"
    }

    fun setOnWeightChangeListener(onIncrease: () -> Unit, onDecrease: () -> Unit) {
        btnIncrease.setOnClickListener { onIncrease() }
        btnDecrease.setOnClickListener { onDecrease() }
    }
} 