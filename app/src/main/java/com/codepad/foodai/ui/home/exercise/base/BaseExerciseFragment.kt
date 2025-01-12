package com.codepad.foodai.ui.home.exercise.base

import android.view.View
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentBaseExerciseBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.exercise.ExerciseViewModel
import com.codepad.foodai.ui.user_property.loading.LoadingType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseExerciseFragment : BaseFragment<FragmentBaseExerciseBinding>() {

    protected val viewModel: ExerciseViewModel by activityViewModels()

    abstract fun getExerciseType(): String
    abstract fun getHeaderIcon(): Int
    abstract fun getHeaderTitle(): String
    abstract fun getIntensityHighText(): String
    abstract fun getIntensityMediumText(): String
    abstract fun getIntensityLowText(): String


    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onReadyView()
    }

    override fun onReadyView() {
        setupViews()
        setupClickListeners()
        setupIntensitySeekBar()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Set header
            txtTitle.text = getHeaderTitle()
            
            // Set animations
            headerIcon.apply {
                setAnimation(getHeaderIcon())
                playAnimation()
            }
            
            speedMeter.apply {
                setAnimation(R.raw.speed_meter)
                playAnimation()
            }

            // Set intensity texts
            txtIntensityHigh.text = getIntensityHighText()
            txtIntensityMedium.text = getIntensityMediumText()
            txtIntensityLow.text = getIntensityLowText()
            
            // Set initial duration
            edtCustomDuration.setText("15")
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            // Duration buttons
            listOf(btn15, btn30, btn60, btn90).forEachIndexed { index, button ->
                val duration = when (index) {
                    0 -> "15"
                    1 -> "30"
                    2 -> "60"
                    else -> "90"
                }
                button.setOnClickListener {
                    edtCustomDuration.setText(duration)
                    updateDurationButtonStates(duration)
                }
            }

            edtCustomDuration.doAfterTextChanged { text ->
                updateDurationButtonStates(text.toString())
            }

            btnAddExercise.setOnClickListener {
                val duration = edtCustomDuration.text.toString().toIntOrNull() ?: 15
                val userID = UserSession.user?.id.orEmpty()
                val intensity = getIntensityString(sliderIntensity.progress)
                viewModel.logExercise(userID, getExerciseType(), intensity, duration)
            }
        }
    }

    private fun updateDurationButtonStates(selectedDuration: String) {
        binding.apply {
            listOf(btn15, btn30, btn60, btn90).forEachIndexed { index, button ->
                val duration = when (index) {
                    0 -> "15"
                    1 -> "30"
                    2 -> "60"
                    else -> "90"
                }
                button.isSelected = duration == selectedDuration
            }
        }
    }

    private fun setupIntensitySeekBar() {
        binding.apply {
            sliderIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // Convert progress to intensity (reversed because of vertical orientation)
                    val intensity = 2 - progress // 2->0 (High), 1->1 (Medium), 0->2 (Low)
                    updateIntensityUI(intensity)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            sliderIntensity.max = 2
            sliderIntensity.progress = 1 // Start with medium intensity
            // Set initial state
            updateIntensityUI(1) // Start with medium intensity
        }
    }

    private fun updateIntensityUI(intensity: Int) {
        binding.apply {
            // Reset all styles
            listOf(txtIntensityLow, txtIntensityMedium, txtIntensityHigh).forEach { textView ->
                textView.apply {
                    setTextColor(resources.getColor(R.color.gray, null))
                    typeface = ResourcesCompat.getFont(requireContext(), R.font.euro_stile_regular)
                }
            }

            // Update selected style based on intensity
            val selectedTextView = when (intensity) {
                0 -> txtIntensityHigh  // High intensity
                1 -> txtIntensityMedium // Medium intensity
                2 -> txtIntensityLow   // Low intensity
                else -> txtIntensityMedium
            }

            selectedTextView.apply {
                setTextColor(resources.getColor(R.color.white, null))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.euro_stile_bold)
            }
        }
    }

    private fun getIntensityString(progress: Int): String {
        return when (2 - progress) { // Convert progress to intensity
            0 -> "high"
            1 -> "moderate"
            2 -> "low"
            else -> "moderate"
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if (isLoading) {
                binding.loadingView.setLoadingType(LoadingType.LOADING_DEFAULT)
            }
        }

        viewModel.exerciseLogged.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { errorMessage ->
                showError(errorMessage)
            }
        }
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

} 