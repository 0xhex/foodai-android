package com.codepad.foodai.ui.user_property.reachinggoals

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentReachingGoalsBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReachingGoalsFragment : BaseFragment<FragmentReachingGoalsBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_reaching_goals

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.showWarning.observe(viewLifecycleOwner) { showWarning ->
            if (showWarning) {
                Toast.makeText(
                    requireContext(),
                    R.string.please_select_a_reaching_goal,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sharedViewModel.selectedReachingGoal.observe(viewLifecycleOwner) { goal ->
            updateButtonStyles(goal)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedReachingGoal.value != null) {
            sharedViewModel.selectReachingGoal(sharedViewModel.selectedReachingGoal.value!!)
        }
    }

    private fun updateButtonStyles(selectedGoal: String?) {
        val consistencyButton = binding.btnConsistency
        val unhealthyButton = binding.btnUnhealthy
        val supportButton = binding.btnSupport
        val busyButton = binding.btnBusy
        val mealButton = binding.btnMeal

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        when (selectedGoal) {
            "Lack of consistency" -> {
                consistencyButton.setBackgroundColor(selectedColor)
                consistencyButton.setTextColor(selectedTextColor)
                unhealthyButton.setBackgroundColor(unselectedColor)
                unhealthyButton.setTextColor(unselectedTextColor)
                supportButton.setBackgroundColor(unselectedColor)
                supportButton.setTextColor(unselectedTextColor)
                busyButton.setBackgroundColor(unselectedColor)
                busyButton.setTextColor(unselectedTextColor)
                mealButton.setBackgroundColor(unselectedColor)
                mealButton.setTextColor(unselectedTextColor)
            }
            "Unhealthy eating habits" -> {
                unhealthyButton.setBackgroundColor(selectedColor)
                unhealthyButton.setTextColor(selectedTextColor)
                consistencyButton.setBackgroundColor(unselectedColor)
                consistencyButton.setTextColor(unselectedTextColor)
                supportButton.setBackgroundColor(unselectedColor)
                supportButton.setTextColor(unselectedTextColor)
                busyButton.setBackgroundColor(unselectedColor)
                busyButton.setTextColor(unselectedTextColor)
                mealButton.setBackgroundColor(unselectedColor)
                mealButton.setTextColor(unselectedTextColor)
            }
            "Lack of support" -> {
                supportButton.setBackgroundColor(selectedColor)
                supportButton.setTextColor(selectedTextColor)
                consistencyButton.setBackgroundColor(unselectedColor)
                consistencyButton.setTextColor(unselectedTextColor)
                unhealthyButton.setBackgroundColor(unselectedColor)
                unhealthyButton.setTextColor(unselectedTextColor)
                busyButton.setBackgroundColor(unselectedColor)
                busyButton.setTextColor(unselectedTextColor)
                mealButton.setBackgroundColor(unselectedColor)
                mealButton.setTextColor(unselectedTextColor)
            }
            "Busy schedule" -> {
                busyButton.setBackgroundColor(selectedColor)
                busyButton.setTextColor(selectedTextColor)
                consistencyButton.setBackgroundColor(unselectedColor)
                consistencyButton.setTextColor(unselectedTextColor)
                unhealthyButton.setBackgroundColor(unselectedColor)
                unhealthyButton.setTextColor(unselectedTextColor)
                supportButton.setBackgroundColor(unselectedColor)
                supportButton.setTextColor(unselectedTextColor)
                mealButton.setBackgroundColor(unselectedColor)
                mealButton.setTextColor(unselectedTextColor)
            }
            "Lack of meal inspiration" -> {
                mealButton.setBackgroundColor(selectedColor)
                mealButton.setTextColor(selectedTextColor)
                consistencyButton.setBackgroundColor(unselectedColor)
                consistencyButton.setTextColor(unselectedTextColor)
                unhealthyButton.setBackgroundColor(unselectedColor)
                unhealthyButton.setTextColor(unselectedTextColor)
                supportButton.setBackgroundColor(unselectedColor)
                supportButton.setTextColor(unselectedTextColor)
                busyButton.setBackgroundColor(unselectedColor)
                busyButton.setTextColor(unselectedTextColor)
            }
            else -> {
                consistencyButton.setBackgroundColor(unselectedColor)
                consistencyButton.setTextColor(unselectedTextColor)
                unhealthyButton.setBackgroundColor(unselectedColor)
                unhealthyButton.setTextColor(unselectedTextColor)
                supportButton.setBackgroundColor(unselectedColor)
                supportButton.setTextColor(unselectedTextColor)
                busyButton.setBackgroundColor(unselectedColor)
                busyButton.setTextColor(unselectedTextColor)
                mealButton.setBackgroundColor(unselectedColor)
                mealButton.setTextColor(unselectedTextColor)
            }
        }
    }
}