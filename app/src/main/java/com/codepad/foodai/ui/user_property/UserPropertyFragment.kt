package com.codepad.foodai.ui.user_property

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
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

        // Set initial state of btnNext
        binding.btnNext.isEnabled = true
        binding.btnNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.btnNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        sharedViewModel.isNextEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnNext.isEnabled = isEnabled
            binding.btnNext.setBackgroundColor(
                if (isEnabled) ContextCompat.getColor(requireContext(), R.color.blue_button)
                else ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.btnNext.setTextColor(
                if (isEnabled) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.black)
            )
        }

        loadFragment(GenderFragment())
    }

    private fun onBackPressed() {
        if (currentStep > 1) {
            currentStep--
            updateProgress()
            childFragmentManager.popBackStack()
        } else {
            // Navigate back to OnboardingFragment
            parentFragmentManager.popBackStack()
        }
    }

    private fun onNextPressed() {
        sharedViewModel.onNextClicked(currentStep)
        if (sharedViewModel.showWarning.value == true) {
            Toast.makeText(requireContext(), R.string.please_select_a_gender, Toast.LENGTH_SHORT).show()
        } else {
            if (currentStep < totalSteps) {
                currentStep++
                updateProgress()
                // Load the next fragment
                loadFragment(WorkoutFragment())
            }
        }
    }

    private fun updateProgress() {
        binding.progressBar.progress = currentStep
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
    }
}