package com.codepad.foodai.ui.home.home.pager.goals

import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoalViewBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalViewFragment : BaseFragment<FragmentGoalViewBinding>() {
    private val viewModel: HomePagerViewModel by activityViewModels()
    override fun getLayoutId(): Int = R.layout.fragment_goal_view

    override fun onReadyView() {
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
    }
}