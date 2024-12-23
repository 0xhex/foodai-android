package com.codepad.foodai.ui.home.menu

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentMenuBottomSheetBinding
import com.codepad.foodai.ui.core.BaseBottomSheetFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.MenuOption
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuSheetFragment : BaseBottomSheetFragment<FragmentMenuBottomSheetBinding>() {
    override val layoutResourcesId: Int = R.layout.fragment_menu_bottom_sheet
    private val viewModel: HomeViewModel by activityViewModels()

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {

                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            }
        }

    private val requestGalleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.parent as View
        setRoundedCornerBottomSheetUI(bottomSheet)
        setStateAsExpanded(bottomSheet)
    }

    override fun listenClick() {
        super.listenClick()
        binding.apply {
            btnScanFood.setOnClickListener {
                showScanOptionsDialog()
            }

            btnLogExercise.setOnClickListener {
                viewModel.setOptionSelected(MenuOption.LOG_FOOD)
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun showScanOptionsDialog() {
        SelectCameraOptionDialog { which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> checkGalleryPermission()
            }
        }.show(parentFragmentManager, "SelectCameraOptionDialog")
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }

                else -> {
                    requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }

                else -> {
                    requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}