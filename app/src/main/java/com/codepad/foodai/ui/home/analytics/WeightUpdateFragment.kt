package com.codepad.foodai.ui.home.analytics

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentWeightUpdateBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.utils.observeEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeightUpdateFragment : BaseFragment<FragmentWeightUpdateBinding>() {

    private val viewModel: AnalyticsViewModel by viewModels()
    private var isMetric = true
    override fun getLayoutId(): Int = R.layout.fragment_weight_update

    override fun onReadyView() {
        setupViews()
        setupObservers()
        setupToggle()
        viewModel.fetchUserData()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            txtTitle.text = getString(R.string.update_weight)

            setupNumberPicker()

            btnSave.setOnClickListener {
                val weight = numberPicker.value.toDouble()
                val weightToSave = if (isMetric) weight else weight * 0.45359237
                viewModel.updateWeight(weightToSave)
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

    private fun setupToggle() {
        binding.toggleMeasurementUnit.setOnCheckedChangeListener { _, isChecked ->
            isMetric = isChecked
            val currentValue = binding.numberPicker.value

            // Convert the current value when switching units
            val newValue = if (isChecked) {
                // Converting from lb to kg
                (currentValue * 0.45359237).toInt()
            } else {
                // Converting from kg to lb
                (currentValue * 2.20462).toInt()
            }

            setupNumberPicker()
            binding.numberPicker.value = newValue

            // Update text colors
            binding.apply {
                txtMetric.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isChecked) R.color.white else R.color.gray
                    )
                )
                txtImperial.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isChecked) R.color.gray else R.color.white
                    )
                )
            }
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            isMetric = userData.isMetric ?: true
            binding.apply {
                toggleMeasurementUnit.isChecked = isMetric
                numberPicker.value = if (isMetric) {
                    userData.weight?.toInt() ?: 60
                } else {
                    (userData.weight?.times(2.20462))?.toInt() ?: 132
                }
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