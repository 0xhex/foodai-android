package com.codepad.foodai.ui.home

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.HomeFragmentBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeFragmentBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.home_fragment
    }

    override fun onReadyView() {
        val navController =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_home)?.findNavController()
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation

        navController?.let {
            bottomNavigationView.setupWithNavController(it)
        }

        binding.imgPlus.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_menuDialog)
        }

        binding.imgCircle.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_menuDialog)
        }
    }
}