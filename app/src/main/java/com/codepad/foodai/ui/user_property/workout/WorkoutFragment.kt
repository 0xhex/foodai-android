package com.codepad.foodai.ui.user_property.workout

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentWorkoutBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutFragment : BaseFragment<FragmentWorkoutBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_workout

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.showWarning.observe(viewLifecycleOwner) { showWarning ->
            if (showWarning) {
                Toast.makeText(requireContext(), R.string.please_select_a_workout, Toast.LENGTH_SHORT).show()
            }
        }

        sharedViewModel.selectedWorkout.observe(viewLifecycleOwner) { workout ->
            updateButtonStyles(workout)
        }
    }

    private fun updateButtonStyles(selectedWorkout: String?) {
        val lowButton = binding.btnLow
        val mediumButton = binding.btnMedium
        val highButton = binding.btnHigh

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        when (selectedWorkout) {
            "0-2" -> {
                lowButton.setBackgroundColor(selectedColor)
                lowButton.setTextColor(selectedTextColor)
                mediumButton.setBackgroundColor(unselectedColor)
                mediumButton.setTextColor(unselectedTextColor)
                highButton.setBackgroundColor(unselectedColor)
                highButton.setTextColor(unselectedTextColor)
            }
            "3-5" -> {
                mediumButton.setBackgroundColor(selectedColor)
                mediumButton.setTextColor(selectedTextColor)
                lowButton.setBackgroundColor(unselectedColor)
                lowButton.setTextColor(unselectedTextColor)
                highButton.setBackgroundColor(unselectedColor)
                highButton.setTextColor(unselectedTextColor)
            }
            "6+" -> {
                highButton.setBackgroundColor(selectedColor)
                highButton.setTextColor(selectedTextColor)
                lowButton.setBackgroundColor(unselectedColor)
                lowButton.setTextColor(unselectedTextColor)
                mediumButton.setBackgroundColor(unselectedColor)
                mediumButton.setTextColor(unselectedTextColor)
            }
            else -> {
                lowButton.setBackgroundColor(unselectedColor)
                lowButton.setTextColor(unselectedTextColor)
                mediumButton.setBackgroundColor(unselectedColor)
                mediumButton.setTextColor(unselectedTextColor)
                highButton.setBackgroundColor(unselectedColor)
                highButton.setTextColor(unselectedTextColor)
            }
        }
    }
}