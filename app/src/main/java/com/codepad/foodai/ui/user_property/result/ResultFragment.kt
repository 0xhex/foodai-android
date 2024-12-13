// ResultFragment.kt
package com.codepad.foodai.ui.user_property.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        viewModel.fetchNutrition()
    }
}