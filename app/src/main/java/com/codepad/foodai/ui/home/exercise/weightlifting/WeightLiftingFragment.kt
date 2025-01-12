package com.codepad.foodai.ui.home.exercise.weightlifting

import com.codepad.foodai.R
import com.codepad.foodai.ui.home.exercise.base.BaseExerciseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightLiftingFragment : BaseExerciseFragment() {

    override fun getExerciseType(): String = "weightlifting"

    override fun getHeaderIcon(): Int = R.raw.dumbel

    override fun getHeaderTitle(): String = getString(R.string.weight_lifting)

    override fun getIntensityHighText(): String {
        return getString(R.string.weight_lifting_high_intensity)
    }

    override fun getIntensityMediumText(): String {
        return getString(R.string.weight_lifting_medium_intensity)
    }

    override fun getIntensityLowText(): String {
        return getString(R.string.weight_lifting_low_intensity)
    }

    override fun getLayoutId(): Int = R.layout.fragment_base_exercise
}