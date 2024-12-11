package com.codepad.foodai.ui.user_property.gender

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGenderBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenderFragment : BaseFragment<FragmentGenderBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_gender

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.selectedGender.observe(viewLifecycleOwner) { gender ->
            updateButtonStyles(gender)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedGender.value != null) {
            sharedViewModel.selectGender(sharedViewModel.selectedGender.value!!)
        }
    }

    private fun updateButtonStyles(selectedGender: String?) {
        val maleButton = binding.btnMale
        val femaleButton = binding.btnFemale

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        when (selectedGender) {
            "male" -> {
                maleButton.setBackgroundColor(selectedColor)
                maleButton.setTextColor(selectedTextColor)
                femaleButton.setBackgroundColor(unselectedColor)
                femaleButton.setTextColor(unselectedTextColor)
            }
            "female" -> {
                femaleButton.setBackgroundColor(selectedColor)
                femaleButton.setTextColor(selectedTextColor)
                maleButton.setBackgroundColor(unselectedColor)
                maleButton.setTextColor(unselectedTextColor)
            }
            else -> {
                maleButton.setBackgroundColor(unselectedColor)
                maleButton.setTextColor(unselectedTextColor)
                femaleButton.setBackgroundColor(unselectedColor)
                femaleButton.setTextColor(unselectedTextColor)
            }
        }
    }
}