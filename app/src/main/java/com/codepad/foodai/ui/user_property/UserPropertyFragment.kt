package com.codepad.foodai.ui.user_property

import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentUserPropertyBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.accomplish.AccomplishFragment
import com.codepad.foodai.ui.user_property.birth.BirthFragment
import com.codepad.foodai.ui.user_property.desiredweight.DesiredWeightFragment
import com.codepad.foodai.ui.user_property.diet.DietFragment
import com.codepad.foodai.ui.user_property.gender.GenderFragment
import com.codepad.foodai.ui.user_property.goal.GoalFragment
import com.codepad.foodai.ui.user_property.heightweight.HeightWeightFragment
import com.codepad.foodai.ui.user_property.reachinggoals.ReachingGoalsFragment
import com.codepad.foodai.ui.user_property.weightspeed.WeightSpeedSelectionFragment
import com.codepad.foodai.ui.user_property.workout.WorkoutFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserPropertyFragment : BaseFragment<FragmentUserPropertyBinding>() {
    private var currentStep = 1
    private val totalSteps = 10
    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_user_property

    override fun onReadyView() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnNext.setOnClickListener { onNextPressed() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        sharedViewModel.isNextEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateNextButtonState()
        }

        loadFragment(GenderFragment(), true)
    }

    private fun onBackPressed() {
        if (currentStep == 1) {
            currentStep = 0
            sharedViewModel.invalidateAll()
            findNavController().popBackStack()
        } else {
            currentStep = currentStep.dec()
            updateProgress()
            childFragmentManager.popBackStack()
        }
    }

    private fun onNextPressed() {
        val requireDesiredWeight = sharedViewModel.goalNavigationParams.value?.first == true
        val isGain = sharedViewModel.goalNavigationParams.value?.second == true

        sharedViewModel.onNextClicked(currentStep)
        if (sharedViewModel.showWarning.value == true) {
            displaySelectionWarning()
            sharedViewModel.invalidateShowWarning()
        } else {
            if (currentStep < totalSteps) {
                when (currentStep) {
                    1 -> loadFragment(WorkoutFragment())
                    2 -> loadFragment(HeightWeightFragment())
                    3 -> loadFragment(BirthFragment())
                    4 -> loadFragment(GoalFragment())
                    5 -> {
                        if (requireDesiredWeight) {
                            loadFragment(DesiredWeightFragment(isGain))
                        } else {
                            loadFragment(ReachingGoalsFragment())
                        }
                    }

                    6 -> {
                        if (requireDesiredWeight) {
                            loadFragment(WeightSpeedSelectionFragment(isGain))
                        } else {
                            loadFragment(DietFragment())
                        }
                    }

                    7 -> {
                        if (requireDesiredWeight) {
                            loadFragment(ReachingGoalsFragment())
                        } else {
                            loadFragment(AccomplishFragment())
                        }
                    }

                    8 -> {
                        if (requireDesiredWeight) {
                            loadFragment(DietFragment())
                        } else {
                            // Complete flow
                        }
                    }

                    9 -> {
                        if (requireDesiredWeight) {
                            loadFragment(AccomplishFragment())
                        }
                    }
                    else -> {
                        // Complete flow
                    }
                }
            }
        }
    }

    private fun displaySelectionWarning() {
        if (currentStep == 1) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_gender,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 2) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_workout,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 3) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_weight_and_height,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 4) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_birth_date,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 5) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_goal,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 6) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_desired_weight,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 7) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_reaching_goal,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 8) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_a_diet,
                Toast.LENGTH_SHORT
            ).show()
        } else if (currentStep == 9) {
            Toast.makeText(
                requireContext(),
                R.string.please_select_an_accomplishment,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateProgress() {
        binding.progressBar.progress = currentStep
    }

    private fun loadFragment(fragment: Fragment, initialLoad: Boolean = false) {
        if (!initialLoad) {
            currentStep = currentStep.inc()
            updateProgress()
        }
        childFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
        updateNextButtonState(false)
    }

    private fun updateNextButtonState(enable: Boolean = true) {
        val isEnabled = when (currentStep) {
            1 -> sharedViewModel.selectedGender.value != null
            2 -> sharedViewModel.selectedWorkout.value != null
            3 -> sharedViewModel.isHeightWeightSet.value == true
            4 -> sharedViewModel.dateOfBirth.value != null
            5 -> sharedViewModel.selectedGoal.value != null
            6 -> sharedViewModel.desiredWeight.value != null
            7 -> sharedViewModel.selectedReachingGoal.value != null // TODO order will change
            8 -> sharedViewModel.selectedDiet.value != null // TODO order will change
            9 -> sharedViewModel.selectedAccomplishment.value != null // TODO order will change
            else -> true
        } && enable
        binding.btnNext.setBackgroundColor(
            if (isEnabled) ContextCompat.getColor(requireContext(), R.color.blue_button)
            else ContextCompat.getColor(requireContext(), R.color.white)
        )
        binding.btnNext.setTextColor(
            if (isEnabled) ContextCompat.getColor(requireContext(), R.color.white)
            else ContextCompat.getColor(requireContext(), R.color.black)
        )
    }
}