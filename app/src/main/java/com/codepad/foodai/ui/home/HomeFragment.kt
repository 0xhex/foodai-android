package com.codepad.foodai.ui.home

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.HomeFragmentBinding
import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.loading.LoadingType
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeFragmentBinding>() {
    private val viewModel: HomeViewModel by activityViewModels()

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

        setFragmentResultListener("uploadResult") { key, bundle ->
            val imageFile = bundle.getSerializable("imageFile") as File
            val userID = UserSession.user?.id.orEmpty()
            val fileName = "uploaded_image.jpg"
            val mimeType = "image/jpeg"
            viewModel.uploadImage(userID, imageFile, fileName, mimeType)
        }

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
                }

                is HomeViewModel.HomeEvent.OnImageFetchStarted -> {

                }
                is HomeViewModel.HomeEvent.OnImageFetchSuccess -> {
                    handleFetchedImage(event.response)
                }
                is HomeViewModel.HomeEvent.OnImageFetchError -> {
                }
            }
        }
    }


    private fun handleFetchedImage(imageData: ImageData) {

    }

    private fun showLoadingView(loadingType: LoadingType) {
        binding.loadingView.apply {
            setLoadingType(loadingType)
            visibility = View.VISIBLE
        }
    }

    private fun hideLoadingView() {
        binding.loadingView.visibility = View.GONE
        binding.loadingView.stopLoading()
    }
}