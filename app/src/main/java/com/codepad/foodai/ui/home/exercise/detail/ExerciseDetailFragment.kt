package com.codepad.foodai.ui.home.exercise.detail

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentExerciseDetailBinding
import com.codepad.foodai.domain.use_cases.user.ExerciseData
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.exercise.ExerciseViewModel
import com.codepad.foodai.ui.user_property.loading.LoadingType
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast

@AndroidEntryPoint
class ExerciseDetailFragment : BaseFragment<FragmentExerciseDetailBinding>() {

    private val viewModel: ExerciseViewModel by activityViewModels()
    private var exerciseData: ExerciseData? = null
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_exercise_detail

    override fun onReadyView() {
        exerciseData = arguments?.getParcelable("exerciseData")
        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Set header
            exerciseData?.let { exercise ->
                txtTitle.text = exercise.exerciseType?.replaceFirstChar { it.uppercase() }

                // Set animations
                headerIcon.apply {
                    val animationRes = when (exercise.exerciseType) {
                        "run" -> R.raw.running
                        "weightlifting" -> R.raw.dumbel
                        else -> R.raw.exercise
                    }
                    setAnimation(animationRes)
                    playAnimation()
                }

                speedMeter.apply {
                    setAnimation(R.raw.speed_meter)
                    playAnimation()
                }

                // Set intensity texts based on type
                val (highText, mediumText, lowText) = when (exercise.exerciseType) {
                    "run" -> Triple(
                        getString(R.string.running_high_intensity),
                        getString(R.string.running_medium_intensity),
                        getString(R.string.running_low_intensity)
                    )

                    "weightlifting" -> Triple(
                        getString(R.string.weight_lifting_high_intensity),
                        getString(R.string.weight_lifting_medium_intensity),
                        getString(R.string.weight_lifting_low_intensity)
                    )

                    else -> Triple("High", "Medium", "Low")
                }

                txtIntensityHigh.text = highText
                txtIntensityMedium.text = mediumText
                txtIntensityLow.text = lowText

                // Set initial intensity
                sliderIntensity.progress = when (exercise.intensity) {
                    "high" -> 2
                    "moderate" -> 1
                    "low" -> 0
                    else -> 1
                }

                // Set initial duration
                val duration = exercise.duration ?: 15
                edtCustomDuration.setText(duration.toString())

                // Select the appropriate chip if duration matches
                val chip = when (duration) {
                    15 -> btn15
                    30 -> btn30
                    60 -> btn60
                    90 -> btn90
                    else -> null
                }
                chip?.isChecked = true
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            // Duration chip listeners
            btn15.setOnClickListener { updateDuration(15) }
            btn30.setOnClickListener { updateDuration(30) }
            btn60.setOnClickListener { updateDuration(60) }
            btn90.setOnClickListener { updateDuration(90) }

            edtCustomDuration.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.durationChips.clearCheck()
                }
            }

            btnUpdateExercise.setOnClickListener {
                exerciseData?.let { exercise ->
                    val duration = edtCustomDuration.text.toString().toIntOrNull() ?: 15
                    val intensity = getIntensityString(sliderIntensity.progress)
                    viewModel.updateCustomExercise(
                        exercise.id.orEmpty(),
                        exercise.exerciseType.orEmpty(),
                        intensity,
                        duration
                    )
                }
            }
        }
    }

    private fun updateDuration(duration: Int) {
        binding.edtCustomDuration.setText(duration.toString())
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
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                android.util.Log.d("ExerciseDetailFragment", "Error: $errorMessage")
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.exerciseLogged.observe(viewLifecycleOwner) { event ->
            android.util.Log.d("ExerciseDetailFragment", "Received exerciseLogged event")
            event.getContentIfNotHandled()?.let { success ->
                android.util.Log.d("ExerciseDetailFragment", "Success: $success")
                if (success) {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }
    }

    fun setExerciseData(exercise: ExerciseData) {
        exerciseData = exercise
    }
} 