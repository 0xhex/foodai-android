package com.codepad.foodai.ui.home.exercise.describe

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentDescribeExerciseBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.exercise.ExerciseViewModel
import com.codepad.foodai.ui.user_property.loading.LoadingType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DescribeExerciseFragment : BaseFragment<FragmentDescribeExerciseBinding>() {

    private val viewModel: ExerciseViewModel by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fragment_describe_exercise
    override fun onReadyView() {
        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Hide keyboard when clicking outside EditText
            root.setOnClickListener {
                hideKeyboard()
            }

            // Set loading overlay background with opacity
            loadingOverlay.setBackgroundResource(R.color.black_60_opacity)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnAddExercise.setOnClickListener {
                val description = edtDescription.text.toString()
                if (description.isNotBlank()) {
                    val userID = UserSession.user?.id.orEmpty()
                    viewModel.submitExerciseDescription(userID, description)
                }
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
                    // Navigate back to home fragment
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { errorMessage ->
                // Just log the error and navigate back, matching iOS behavior
                Log.e("DescribeExercise", "Error adding exercise: $errorMessage")
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
} 