package com.codepad.foodai.ui.home.analytics

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentAnalyticsTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsTabFragment : BaseFragment<FragmentAnalyticsTabBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_analytics_tab
    }

    override fun onReadyView() {
    }
}