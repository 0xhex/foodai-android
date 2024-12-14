package com.codepad.foodai.ui.home.home

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_home_tab
    }

    override fun onReadyView() {
        // Additional setup if needed
    }
}