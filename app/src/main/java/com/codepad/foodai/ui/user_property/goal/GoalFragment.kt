package com.codepad.foodai.ui.user_property.goal

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoalBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalFragment : BaseFragment<FragmentGoalBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_goal

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.showWarning.observe(viewLifecycleOwner) { showWarning ->
            if (showWarning) {
                Toast.makeText(
                    requireContext(),
                    R.string.please_select_a_goal,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sharedViewModel.selectedGoal.observe(viewLifecycleOwner) { goal ->
            updateButtonStyles(goal)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedGoal.value != null) {
            sharedViewModel.selectGoal(sharedViewModel.selectedGoal.value!!)
        }
    }

    private fun updateButtonStyles(selectedGoal: String?) {
        val loseWeightButton = binding.btnLoseWeight
        val maintainButton = binding.btnMaintain
        val gainWeightButton = binding.btnGainWeight

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        when (selectedGoal) {
            "lose_weight" -> {
                loseWeightButton.setBackgroundColor(selectedColor)
                loseWeightButton.setTextColor(selectedTextColor)
                maintainButton.setBackgroundColor(unselectedColor)
                maintainButton.setTextColor(unselectedTextColor)
                gainWeightButton.setBackgroundColor(unselectedColor)
                gainWeightButton.setTextColor(unselectedTextColor)
            }
            "maintain" -> {
                maintainButton.setBackgroundColor(selectedColor)
                maintainButton.setTextColor(selectedTextColor)
                loseWeightButton.setBackgroundColor(unselectedColor)
                loseWeightButton.setTextColor(unselectedTextColor)
                gainWeightButton.setBackgroundColor(unselectedColor)
                gainWeightButton.setTextColor(unselectedTextColor)
            }
            "gain_weight" -> {
                gainWeightButton.setBackgroundColor(selectedColor)
                gainWeightButton.setTextColor(selectedTextColor)
                loseWeightButton.setBackgroundColor(unselectedColor)
                loseWeightButton.setTextColor(unselectedTextColor)
                maintainButton.setBackgroundColor(unselectedColor)
                maintainButton.setTextColor(unselectedTextColor)
            }
            else -> {
                loseWeightButton.setBackgroundColor(unselectedColor)
                loseWeightButton.setTextColor(unselectedTextColor)
                maintainButton.setBackgroundColor(unselectedColor)
                maintainButton.setTextColor(unselectedTextColor)
                gainWeightButton.setBackgroundColor(unselectedColor)
                gainWeightButton.setTextColor(unselectedTextColor)
            }
        }
    }
}