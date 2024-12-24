package com.codepad.foodai.ui.home.home.pager.goals

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoalViewBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeFragmentDirections
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalViewFragment : BaseFragment<FragmentGoalViewBinding>() {
    private val viewModel: HomePagerViewModel by activityViewModels()
    private val sharedViewModel: HomeViewModel by activityViewModels()
    override fun getLayoutId(): Int = R.layout.fragment_goal_view

    override fun onReadyView() {
        binding.cardCalorieGoal.setOnClickListener {
            navigateToGoal()
        }

        binding.cardProtein.setOnClickListener {
            navigateToGoal()
        }

        binding.cardCarbs.setOnClickListener {
            navigateToGoal()
        }

        binding.cardFats.setOnClickListener {
            navigateToGoal()
        }

        viewModel.dailySummary.observe(viewLifecycleOwner) {
            binding.apply {
                txtCount.text = it.remainingNutrition?.calories?.toString()
                txtBurnedCalories.text = getString(
                    R.string.colories_burned_count,
                    (it.exercises?.firstOrNull()?.caloriesBurned ?: 0).toString()
                )
                txtProteinTitle.text =
                    getString(R.string.macros_g, it.remainingNutrition?.protein.toString())
                txtCarbsTitle.text =
                    getString(R.string.macros_g, it.remainingNutrition?.carbs.toString())
                txtFatsTitle.text =
                    getString(R.string.macros_g, it.remainingNutrition?.fat.toString())
            }
        }

        viewModel.calorieAchievedPercent.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.clProgressOne.progress = it.toFloat()
                binding.clProgressOne.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.white)
            } else {
                binding.clProgressOne.progress = 100f
                binding.clProgressOne.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.color_graph_initial)
            }
        }

        viewModel.proteinAchievedPercent.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.clProgressTwo.progress = it.toFloat()
                binding.clProgressTwo.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.red)
            } else {
                binding.clProgressTwo.progress = 100f
                binding.clProgressTwo.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.color_graph_initial)
            }
        }

        viewModel.carbsAchievedPercent.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.clProgressThree.progress = it.toFloat()
                binding.clProgressThree.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.orange)
            } else {
                binding.clProgressThree.progress = 100f
                binding.clProgressThree.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.color_graph_initial)
            }
        }

        viewModel.fatsAchievedPercent.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.clProgressFour.progress = it.toFloat()
                binding.clProgressFour.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.blue_button)
            } else {
                binding.clProgressFour.progress = 100f
                binding.clProgressFour.progressBarColor =
                    ContextCompat.getColor(requireContext(), R.color.color_graph_initial)
            }
        }
    }

    private fun navigateToGoal() {
        findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToAdjustGoalsFragment())
    }
}