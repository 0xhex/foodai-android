package com.codepad.foodai.ui.home.exercise.base

import android.os.Bundle
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Set header
            txtTitle.text = getHeaderTitle()
            // Set animations
            headerIcon.setAnimation(getHeaderIcon())
            speedMeter.setAnimation(R.raw.speed_meter)
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
                val intensity = when (sliderIntensity.progress) {
                    0 -> "low"
                    1 -> "moderate"
                    else -> "high"
                }
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
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { errorMessage ->
                // Show error message to user (you can use a Snackbar or Toast)
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