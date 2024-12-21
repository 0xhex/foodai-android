package com.codepad.foodai.ui.home.home.pager.goals

import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoalViewBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalViewFragment : BaseFragment<FragmentGoalViewBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.fragment_goal_view

    override fun onReadyView() {

    }
}