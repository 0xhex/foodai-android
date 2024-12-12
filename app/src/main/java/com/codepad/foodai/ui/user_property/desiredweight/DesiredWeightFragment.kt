package com.codepad.foodai.ui.user_property.desiredweight

import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentDesiredWeightBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import com.codepad.foodai.ui.user_property.heightweight.MeasurementUnit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DesiredWeightFragment(val isGain: Boolean) : BaseFragment<FragmentDesiredWeightBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_desired_weight

    override fun onReadyView() {
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupWeightPicker()
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.desiredWeight.value != null) {
            binding.weightPicker.value = sharedViewModel.desiredWeight.value!!
        }
    }

    private fun setupWeightPicker() {
        val userWeight = sharedViewModel.weight.value ?: 60
        val isMetric = sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC

        binding.weightPicker.minValue =
            if (isMetric) (userWeight * 0.7).toInt() else (userWeight * 0.5).toInt()
        binding.weightPicker.maxValue =
            if (isMetric) (userWeight * 1.5).toInt() else (userWeight * 1.5).toInt()
        binding.weightPicker.value = userWeight
        binding.weightPicker.setFormatter { value -> if (isMetric) "$value kg" else "$value lb" }
        binding.weightPicker.setOnValueChangedListener { _, _, newVal ->
            sharedViewModel.setDesiredWeight(newVal)
        }
    }

}