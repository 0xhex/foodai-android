package com.codepad.foodai.ui.user_property.accomplish

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentAccomplishBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccomplishFragment : BaseFragment<FragmentAccomplishBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_accomplish

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.showWarning.observe(viewLifecycleOwner) { showWarning ->
            if (showWarning) {
                Toast.makeText(
                    requireContext(),
                    R.string.please_select_an_accomplishment,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sharedViewModel.selectedAccomplishment.observe(viewLifecycleOwner) { accomplishment ->
            updateButtonStyles(accomplishment)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedAccomplishment.value != null) {
            sharedViewModel.selectAccomplishment(sharedViewModel.selectedAccomplishment.value!!)
        }
    }

    private fun updateButtonStyles(selectedAccomplishment: String?) {
        val eatHealthierButton = binding.btnEatHealthier
        val boostEnergyMoodButton = binding.btnBoostEnergyMood
        val stayMotivatedConsistentButton = binding.btnStayMotivatedConsistent
        val feelBetterBodyButton = binding.btnFeelBetterBody

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        when (selectedAccomplishment) {
            "eat_healthier" -> {
                eatHealthierButton.setBackgroundColor(selectedColor)
                eatHealthierButton.setTextColor(selectedTextColor)
                boostEnergyMoodButton.setBackgroundColor(unselectedColor)
                boostEnergyMoodButton.setTextColor(unselectedTextColor)
                stayMotivatedConsistentButton.setBackgroundColor(unselectedColor)
                stayMotivatedConsistentButton.setTextColor(unselectedTextColor)
                feelBetterBodyButton.setBackgroundColor(unselectedColor)
                feelBetterBodyButton.setTextColor(unselectedTextColor)
            }
            "boost_energy_mood" -> {
                boostEnergyMoodButton.setBackgroundColor(selectedColor)
                boostEnergyMoodButton.setTextColor(selectedTextColor)
                eatHealthierButton.setBackgroundColor(unselectedColor)
                eatHealthierButton.setTextColor(unselectedTextColor)
                stayMotivatedConsistentButton.setBackgroundColor(unselectedColor)
                stayMotivatedConsistentButton.setTextColor(unselectedTextColor)
                feelBetterBodyButton.setBackgroundColor(unselectedColor)
                feelBetterBodyButton.setTextColor(unselectedTextColor)
            }
            "stay_motivated_consistent" -> {
                stayMotivatedConsistentButton.setBackgroundColor(selectedColor)
                stayMotivatedConsistentButton.setTextColor(selectedTextColor)
                eatHealthierButton.setBackgroundColor(unselectedColor)
                eatHealthierButton.setTextColor(unselectedTextColor)
                boostEnergyMoodButton.setBackgroundColor(unselectedColor)
                boostEnergyMoodButton.setTextColor(unselectedTextColor)
                feelBetterBodyButton.setBackgroundColor(unselectedColor)
                feelBetterBodyButton.setTextColor(unselectedTextColor)
            }
            "feel_better_body" -> {
                feelBetterBodyButton.setBackgroundColor(selectedColor)
                feelBetterBodyButton.setTextColor(selectedTextColor)
                eatHealthierButton.setBackgroundColor(unselectedColor)
                eatHealthierButton.setTextColor(unselectedTextColor)
                boostEnergyMoodButton.setBackgroundColor(unselectedColor)
                boostEnergyMoodButton.setTextColor(unselectedTextColor)
                stayMotivatedConsistentButton.setBackgroundColor(unselectedColor)
                stayMotivatedConsistentButton.setTextColor(unselectedTextColor)
            }
            else -> {
                eatHealthierButton.setBackgroundColor(unselectedColor)
                eatHealthierButton.setTextColor(unselectedTextColor)
                boostEnergyMoodButton.setBackgroundColor(unselectedColor)
                boostEnergyMoodButton.setTextColor(unselectedTextColor)
                stayMotivatedConsistentButton.setBackgroundColor(unselectedColor)
                stayMotivatedConsistentButton.setTextColor(unselectedTextColor)
                feelBetterBodyButton.setBackgroundColor(unselectedColor)
                feelBetterBodyButton.setTextColor(unselectedTextColor)
            }
        }
    }
}