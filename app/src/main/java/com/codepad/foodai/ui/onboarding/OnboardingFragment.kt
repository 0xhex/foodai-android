package com.codepad.foodai.ui.onboarding

import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.OnboardingFragmentBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.onboarding.pager.OnboardingPagerAdapter
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : BaseFragment<OnboardingFragmentBinding>() {
    private val viewModel: OnboardingViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: MaterialButton
    private lateinit var lottieAnimation: LottieAnimationView

    override fun getLayoutId() = R.layout.onboarding_fragment

    override fun onReadyView() {
        viewPager = binding.viewPager
        nextButton = binding.nextButton
        lottieAnimation = binding.lottieAnimation

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
                // Handle the end of the onboarding
            }
        }

        viewModel.registerUserResponse.observe(viewLifecycleOwner) { registerUserResponse ->
            if (registerUserResponse != null) {
                // login revenue cat user
            }
        }

        viewModel.registerUser()
    }
}