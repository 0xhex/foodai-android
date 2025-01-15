package com.codepad.foodai.ui.home.analytics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentWeightUpdateBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.utils.observeEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalWeightUpdateFragment : BaseFragment<FragmentWeightUpdateBinding>() {

    private val viewModel: AnalyticsViewModel by viewModels()
    private var isMetric = true
    override fun getLayoutId() = R.layout.fragment_weight_update

    override fun onReadyView() {
        setupViews()
        setupObservers()
        viewModel.fetchUserData()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            txtTitle.text = getString(R.string.edit_weight_goal)
            
            // Hide the unit toggle since we use user's preference
            unitToggleContainer.visibility = View.GONE

            setupNumberPicker()

            btnSave.setOnClickListener {
                val weight = numberPicker.value
                viewModel.updateTargetWeight(weight)
            }
        }
    }

    private fun setupNumberPicker() {
        binding.numberPicker.apply {
            minValue = if (isMetric) 20 else 50
            maxValue = if (isMetric) 360 else 700
            wrapSelectorWheel = false
            setFormatter { value -> "$value ${if (isMetric) "kg" else "lb"}" }
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            isMetric = userData.isMetric ?: true
            binding.apply {
                numberPicker.value = if (isMetric) {
                    userData.targetWeight?.toInt() ?: 60
                } else {
                    (userData.targetWeight?.times(2.20462))?.toInt() ?: 132
                }
                setupNumberPicker() // Update picker range and formatter based on metric preference
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.weightUpdateSuccess.observeEvent(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigateUp()
            }
        }
    }
} 