package com.codepad.foodai.ui.user_property.result.edit

import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentEditCaloriesBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.result.ResultViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditCaloriesFragment : BaseFragment<FragmentEditCaloriesBinding>() {

    private val viewModel: ResultViewModel by viewModels()
    override fun getLayoutId() = R.layout.fragment_edit_calories
    private val args by navArgs<EditCaloriesFragmentArgs>()

    override fun onReadyView() {
        val title = args.title
        val textAreaTitle = args.textAreaTitle
        val nutritionType = args.nutritionType
        val value = args.value
        val color = args.color

        binding.title.text = title
        binding.value.editText?.hint = textAreaTitle
        binding.etValue.setText(value)
        binding.circleProgressBar.progressBarColor = color

        binding.imgParameter.setImageResource(
            when (nutritionType) {
                "dailyCarb" -> R.drawable.carbs
                "dailyProtein" -> R.drawable.protein
                "dailyFat" -> R.drawable.fats
                "dailyCalories" -> R.drawable.ic_flame
                else -> R.drawable.ic_flame
            }
        )

        binding.btnRevert.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDone.setOnClickListener {
            val userID = UserSession.user?.id ?: return@setOnClickListener
            val fieldValue =
                binding.etValue.text.toString().toIntOrNull() ?: return@setOnClickListener
            viewModel.updateUserFields(userID, nutritionType, fieldValue.toString())
        }

        viewModel.updatedUser.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                setFragmentResult("updateRequestKey", bundleOf("updated" to true))
                findNavController().popBackStack()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}