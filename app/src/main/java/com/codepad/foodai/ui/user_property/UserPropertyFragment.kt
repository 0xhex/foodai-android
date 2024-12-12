package com.codepad.foodai.ui.user_property

import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentUserPropertyBinding
import com.codepad.foodai.helpers.FirebaseRemoteConfigManager
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.accomplish.AccomplishFragment
import com.codepad.foodai.ui.user_property.birth.BirthFragment
import com.codepad.foodai.ui.user_property.desiredweight.DesiredWeightFragment
import com.codepad.foodai.ui.user_property.diet.DietFragment
import com.codepad.foodai.ui.user_property.gender.GenderFragment
import com.codepad.foodai.ui.user_property.goal.GoalFragment
import com.codepad.foodai.ui.user_property.heightweight.HeightWeightFragment
import com.codepad.foodai.ui.user_property.loading.LoadingFragment
import com.codepad.foodai.ui.user_property.rating.RatingFragment
import com.codepad.foodai.ui.user_property.reachinggoals.ReachingGoalsFragment
import com.codepad.foodai.ui.user_property.weightspeed.WeightSpeedSelectionFragment
import com.codepad.foodai.ui.user_property.workout.WorkoutFragment
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserPropertyFragment : BaseFragment<FragmentUserPropertyBinding>() {
    private var currentStep = 1
    private val totalSteps = 10
    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_user_property
    private val playReviewManager by lazy {
        ReviewManagerFactory.create(requireContext())
    }

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
            if (currentStep <= totalSteps) {
                val nextFragment = when (currentStep) {
                    1 -> WorkoutFragment()
                    2 -> HeightWeightFragment()
                    3 -> BirthFragment()
                    4 -> GoalFragment()
                    5 -> if (requireDesiredWeight) DesiredWeightFragment(isGain) else ReachingGoalsFragment()
                    6 -> if (requireDesiredWeight) WeightSpeedSelectionFragment(isGain) else DietFragment()
                    7 -> if (requireDesiredWeight) ReachingGoalsFragment() else AccomplishFragment()
                    8 -> if (requireDesiredWeight) DietFragment() else if (isPassedStore()) RatingFragment() else LoadingFragment()
                    9 -> if (requireDesiredWeight) AccomplishFragment() else if (isPassedStore()) LoadingFragment() else null
                    10 -> if (isPassedStore()) RatingFragment() else LoadingFragment()
                    else -> null
                }
                nextFragment?.let { loadFragment(it) }
            } else if (currentStep > totalSteps && isPassedStore()) {
                launchPlayReviewPopup()
            }
        }
    }

    private fun isPassedStore() = FirebaseRemoteConfigManager.getBoolean("isPassedStore")

    private fun displaySelectionWarning() {
        val requireDesiredWeight = sharedViewModel.goalNavigationParams.value?.first == true
        val messageResId = when (currentStep) {
            1 -> R.string.please_select_a_gender
            2 -> R.string.please_select_a_workout
            3 -> R.string.please_select_weight_and_height
            4 -> R.string.please_select_a_birth_date
            5 -> R.string.please_select_a_goal
            6 -> if (requireDesiredWeight) R.string.please_select_a_desired_weight else R.string.please_select_a_reaching_goal
            7 -> if (requireDesiredWeight) R.string.select_a_weight_speed else R.string.please_select_a_diet
            8 -> if (requireDesiredWeight) R.string.please_select_a_reaching_goal else R.string.please_select_an_accomplishment
            9 -> if (requireDesiredWeight) R.string.please_select_a_diet else null
            10 -> if (requireDesiredWeight) R.string.please_select_an_accomplishment else null
            else -> null
        }
        messageResId?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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

        updateNextButtonState(fragment is RatingFragment)
    }

    private fun updateNextButtonState(enable: Boolean = true) {
        val requireDesiredWeight = sharedViewModel.goalNavigationParams.value?.first == true
        val isEnabled = when (currentStep) {
            1 -> sharedViewModel.selectedGender.value != null
            2 -> sharedViewModel.selectedWorkout.value != null
            3 -> sharedViewModel.isHeightWeightSet.value == true
            4 -> sharedViewModel.dateOfBirth.value != null
            5 -> sharedViewModel.selectedGoal.value != null
            6 -> if (requireDesiredWeight) sharedViewModel.desiredWeight.value != null else sharedViewModel.selectedReachingGoal.value != null
            7 -> if (requireDesiredWeight) sharedViewModel.weightSpeed.value != null else sharedViewModel.selectedDiet.value != null
            8 -> if (requireDesiredWeight) sharedViewModel.selectedReachingGoal.value != null else sharedViewModel.selectedAccomplishment.value != null
            9 -> if (requireDesiredWeight) sharedViewModel.selectedDiet.value != null else isPassedStore()
            10 -> if (requireDesiredWeight) sharedViewModel.selectedAccomplishment.value != null else true
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

    private fun launchPlayReviewPopup() {
        val request = playReviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                playReviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                    .addOnCompleteListener { reviewTask ->
                        navigateToLoader()
                    }
            } else {
                navigateToLoader()
            }
        }
    }

    private fun navigateToLoader() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.userPropertyFragment, true)
            .build()
        findNavController().navigate(
            R.id.action_userPropertyFragment_to_loadingFragment,
            null,
            navOptions
        )
    }

}