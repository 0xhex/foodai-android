package com.codepad.foodai.ui.home.home.pager.health

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoogleHealthBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleHealthFragment : BaseFragment<FragmentGoogleHealthBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fragment_google_health

    override fun onReadyView() {
        sharedViewModel.dailySummary.observe(viewLifecycleOwner) {
            binding.apply {
                progressBar.progress = (it.healthScore?.times(10))?.toInt() ?: 0
                txtProgress.text = "${it.healthScore}/10"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}