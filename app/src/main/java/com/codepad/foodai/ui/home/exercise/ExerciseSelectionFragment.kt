package com.codepad.foodai.ui.home.exercise

import android.view.View
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentExerciseSelectionBinding
import com.codepad.foodai.databinding.LayoutExerciseOptionBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseSelectionFragment : BaseFragment<FragmentExerciseSelectionBinding>() {

    override fun getLayoutId(): Int = R.layout.fragment_exercise_selection
    override fun onReadyView() {
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        setupExerciseOption(
            binding.btnRunning,
            R.drawable.ic_run,
            getString(R.string.run),
            getString(R.string.run_description)
        )
        setupExerciseOption(
            binding.btnWeightLifting,
            R.drawable.ic_weight,
            getString(R.string.weight_lifting),
            getString(R.string.weight_lifting_description)
        )
        setupExerciseOption(
            binding.btnDescribe,
            R.drawable.ic_pencil,
            getString(R.string.describe),
            getString(R.string.describe_description)
        )
    }

    private fun setupExerciseOption(
        binding: LayoutExerciseOptionBinding,
        iconResId: Int,
        title: String,
        subtitle: String,
    ) {
        binding.imgIcon.setImageResource(iconResId)
        binding.txtTitle.text = title
        binding.txtSubtitle.text = subtitle
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRunning.root.setOnClickListener {
            findNavController().navigate(R.id.action_exerciseSelectionFragment_to_runningFragment)
        }

        binding.btnWeightLifting.root.setOnClickListener {
            findNavController().navigate(R.id.action_exerciseSelectionFragment_to_weightLiftingFragment)
        }

        binding.btnDescribe.root.setOnClickListener {
            //findNavController().navigate(R.id.action_exerciseSelectionFragment_to_describeExerciseFragment)
        }
    }
} 