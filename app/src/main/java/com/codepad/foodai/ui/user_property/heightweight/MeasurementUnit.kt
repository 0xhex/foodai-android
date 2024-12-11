package com.codepad.foodai.ui.user_property.heightweight

enum class MeasurementUnit {
    METRIC, IMPERIAL
}

data class HeightWeightModel(
    var heightCM: Int? = 160,
    var weightKG: Int? = 60,
    var heightFT: Int? = 5,
    var heightIN: Int? = 3,
    var weightLB: Int? = 132,
)