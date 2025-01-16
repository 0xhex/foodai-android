package com.codepad.foodai.ui.home.analytics

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentAnalyticsTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsTabFragment : BaseFragment<FragmentAnalyticsTabBinding>() {

    private val viewModel: AnalyticsViewModel by viewModels()
    override fun getLayoutId(): Int = R.layout.fragment_analytics_tab
    override fun onReadyView() {
        setupViews()
        setupObservers()
        viewModel.fetchUserData()
    }

    private fun setupViews() {
        binding.apply {
            btnUpdateGoal.setOnClickListener {
                findNavController().navigate(R.id.action_analytics_to_goal_weight_update)
            }

            btnUpdateWeight.setOnClickListener {
                findNavController().navigate(R.id.action_analytics_to_weight_update)
            }

            btnBmiInfo.setOnClickListener {
                openBmiInfoUrl()
            }
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.apply {
                tvGoalWeight.text = getString(R.string.goal_weight, "${userData.targetWeight} kg")
                tvCurrentWeight.text =
                    getString(R.string.current_weight_param, "${userData.weight?.toInt()} kg")

                // Update BMI related views
                val bmi = viewModel.calculateBMI()
                tvBmiValue.text = String.format("%.1f", bmi)
                updateBmiCategory(bmi)
                updateBmiIndicator(bmi)
            }
        }
    }

    private fun updateBmiCategory(bmi: Double) {
        binding.tvBmiCategory.apply {
            val (category, color) = when {
                bmi < 18.5 -> getString(R.string.underweight) to R.color.blue
                bmi < 25 -> getString(R.string.healthy) to R.color.green
                bmi < 30 -> getString(R.string.overweight) to R.color.orange
                else -> getString(R.string.obese) to R.color.red
            }
            text = category
            setTextColor(requireContext().getColor(color))
        }
    }

    private fun updateBmiIndicator(bmi: Double) {
        // Clamp BMI between 18.5 and 33 for the indicator position
        val clampedBmi = bmi.coerceIn(18.5, 33.0)

        // Calculate the position ratio (0 to 1)
        val ratio = (clampedBmi - 18.5) / (33.0 - 18.5)

        // Get the width of the progress bar
        binding.bmiProgressBackground.post {
            val progressWidth = binding.bmiProgressBackground.width
            val indicatorWidth = binding.bmiIndicator.width

            // Calculate the offset, accounting for the indicator width
            val offset = (progressWidth - indicatorWidth) * ratio

            // Update the indicator position
            binding.bmiIndicator.translationX = offset.toFloat()
        }
    }

    private fun openBmiInfoUrl() {
        val url = "https://www.who.int/news-room/fact-sheets/detail/obesity-and-overweight"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}