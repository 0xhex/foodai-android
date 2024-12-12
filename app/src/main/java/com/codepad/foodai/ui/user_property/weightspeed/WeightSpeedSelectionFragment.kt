package com.codepad.foodai.ui.user_property.weightspeed

import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentWeightSelectionSpeedBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightSpeedSelectionFragment(private val isGain: Boolean) :
    BaseFragment<FragmentWeightSelectionSpeedBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_weight_selection_speed

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.selectedGender.observe(viewLifecycleOwner) { gender ->
            //updateButtonStyles(gender)
        }

        binding.tvSubtitle.text = if (isGain) {
            resources.getString(R.string.gain_weight_speed)
        } else {
            resources.getString(R.string.lose_weight_speed)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val weightLossPerWeek = progress / 10.0
                binding.currentKg.text = String.format("%.1f kg", weightLossPerWeek)
                binding.tvDynamicText.text = recommendedText(weightLossPerWeek)
                sharedViewModel.setWeightSpeed(weightLossPerWeek)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun recommendedText(weight: Double): String {
        return when (weight) {
            in 0.0..0.4 -> getString(R.string.slow_and_steady)
            in 0.4..1.1 -> getString(R.string.recommended)
            in 1.1..1.5 -> getString(R.string.you_may_feel_very_tired_and_develop_loose_skin)
            else -> getString(R.string.recommended)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedGender.value != null) {
            //sharedViewModel.selectGender(sharedViewModel.selectedGender.value!!)
        }
    }

}