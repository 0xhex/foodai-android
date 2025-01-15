package com.codepad.foodai.ui.home.analytics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentAnalyticsTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsTabFragment : BaseFragment<FragmentAnalyticsTabBinding>() {

    private val viewModel: AnalyticsViewModel by viewModels()
    override fun getLayoutId(): Int = R.layout.fragment_analytics_tab

    override fun onReadyView() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
        viewModel.fetchUserData()
    }

    private fun setupViews() {
        binding.apply {
            btnUpdateGoal.setOnClickListener {
                findNavController().navigate(R.id.action_analytics_to_goal_weight_update)
            }

            btnUpdateWeight.setOnClickListener {
                findNavController().navigate(R.id.action_analytics_to_weight_update)
            }
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.apply {
                tvGoalWeight.text = getString(R.string.goal_weight, "${userData.targetWeight} kg")
                tvCurrentWeight.text = getString(R.string.current_weight_param, "${userData.weight} kg")
            }
        }
    }
}