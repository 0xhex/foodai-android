package com.codepad.foodai.ui.onboarding

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.OnboardingFragmentBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : BaseFragment<OnboardingFragmentBinding>() {
    private val viewModel: OnboardingViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.onboarding_fragment
    }

    override fun onReadyView() {
    }
}