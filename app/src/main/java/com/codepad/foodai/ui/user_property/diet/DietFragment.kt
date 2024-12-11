package com.codepad.foodai.ui.user_property.diet

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentDietBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DietFragment : BaseFragment<FragmentDietBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_diet

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        sharedViewModel.showWarning.observe(viewLifecycleOwner) { showWarning ->
            if (showWarning) {
                Toast.makeText(
                    requireContext(),
                    R.string.please_select_a_diet,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sharedViewModel.selectedDiet.observe(viewLifecycleOwner) { diet ->
            updateButtonStyles(diet)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.selectedDiet.value != null) {
            sharedViewModel.selectDiet(sharedViewModel.selectedDiet.value!!)
        }
    }

    private fun updateButtonStyles(selectedDiet: String?) {
        val classicButton = binding.btnClassic
        val pescatarianButton = binding.btnPescatarian
        val vegetarianButton = binding.btnVegetarian
        val veganButton = binding.btnVegan

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.blue_button)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        when (selectedDiet) {
            "classic" -> {
                classicButton.setBackgroundColor(selectedColor)
                classicButton.setTextColor(selectedTextColor)
                pescatarianButton.setBackgroundColor(unselectedColor)
                pescatarianButton.setTextColor(unselectedTextColor)
                vegetarianButton.setBackgroundColor(unselectedColor)
                vegetarianButton.setTextColor(unselectedTextColor)
                veganButton.setBackgroundColor(unselectedColor)
                veganButton.setTextColor(unselectedTextColor)
            }
            "pescatarian" -> {
                pescatarianButton.setBackgroundColor(selectedColor)
                pescatarianButton.setTextColor(selectedTextColor)
                classicButton.setBackgroundColor(unselectedColor)
                classicButton.setTextColor(unselectedTextColor)
                vegetarianButton.setBackgroundColor(unselectedColor)
                vegetarianButton.setTextColor(unselectedTextColor)
                veganButton.setBackgroundColor(unselectedColor)
                veganButton.setTextColor(unselectedTextColor)
            }
            "vegetarian" -> {
                vegetarianButton.setBackgroundColor(selectedColor)
                vegetarianButton.setTextColor(selectedTextColor)
                classicButton.setBackgroundColor(unselectedColor)
                classicButton.setTextColor(unselectedTextColor)
                pescatarianButton.setBackgroundColor(unselectedColor)
                pescatarianButton.setTextColor(unselectedTextColor)
                veganButton.setBackgroundColor(unselectedColor)
                veganButton.setTextColor(unselectedTextColor)
            }
            "vegan" -> {
                veganButton.setBackgroundColor(selectedColor)
                veganButton.setTextColor(selectedTextColor)
                classicButton.setBackgroundColor(unselectedColor)
                classicButton.setTextColor(unselectedTextColor)
                pescatarianButton.setBackgroundColor(unselectedColor)
                pescatarianButton.setTextColor(unselectedTextColor)
                vegetarianButton.setBackgroundColor(unselectedColor)
                vegetarianButton.setTextColor(unselectedTextColor)
            }
            else -> {
                classicButton.setBackgroundColor(unselectedColor)
                classicButton.setTextColor(unselectedTextColor)
                pescatarianButton.setBackgroundColor(unselectedColor)
                pescatarianButton.setTextColor(unselectedTextColor)
                vegetarianButton.setBackgroundColor(unselectedColor)
                vegetarianButton.setTextColor(unselectedTextColor)
                veganButton.setBackgroundColor(unselectedColor)
                veganButton.setTextColor(unselectedTextColor)
            }
        }
    }
}