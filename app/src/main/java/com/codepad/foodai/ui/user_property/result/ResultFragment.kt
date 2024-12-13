// ResultFragment.kt
package com.codepad.foodai.ui.user_property.result

import android.graphics.Color
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentResultBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : BaseFragment<FragmentResultBinding>() {

    private val viewModel: ResultViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_result

    override fun onReadyView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.nextButton.setOnClickListener {
            viewModel.navigateToNextScreen()
            findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
        }

        binding.layoutCarbs.circularProgressBar.progressBarColor = R.color.orange
        binding.layoutProtein.circularProgressBar.progressBarColor = R.color.red
        binding.layoutFats.circularProgressBar.progressBarColor = R.color.blue_button
        binding.layoutCalories.circularProgressBar.progressBarColor = R.color.black

        viewModel.fetchNutrition()
    }
}