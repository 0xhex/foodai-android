package com.codepad.foodai.ui.home.settings.goals

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoalsBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdjustGoalsFragment : BaseFragment<FragmentGoalsBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int {
        return R.layout.fragment_goals
    }

    override fun onReadyView() {
        fetchNutrition()

        binding.btnRevert.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDone.setOnClickListener {
            saveGoals()
            lifecycleScope.launch {
                delay(1000)
                findNavController().popBackStack()
            }
        }

        viewModel.calories.observe(viewLifecycleOwner) {
            binding.rowCalories.etValue.setText(it.value)
            binding.rowCalories.imgIcon.setImageResource(it.color)
            binding.rowCalories.txtTitle.text = it.title
        }

        viewModel.carbs.observe(viewLifecycleOwner) {
            binding.rowCarbs.etValue.setText(it.value)
            binding.rowCarbs.imgIcon.setImageResource(it.color)
            binding.rowCarbs.txtTitle.text = it.title
        }

        viewModel.protein.observe(viewLifecycleOwner) {
            binding.rowProtein.etValue.setText(it.value)
            binding.rowProtein.imgIcon.setImageResource(it.color)
            binding.rowProtein.txtTitle.text = it.title
        }

        viewModel.fats.observe(viewLifecycleOwner) {
            binding.rowFats.etValue.setText(it.value)
            binding.rowFats.imgIcon.setImageResource(it.color)
            binding.rowFats.txtTitle.text = it.title
        }
    }

    private fun fetchNutrition() {
        viewModel.fetchNutrition()
    }

    private fun saveGoals() {
        val calories = binding.rowCalories.etValue.text.toString().toIntOrNull()
        val carbs = binding.rowCarbs.etValue.text.toString().toIntOrNull()
        val protein = binding.rowProtein.etValue.text.toString().toIntOrNull()
        val fats = binding.rowFats.etValue.text.toString().toIntOrNull()

        if (calories != null && carbs != null && protein != null && fats != null) {
            viewModel.saveGoals(calories, carbs, protein, fats)
        } else {
            // Handle invalid input
        }
    }
}