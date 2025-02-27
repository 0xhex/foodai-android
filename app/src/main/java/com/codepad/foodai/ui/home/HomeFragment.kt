package com.codepad.foodai.ui.home

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.HomeFragmentBinding
import com.codepad.foodai.helpers.FirebaseManager
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.helpers.setupWithNavControllerSafely
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.loading.LoadingType
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.onesignal.OneSignal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeFragmentBinding>() {
    private val viewModel: HomeViewModel by activityViewModels()

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    @Inject
    lateinit var firebaseManager: FirebaseManager

    override fun getLayoutId(): Int {
        return R.layout.home_fragment
    }

    override fun onReadyView() {
        val navController =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_home)?.findNavController()
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation

        navController?.let {
            bottomNavigationView.setupWithNavControllerSafely(it)
        }

        binding.imgPlus.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_menuDialog)
        }

        binding.imgCircle.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_menuDialog)
        }

        setFragmentResultListener("uploadResult") { key, bundle ->
            val imageFile = bundle.getSerializable("imageFile") as File
            val userID = UserSession.user?.id.orEmpty()
            val fileName = "uploaded_image.jpg"
            val mimeType = "image/jpeg"
            viewModel.uploadImage(userID, imageFile, fileName, mimeType)
        }

        setupObservers()
        requestNotificationPermission()
    }

    private fun setupObservers() {
        viewModel.homeEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is HomeViewModel.HomeEvent.OnMenuOptionSelected -> {
                    // Handle menu option selected
                }

                is HomeViewModel.HomeEvent.OnImageUploadStarted -> {
                    showLoadingView(LoadingType.UPLOAD_FILE)
                }

                is HomeViewModel.HomeEvent.OnImageUploadSuccess -> {
                    hideLoadingView()
                }

                is HomeViewModel.HomeEvent.OnImageUploadError -> {
                    hideLoadingView()
                    showErrorBanner(event.errorMessage)
                }

                is HomeViewModel.HomeEvent.OnImageFetchStarted -> {

                }

                is HomeViewModel.HomeEvent.OnImageFetchSuccess -> {}

                is HomeViewModel.HomeEvent.OnImageFetchError -> {
                    showErrorBanner(event.errorMessage)
                }

                null -> {}
            }
        }

        viewModel.launchFoodLogDialog.observe(viewLifecycleOwner) { event ->
            if (event) {
                findNavController().navigate(R.id.action_homeFragment_to_menuDialog)
                viewModel.launchFoodLogDialog.value = false
            }
        }

        // Observe paywall trigger
        var isNavigatingToPaywall = false
        viewLifecycleOwner.lifecycleScope.launch {
            revenueCatManager.showPaywall.collectLatest { show ->
                if (show && !isNavigatingToPaywall) {
                    isNavigatingToPaywall = true
                    findNavController().navigate(R.id.action_home_to_paywall)
                    revenueCatManager.resetPaywallTrigger()
                    isNavigatingToPaywall = false
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        viewLifecycleOwner.lifecycleScope.launch {
            OneSignal.Notifications.requestPermission(true)
        }
        OneSignal.login(UserSession.user?.id.orEmpty())
    }

    private fun showLoadingView(loadingType: LoadingType) {
        binding.loadingView.apply {
            setLoadingType(loadingType)
            visibility = View.VISIBLE
        }
    }

    private fun showErrorBanner(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    private fun hideLoadingView() {
        binding.loadingView.visibility = View.GONE
        binding.loadingView.stopLoading()
    }
}