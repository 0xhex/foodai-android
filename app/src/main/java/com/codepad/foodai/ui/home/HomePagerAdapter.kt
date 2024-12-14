package com.codepad.foodai.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codepad.foodai.ui.home.analytics.AnalyticsTabFragment
import com.codepad.foodai.ui.home.home.HomeTabFragment
import com.codepad.foodai.ui.home.settings.SettingsTabFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeTabFragment()
            1 -> AnalyticsTabFragment()
            2 -> SettingsTabFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}