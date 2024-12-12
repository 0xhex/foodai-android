package com.codepad.foodai.ui.user_property.rating

import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGenderBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingFragment() :
    BaseFragment<FragmentGenderBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_gender

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.selectedGender.observe(viewLifecycleOwner) { gender ->
            //updateButtonStyles(gender)
        }

    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedGender.value != null) {
            //sharedViewModel.selectGender(sharedViewModel.selectedGender.value!!)
        }
    }

}