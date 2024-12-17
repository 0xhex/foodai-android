package com.codepad.foodai.ui.user_property.gender

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGenderBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenderFragment : BaseFragment<FragmentGenderBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_gender
    override val hideBottomNavBar: Boolean = true

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.selectedGender.observe(viewLifecycleOwner) { gender ->
            updateButtonStyles(gender)
        }

        val isEdit = arguments?.getBoolean("isEdit") ?: false
        setPreDefinedDate(isEdit)
    }

    private fun setPreDefinedDate(edit: Boolean) {
        if (edit) {
            binding.tvTitle.visibility = View.GONE
            binding.tvSubtitle.visibility = View.GONE
            binding.nextButton.visibility = View.VISIBLE
            binding.clTopHeader.visibility = View.VISIBLE

            val gender = UserSession.user?.gender.orEmpty()
            updateButtonStyles(gender)
            sharedViewModel.selectGender(gender)

            binding.nextButton.setOnClickListener {
                sharedViewModel.updateGender()
                lifecycleScope.launch {
                    delay(1000)
                    findNavController().popBackStack()
                }
            }

            binding.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
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