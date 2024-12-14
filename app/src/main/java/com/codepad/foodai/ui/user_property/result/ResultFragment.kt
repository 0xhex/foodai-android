// ResultFragment.kt
package com.codepad.foodai.ui.user_property.result

import android.graphics.Color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentResultBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : BaseFragment<FragmentResultBinding>() {

    private val viewModel: ResultViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_result

    override fun onReadyView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.nextButton.setOnClickListener {
            viewModel.navigateToNextScreen()
            findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
        }

        setupEditButtonListeners()

        setFragmentResultListener("updateRequestKey") { _, bundle ->
            if (bundle.getBoolean("updated")) {
                viewModel.fetchNutrition()
            }
        }

        viewModel.fetchNutrition()
    }

    private fun setupEditButtonListeners() {
        val editActions = listOf(
            Triple(binding.layoutCarbs.root, Pair("Carbs", "dailyCarb"), "#ff9500"),
            Triple(binding.layoutProtein.root, Pair("Protein", "dailyProtein"), "#ff3b30"),
            Triple(binding.layoutFats.root, Pair("Fats", "dailyFat"), "#007aff"),
            Triple(binding.layoutCalories.root, Pair("Calories", "dailyCalories"), "#FFFFFF")
        )

        editActions.forEach { (button, nutritionType, color) ->
            button.setOnClickListener {
                val value = when (nutritionType.second) {
                    "dailyCarb" -> viewModel.carbs.value?.value ?: "0"
                    "dailyProtein" -> viewModel.protein.value?.value ?: "0"
                    "dailyFat" -> viewModel.fats.value?.value ?: "0"
                    "dailyCalories" -> viewModel.calories.value?.value ?: "0"
                    else -> "0"
                }
                findNavController().navigate(
                    ResultFragmentDirections.actionResultFragmentToEditCaloriesFragment(
                        nutritionType.first,
                        "Enter ${nutritionType.first}",
                        nutritionType.second,
                        value,
                        Color.parseColor(color)
                    )
                )
            }
        }
    }
}