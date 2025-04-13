package com.codepad.foodai.ui.home.home.pager.goals

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoalViewBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.custom.MacroProgressBar
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
        setupClickListeners()
        setupObservers()
        setupMacroViews()
    }

    private fun setupClickListeners() {
        binding.root.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToNewResultFragment(true))
        }
    }

    private fun setupObservers() {
        viewModel.dailySummary.observe(viewLifecycleOwner) { summary ->
            // Update consumed calories
            binding.consumedCalories.text = summary.meals?.sumOf { it.calories ?: 0 }?.toString() ?: "0"
            
            // Update remaining calories
            val remainingCalories = summary.remainingNutrition?.calories ?: 0
            binding.calorieProgress.setProgress(
                consumed = summary.meals?.sumOf { it.calories ?: 0 } ?: 0,
                total = remainingCalories + (summary.meals?.sumOf { it.calories ?: 0 } ?: 0)
            )

            // Update burned calories
            binding.burnedCalories.text = (summary.exercises?.firstOrNull()?.caloriesBurned ?: 0).toString()

            // Update macros
            updateMacroProgress(
                binding.carbsProgress,
                getString(R.string.carbs),
                summary.meals?.sumOf { it.carbs ?: 0 } ?: 0,
                summary.remainingNutrition?.carbs ?: 0,
                ContextCompat.getColor(requireContext(), R.color.green)
            )

            updateMacroProgress(
                binding.proteinProgress,
                getString(R.string.protein),
                summary.meals?.sumOf { it.protein ?: 0 } ?: 0,
                summary.remainingNutrition?.protein ?: 0,
                ContextCompat.getColor(requireContext(), R.color.blue)
            )

            updateMacroProgress(
                binding.fatsProgress,
                getString(R.string.fats),
                summary.meals?.sumOf { it.fats ?: 0 } ?: 0,
                summary.remainingNutrition?.fat ?: 0,
                ContextCompat.getColor(requireContext(), R.color.orange)
            )
        }
    }

    private fun setupMacroViews() {
        // Initial setup of macro titles
        binding.carbsProgress.findViewById<TextView>(R.id.macro_title).text = getString(R.string.carbs)
        binding.proteinProgress.findViewById<TextView>(R.id.macro_title).text = getString(R.string.protein)
        binding.fatsProgress.findViewById<TextView>(R.id.macro_title).text = getString(R.string.fats)
    }

    private fun updateMacroProgress(
        view: View,
        title: String,
        consumed: Int,
        remaining: Int,
        color: Int
    ) {
        val total = consumed + remaining
        view.findViewById<MacroProgressBar>(R.id.progress_bar).setProgress(consumed, total, color)
        view.findViewById<TextView>(R.id.macro_value).text = "${consumed}g / ${total}g"
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}