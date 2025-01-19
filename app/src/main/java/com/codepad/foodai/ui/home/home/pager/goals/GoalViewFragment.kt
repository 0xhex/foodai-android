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
            binding.clProgressOne.progress = it.toFloat()
        }

        viewModel.proteinAchievedPercent.observe(viewLifecycleOwner) {
            binding.clProgressTwo.progress = it.toFloat()
        }

        viewModel.carbsAchievedPercent.observe(viewLifecycleOwner) {
            binding.clProgressThree.progress = it.toFloat()
        }

        viewModel.fatsAchievedPercent.observe(viewLifecycleOwner) {
            binding.clProgressFour.progress = it.toFloat()
        }
    }

    private fun navigateToGoal() {
        findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToAdjustGoalsFragment())
    }
}