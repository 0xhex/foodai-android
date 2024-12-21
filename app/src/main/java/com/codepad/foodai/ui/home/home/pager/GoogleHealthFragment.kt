package com.codepad.foodai.ui.home.home.pager

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoogleHealthBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleHealthFragment : BaseFragment<FragmentGoogleHealthBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.fragment_google_health

    override fun onReadyView() {
        // Initialize your view here
    }
}