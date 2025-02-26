package com.codepad.foodai.ui.home.community

import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentCommunityTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityTabFragment : BaseFragment<FragmentCommunityTabBinding>() {
    
    override fun getLayoutId(): Int = R.layout.fragment_community_tab

    override fun onReadyView() {
        setupViews()
    }

    private fun setupViews() {
        // Initialize your community views here
    }
} 