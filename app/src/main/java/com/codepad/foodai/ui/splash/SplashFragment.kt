package com.codepad.foodai.ui.splash

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.SplashFragmentBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashFragmentBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.splash_fragment
    }

    override fun onReadyView() {
    }
}