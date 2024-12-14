package com.codepad.foodai.ui.home.settings

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentSettingsBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsTabFragment : BaseFragment<FragmentSettingsBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun onReadyView() {
        // Additional setup if needed
    }
}