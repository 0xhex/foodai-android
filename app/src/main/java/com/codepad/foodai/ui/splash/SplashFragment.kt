package com.codepad.foodai.ui.splash

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.SplashFragmentBinding
import com.codepad.foodai.helpers.FirebaseRemoteConfigManager
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment: BaseFragment<SplashFragmentBinding>() {
    private val viewModel: SplashViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.splash_fragment
    }

    override fun onReadyView() {
        binding.viewModel = viewModel
        FirebaseRemoteConfigManager.fetchRemoteConfigurations(requireActivity() as AppCompatActivity, {})

        viewModel.userDataResponse.observe(viewLifecycleOwner) { userDataResponse ->
            if (userDataResponse != null) {
                navigateToHome()
            } else {
                navigateToOnboarding()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            viewModel.checkOnboardingAndFetchUserData()
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
    }

    private fun navigateToOnboarding() {
        findNavController().navigate(R.id.action_splashFragment_to_onBoardingFragment)
    }
}