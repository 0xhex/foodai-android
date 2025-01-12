package com.codepad.foodai.ui.home.exercise.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentDescribeExerciseDetailBinding
import com.codepad.foodai.domain.use_cases.user.ExerciseData
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.exercise.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DescribeExerciseDetailFragment : BaseFragment<FragmentDescribeExerciseDetailBinding>() {

    private val viewModel: ExerciseViewModel by viewModels()
    private var exerciseData: ExerciseData? = null
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_describe_exercise_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exerciseData = arguments?.getParcelable("exerciseData")
        onReadyView()
    }

    override fun onReadyView() {
        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        exerciseData?.let { exercise ->
            binding.edtDescription.setText("${exercise.exerciseType} ${exercise.duration} min")
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnUpdateExercise.setOnClickListener {
            updateExercise()
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
                android.util.Log.d("DescribeExerciseDetailFragment", "Error: $errorMessage")
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.exerciseLogged.observe(viewLifecycleOwner) { event ->
            android.util.Log.d("DescribeExerciseDetailFragment", "Received exerciseLogged event")
            event.getContentIfNotHandled()?.let { success ->
                android.util.Log.d("DescribeExerciseDetailFragment", "Success: $success")
                if (success) {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }
    }

    private fun updateExercise() {
        exerciseData?.let { exercise ->
            viewModel.updateExerciseDescription(
                exerciseID = exercise.id.orEmpty(),
                description = binding.edtDescription.text.toString()
            )
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Exercise")
            .setPositiveButton("Ok") { _, _ ->
                deleteExercise()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteExercise() {
        exerciseData?.let { exercise ->
            viewModel.deleteExercise(exercise.id.orEmpty())
        }
    }
} 