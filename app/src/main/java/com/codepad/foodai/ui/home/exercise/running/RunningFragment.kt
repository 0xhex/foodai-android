package com.codepad.foodai.ui.home.exercise.running

import com.codepad.foodai.R
import com.codepad.foodai.ui.home.exercise.base.BaseExerciseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunningFragment : BaseExerciseFragment() {

    override fun getExerciseType(): String = "run"

    override fun getHeaderIcon(): Int = R.raw.running

    override fun getHeaderTitle(): String = getString(R.string.run)
    override fun getIntensityHighText(): String {
        return getString(R.string.running_high_intensity)
    }

    override fun getIntensityMediumText(): String {
        return getString(R.string.running_medium_intensity)
    }

    override fun getIntensityLowText(): String {
        return getString(R.string.running_low_intensity)
    }

    override fun getLayoutId(): Int = R.layout.fragment_base_exercise

    override fun onReadyView() {}
} 