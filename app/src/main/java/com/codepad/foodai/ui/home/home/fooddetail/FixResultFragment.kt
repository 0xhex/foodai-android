package com.codepad.foodai.ui.home.home.fooddetail

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFixResultBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.codepad.foodai.ui.user_property.loading.LoadingType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FixResultFragment : BaseFragment<FragmentFixResultBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_fix_result
    override fun onReadyView() {
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        sharedViewModel.fixResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it) {
                    Snackbar.make(requireView(), R.string.fix_result_success, Snackbar.LENGTH_SHORT)
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                if (isAdded && !isDetached) {
                                    findNavController().popBackStack()
                                }
                            }
                        }).show()
                }
                sharedViewModel.clearFixResult()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnUpdate.setOnClickListener {
            val description = binding.editDescription.text.toString()
            if (description.isNotBlank()) {
                sharedViewModel.foodDetail.value?.id?.let { imageId ->
                    sharedViewModel.fixImageResults(imageId, description)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.isVisible = show
        if (show) {
            binding.loadingView.setLoadingType(LoadingType.EDITING_FOOD)
        }
    }
} 