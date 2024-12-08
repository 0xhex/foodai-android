package com.codepad.foodai.ui.onboarding.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codepad.foodai.R

class OnboardingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val pages = listOf(
        OnboardingPage(R.string.quick_calorie_tracking, R.string.fast_photo_of_food),
        OnboardingPage(R.string.advanced_nutrition_tracking, R.string.get_in_depth_info),
        OnboardingPage(R.string.reach_your_health_goals, R.string.start_now_to_transform_your_body)
    )

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        val page = pages[position]
        return OnboardingPageFragment.newInstance(page.titleRes, page.descRes)
    }
}