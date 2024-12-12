package com.codepad.foodai.ui.user_property.weightspeed

import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGenderBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightSpeedSelectionFragment(private val isGain: Boolean) :
    BaseFragment<FragmentGenderBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_gender

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
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedGender.value != null) {
            //sharedViewModel.selectGender(sharedViewModel.selectedGender.value!!)
        }
    }

}