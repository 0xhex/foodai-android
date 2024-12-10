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
import com.codepad.foodai.ui.user_property.gender.GenderFragment
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
        sharedViewModel.onNextClicked(currentStep)
        if (sharedViewModel.showWarning.value == true) {
            displaySelectionWarning()
            sharedViewModel.invalidateShowWarning()
        } else {
            if (currentStep < totalSteps) {
                // Load the next fragment
                when (currentStep) {
                    1 -> loadFragment(WorkoutFragment())
                    2 -> {
                        //loadFragment(WorkoutFragment())
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
    }

    private fun updateNextButtonState() {
        val isEnabled = when (currentStep) {
            1 -> sharedViewModel.selectedGender.value != null
            2 -> sharedViewModel.selectedWorkout.value != null
            else -> true
        }
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