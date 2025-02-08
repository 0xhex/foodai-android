package com.codepad.foodai.ui.onboarding

import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.OnboardingFragmentBinding
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.onboarding.pager.OnboardingPagerAdapter
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : BaseFragment<OnboardingFragmentBinding>() {
    private val viewModel: OnboardingViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: MaterialButton
    private lateinit var lottieAnimation: LottieAnimationView

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    override fun getLayoutId() = R.layout.onboarding_fragment

    override fun onReadyView() {
        viewPager = binding.viewPager
        nextButton = binding.nextButton
        lottieAnimation = binding.lottieAnimation
        revenueCatManager.init()

        val adapter = OnboardingPagerAdapter(requireActivity())
        viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(viewPager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        lottieAnimation.setAnimation(R.raw.scan)
                        nextButton.setText(R.string.next)
                    }

                    1 -> {
                        lottieAnimation.setAnimation(R.raw.food_calorie)
                        nextButton.setText(R.string.next)
                    }

                    2 -> {
                        lottieAnimation.setAnimation(R.raw.slim_figure)
                        nextButton.setText(R.string.get_started)
                    }
                }
                lottieAnimation.playAnimation()
            }
        })

        nextButton.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                viewPager.currentItem = nextItem
            } else {
                try {
                    val navController = findNavController()
                    if (navController.currentDestination?.id == R.id.onBoardingFragment) {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.onBoardingFragment, true)
                            .build()
                        navController.navigate(
                            R.id.action_onBoardingFragment_to_userPropertyFragment,
                            null,
                            navOptions
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Navigation error")
                }
            }
        }

        viewModel.registerUserResponse.observe(viewLifecycleOwner) { registerUserResponse ->
            if (registerUserResponse != null) {
                val userID = registerUserResponse.id
                revenueCatManager.logInUser(userID) {}
            }
        }

        viewModel.registerUser()
    }
}