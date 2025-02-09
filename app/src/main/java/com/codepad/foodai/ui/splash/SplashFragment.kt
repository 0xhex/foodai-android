package com.codepad.foodai.ui.splash

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.SplashFragmentBinding
import com.codepad.foodai.helpers.FirebaseRemoteConfigManager
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashFragmentBinding>() {
    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    override fun getLayoutId(): Int {
        return R.layout.splash_fragment
    }

    override fun onReadyView() {
        binding.viewModel = viewModel
        FirebaseRemoteConfigManager.fetchRemoteConfigurations(
            requireActivity() as AppCompatActivity,
            {})
        revenueCatManager.init()

        viewModel.userDataResponse.observe(viewLifecycleOwner) { userDataResponse ->
            if (userDataResponse != null) {
                revenueCatManager.logInUser(userDataResponse.id) {
                    navigateToHome()
                }
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
        try {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.splashFragment) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
                navController.navigate(R.id.action_splashFragment_to_homeFragment, null, navOptions)
            }
        } catch (e: Exception) {
            Timber.e(e, "Navigation error")
        }
    }

    private fun navigateToOnboarding() {
        try {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.splashFragment) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
                navController.navigate(R.id.action_splashFragment_to_onBoardingFragment, null, navOptions)
            }
        } catch (e: Exception) {
            Timber.e(e, "Navigation error")
        }
    }
}