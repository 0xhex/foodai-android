package com.codepad.foodai.ui.home.home.fooddetail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.image.ImageData

class NutritionDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var titleText: TextView
    private lateinit var caloriesValue: TextView
    private lateinit var proteinValue: TextView
    private lateinit var carbsValue: TextView
    private lateinit var fatsValue: TextView
    private lateinit var fiberValue: TextView
    private lateinit var sugarValue: TextView
    private lateinit var sodiumValue: TextView
    private lateinit var cholesterolValue: TextView

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.view_nutrition_details, this, true)

        // Initialize views
        titleText = view.findViewById(R.id.lblNutritionDetails)
        caloriesValue = view.findViewById(R.id.txtCaloriesValue)
        proteinValue = view.findViewById(R.id.txtProteinValue)
        carbsValue = view.findViewById(R.id.txtCarbsValue)
        fatsValue = view.findViewById(R.id.txtFatsValue)
        fiberValue = view.findViewById(R.id.txtFiberValue)
        sugarValue = view.findViewById(R.id.txtSugarValue)
        sodiumValue = view.findViewById(R.id.txtSodiumValue)
        cholesterolValue = view.findViewById(R.id.txtCholesterolValue)
        
        // Set title text
        titleText.text = context.getString(R.string.nutritional_information)
        titleText.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    fun setNutritionData(imageData: ImageData) {
        // Set values with appropriate units and formatted values
        imageData.calories?.let { calories ->
            caloriesValue.text = "$calories kcal"
        }
        
        imageData.protein?.let { protein ->
            proteinValue.text = formatNutrientValue(protein, "g")
        }
        
        imageData.carbs?.let { carbs ->
            carbsValue.text = formatNutrientValue(carbs, "g")
        }
        
        imageData.fats?.let { fats ->
            fatsValue.text = formatNutrientValue(fats, "g")
        }
        
        // For other nutrients, use default values if they're null
        //fiberValue.text = formatNutrientValue(imageData.fiber ?: 0, "g")
        //sugarValue.text = formatNutrientValue(imageData.sugar ?: 0, "g")
        //sodiumValue.text = formatNutrientValue(imageData.sodium ?: 0, "mg")
        //cholesterolValue.text = formatNutrientValue(imageData.cholesterol ?: 0, "mg")
    }
    
    private fun formatNutrientValue(value: Int, unit: String): String {
        return "$value$unit"
    }
} 