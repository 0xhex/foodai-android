package com.codepad.foodai.ui.home.menu

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentMenuBottomSheetBinding
import com.codepad.foodai.ui.core.BaseBottomSheetFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MenuSheetFragment : BaseBottomSheetFragment<FragmentMenuBottomSheetBinding>() {
    override val layoutResourcesId: Int = R.layout.fragment_menu_bottom_sheet
    private val viewModel: HomeViewModel by activityViewModels()
    
    // URI to store the image captured by camera
    private var currentPhotoUri: Uri? = null
    
    // Target dimensions for resized images - now using larger values for higher quality
    private val TARGET_IMAGE_WIDTH = 2048
    private val TARGET_IMAGE_HEIGHT = 2048
    private val IMAGE_QUALITY = 90  // JPEG compression quality (0-100)

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoUri?.let { uri ->
                    try {
                        handleImageResult(uri)
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing camera image")
                    }
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageResult(it) }
        }

    private fun handleImageResult(uri: Uri) {
        try {
            val imageFile = processImageFromUri(uri)
            setFragmentResult("uploadResult", bundleOf("imageFile" to imageFile))
            dismiss()
        } catch (e: Exception) {
            Timber.e(e, "Error processing image")
        }
    }
    
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            }
        }

    private val requestGalleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Even if not granted, try to open gallery as modern Android versions
            // allow partial access through system picker
            openGallery()
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
                findNavController().navigate(R.id.action_menuDialog_to_exerciseSelectionFragment)
                dismiss()
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
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                    // Show rationale if needed
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
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // Show rationale if needed
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
        
        // Create file where the photo will be saved
        val photoFile = createImageFile()
        
        photoFile?.let { file ->
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            
            // Tell the camera app where to save the full-resolution image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            
            try {
                cameraLauncher.launch(intent)
            } catch (e: Exception) {
                Timber.e(e, "Error launching camera")
            }
        }
    }

    private fun openGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            Timber.e(e, "Error launching gallery")
        }
    }
    
    /**
     * Create a file to save the camera image
     */
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().filesDir
        
        return try {
            File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating image file")
            null
        }
    }

    /**
     * Process an image from a URI, resize it to reasonable dimensions,
     * and save it as a compressed JPEG file
     */
    private fun processImageFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        
        // Decode bounds without loading the full bitmap to memory
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()
        
        // Calculate scaling factor to achieve target size
        var scaleFactor = 1
        if (options.outWidth > TARGET_IMAGE_WIDTH || options.outHeight > TARGET_IMAGE_HEIGHT) {
            val widthScale = options.outWidth.toFloat() / TARGET_IMAGE_WIDTH
            val heightScale = options.outHeight.toFloat() / TARGET_IMAGE_HEIGHT
            scaleFactor = Math.max(widthScale, heightScale).toInt()
        }
        
        // Decode the bitmap with the calculated scale factor
        val decodingOptions = BitmapFactory.Options().apply {
            inSampleSize = scaleFactor
        }
        
        val secondInputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(secondInputStream, null, decodingOptions)
        secondInputStream?.close()
        
        // Log the actual dimensions for debugging
        bitmap?.let {
            Timber.d("Processed image dimensions: ${it.width}x${it.height}, original: ${options.outWidth}x${options.outHeight}")
        }
        
        // Save the bitmap to a file
        val filesDir = requireContext().filesDir
        val imageFile = File(filesDir, "uploaded_image.jpg")
        val os = FileOutputStream(imageFile)
        
        // Use higher quality (90) for better image clarity
        bitmap?.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, os)
        os.flush()
        os.close()
        
        // Log the file size for debugging
        Timber.d("Processed image file size: ${imageFile.length() / 1024} KB")
        
        return imageFile
    }
}