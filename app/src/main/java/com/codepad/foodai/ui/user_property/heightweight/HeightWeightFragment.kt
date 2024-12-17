package com.codepad.foodai.ui.user_property.heightweight

import android.view.View
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHeightWeightBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HeightWeightFragment : BaseFragment<FragmentHeightWeightBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()
    private val args: HeightWeightFragmentArgs by navArgs()
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId() = R.layout.fragment_height_weight

    override fun onReadyView() {
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupPickers()
        setupToggle()

        sharedViewModel.height.observe(viewLifecycleOwner) { height ->
            updateHeightPicker(height)
        }

        sharedViewModel.weight.observe(viewLifecycleOwner) { weight ->
            updateWeightPicker(weight)
        }

        sharedViewModel.measurementUnit.observe(viewLifecycleOwner) {
            updatePickers()
        }

        if (!arguments?.getString("type").isNullOrEmpty()) {
            setPreDefinedValues()
        }
    }

    private fun setPreDefinedValues() {
        val isMetric = UserSession.user?.isMetric ?: true
        binding.nextButton.visibility = View.VISIBLE
        binding.toggleMeasurementUnit.isChecked = isMetric
        binding.clTopHeader.visibility = View.VISIBLE
        binding.tvTitle.visibility = View.GONE
        binding.tvSubtitle.visibility = View.GONE
        when (args.type) {
            "weight" -> {
                if (isMetric) {
                    UserSession.user?.weight?.let {
                        binding.weightPicker.value = it.toInt()
                    }
                } else {
                    UserSession.user?.weight?.let {
                        binding.weightPicker.value = (it * 2.205).toInt()
                    }
                }
            }

            "height" -> {
                if (isMetric) {
                    UserSession.user?.height?.let {
                        binding.heightPicker2.value = it.toInt()
                    }
                } else {
                    UserSession.user?.height?.let {
                        val feet = (it / 30.48).toInt()
                        val inches = ((it % 30.48) / 2.54).toInt()
                        binding.heightPicker1.value = feet
                        binding.heightPicker2.value = inches
                    }
                }
            }
        }

        binding.nextButton.setOnClickListener {
            sharedViewModel.updateHeightWeight()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.isHeightWeightSet.value == true) {
            binding.toggleMeasurementUnit.isChecked =
                sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC
            when (sharedViewModel.measurementUnit.value) {
                MeasurementUnit.METRIC -> {
                    val height = sharedViewModel.height.value ?: 160
                    val weight = sharedViewModel.weight.value ?: 60
                    binding.heightPicker2.value = height
                    binding.weightPicker.value = weight
                }

                MeasurementUnit.IMPERIAL -> {
                    val heightFT = sharedViewModel.heightFT.value ?: 5
                    val heightIN = sharedViewModel.heightIN.value ?: 3
                    val weightLB = sharedViewModel.weightLB.value ?: 132
                    binding.heightPicker1.value = heightFT
                    binding.heightPicker2.value = heightIN
                    binding.weightPicker.value = weightLB
                }

                null -> {}
            }
        }
    }

    private fun setupPickers() {
        if (sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC) {
            binding.heightPicker2.minValue = 60
            binding.heightPicker2.maxValue = 243
            binding.heightPicker2.value = sharedViewModel.height.value ?: 160
            binding.heightPicker2.setFormatter { value -> "$value cm" }
            binding.heightPicker2.setOnValueChangedListener { _, _, newVal ->
                sharedViewModel.setHeight(newVal)
            }

            binding.weightPicker.minValue = 20
            binding.weightPicker.maxValue = 360
            binding.weightPicker.value = sharedViewModel.weight.value ?: 60
            binding.weightPicker.setFormatter { value -> "$value kg" }
            binding.weightPicker.setOnValueChangedListener { _, _, newVal ->
                sharedViewModel.setWeight(newVal)
            }

            binding.heightPicker1.visibility = NumberPicker.GONE
        } else {
            binding.heightPicker1.visibility = NumberPicker.VISIBLE
            binding.heightPicker1.minValue = 1
            binding.heightPicker1.maxValue = 8
            binding.heightPicker1.value = sharedViewModel.heightFT.value ?: 5
            binding.heightPicker1.setFormatter { value -> "$value ft" }
            binding.heightPicker1.setOnValueChangedListener { _, _, newVal ->
                sharedViewModel.setHeightFT(newVal)
            }

            binding.heightPicker2.minValue = 0
            binding.heightPicker2.maxValue = 11
            binding.heightPicker2.value = sharedViewModel.heightIN.value ?: 3
            binding.heightPicker2.setFormatter { value -> "$value in" }
            binding.heightPicker2.setOnValueChangedListener { _, _, newVal ->
                sharedViewModel.setHeightIN(newVal)
            }

            binding.weightPicker.minValue = 50
            binding.weightPicker.maxValue = 700
            binding.weightPicker.value = sharedViewModel.weightLB.value ?: 132
            binding.weightPicker.setFormatter { value -> "$value lb" }
            binding.weightPicker.setOnValueChangedListener { _, _, newVal ->
                sharedViewModel.setWeightLB(newVal)
            }
        }
    }

    private fun setupToggle() {
        binding.toggleMeasurementUnit.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setMeasurementUnit(if (isChecked) MeasurementUnit.METRIC else MeasurementUnit.IMPERIAL)

            val currentHeight =
                if (sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC) {
                    sharedViewModel.height.value ?: 160
                } else {
                    val feet = sharedViewModel.heightFT.value ?: 5
                    val inches = sharedViewModel.heightIN.value ?: 3
                    (feet * 30.48 + inches * 2.54).toInt()
                }

            val currentWeight =
                if (sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC) {
                    (sharedViewModel.weight.value ?: 60).toFloat()
                } else {
                    ((sharedViewModel.weightLB.value ?: 132) / 2.205).toFloat()
                }

            if (isChecked) {
                sharedViewModel.setHeight(currentHeight)
                sharedViewModel.setWeight(currentWeight.toInt())
                binding.txtMetric.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
                binding.txtImperial.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.gray
                    )
                )
            } else {
                val feet = (currentHeight / 30.48).toInt()
                val inches = ((currentHeight % 30.48) / 2.54).toInt()
                sharedViewModel.setHeightFT(feet)
                sharedViewModel.setHeightIN(inches)
                sharedViewModel.setWeightLB((currentWeight * 2.205).toInt())
                binding.txtImperial.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
                binding.txtMetric.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.gray
                    )
                )
            }

            setupPickers()
        }
    }

    private fun updatePickers() {
        if (sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC) {
            binding.heightPicker1.visibility = NumberPicker.GONE
            binding.heightPicker2.minValue = 60
            binding.heightPicker2.maxValue = 243
            binding.heightPicker2.value = sharedViewModel.height.value ?: 160

            binding.weightPicker.minValue = 20
            binding.weightPicker.maxValue = 360
            binding.weightPicker.value = sharedViewModel.weight.value ?: 60
        } else {
            binding.heightPicker1.visibility = NumberPicker.VISIBLE
            binding.heightPicker1.minValue = 1
            binding.heightPicker1.maxValue = 8
            binding.heightPicker1.value = sharedViewModel.heightFT.value ?: 5

            binding.heightPicker2.minValue = 0
            binding.heightPicker2.maxValue = 11
            binding.heightPicker2.value = sharedViewModel.heightIN.value ?: 3

            binding.weightPicker.minValue = 50
            binding.weightPicker.maxValue = 700
            binding.weightPicker.value = sharedViewModel.weightLB.value ?: 132
        }
    }

    private fun updateHeightPicker(height: Int) {
        if (sharedViewModel.measurementUnit.value == MeasurementUnit.METRIC) {
            binding.heightPicker2.value = height
        } else {
            // Convert height to feet and inches
            val feet = height / 30.48
            val inches = (height % 30.48) / 2.54
            binding.heightPicker1.value = feet.toInt()
            binding.heightPicker2.value = inches.toInt()
        }
    }

    private fun updateWeightPicker(weight: Int) {
        binding.weightPicker.value = weight
    }
}