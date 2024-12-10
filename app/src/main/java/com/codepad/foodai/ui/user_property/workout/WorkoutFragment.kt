package com.codepad.foodai.ui.user_property.workout

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
                Toast.makeText(
                    requireContext(),
                    R.string.please_select_a_workout,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sharedViewModel.selectedWorkout.observe(viewLifecycleOwner) { workout ->
            updateButtonStyles(workout)
        }
    }


    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedWorkout.value != null) {
            sharedViewModel.selectWorkout(sharedViewModel.selectedWorkout.value!!)
        }
    }

    private fun updateButtonStyles(selectedWorkout: String?) {
        val lowCard = binding.btnLow
        val mediumCard = binding.btnMedium
        val highCard = binding.btnHigh

        val lowText = binding.textLow
        val mediumText = binding.textMedium
        val highText = binding.textHigh

        val subLowText = binding.subtextLow
        val subMediumText = binding.subtextMedium
        val subHighText = binding.subtextHigh

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        val unselectedSubTextColor = ContextCompat.getColor(requireContext(), R.color.gray)

        when (selectedWorkout) {
            "0-2" -> {
                lowCard.setCardBackgroundColor(selectedColor)
                lowText.setTextColor(selectedTextColor)
                subLowText.setTextColor(selectedTextColor)
                mediumCard.setCardBackgroundColor(unselectedColor)
                mediumText.setTextColor(unselectedTextColor)
                subMediumText.setTextColor(unselectedSubTextColor)
                highCard.setCardBackgroundColor(unselectedColor)
                highText.setTextColor(unselectedTextColor)
                subHighText.setTextColor(unselectedSubTextColor)
            }
            "3-5" -> {
                mediumCard.setCardBackgroundColor(selectedColor)
                mediumText.setTextColor(selectedTextColor)
                subMediumText.setTextColor(selectedTextColor)
                lowCard.setCardBackgroundColor(unselectedColor)
                lowText.setTextColor(unselectedTextColor)
                subLowText.setTextColor(unselectedSubTextColor)
                highCard.setCardBackgroundColor(unselectedColor)
                highText.setTextColor(unselectedTextColor)
                subHighText.setTextColor(unselectedSubTextColor)
            }
            "6+" -> {
                highCard.setCardBackgroundColor(selectedColor)
                highText.setTextColor(selectedTextColor)
                subHighText.setTextColor(selectedTextColor)
                lowCard.setCardBackgroundColor(unselectedColor)
                lowText.setTextColor(unselectedTextColor)
                subLowText.setTextColor(unselectedSubTextColor)
                mediumCard.setCardBackgroundColor(unselectedColor)
                mediumText.setTextColor(unselectedTextColor)
                subMediumText.setTextColor(unselectedSubTextColor)
            }
            else -> {
                lowCard.setCardBackgroundColor(unselectedColor)
                lowText.setTextColor(unselectedTextColor)
                subLowText.setTextColor(unselectedSubTextColor)
                mediumCard.setCardBackgroundColor(unselectedColor)
                mediumText.setTextColor(unselectedTextColor)
                subMediumText.setTextColor(unselectedSubTextColor)
                highCard.setCardBackgroundColor(unselectedColor)
                highText.setTextColor(unselectedTextColor)
                subHighText.setTextColor(unselectedSubTextColor)
            }
        }
    }
}