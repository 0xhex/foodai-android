package com.codepad.foodai.ui.onboarding

import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.databinding.OnboardingFragmentBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.onboarding.pager.OnboardingPagerAdapter
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : BaseFragment<OnboardingFragmentBinding>() {
    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: MaterialButton

    override fun getLayoutId() = R.layout.onboarding_fragment

    override fun onReadyView() {
        viewPager = binding.viewPager
        nextButton = binding.nextButton

        val adapter = OnboardingPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        nextButton.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                viewPager.currentItem = nextItem
            } else {
                // Handle the end of the onboarding
            }
        }
    }
}