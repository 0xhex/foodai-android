package com.codepad.foodai.ui.home.home.fooddetail

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFoodDetailBinding
import com.codepad.foodai.extensions.toHourString
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class FoodDetailFragment : BaseFragment<FragmentFoodDetailBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_food_detail

    override fun onReadyView() {
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        sharedViewModel.foodDetail.observe(viewLifecycleOwner) { foodDetail ->
            binding.apply {
                txtTime.text = "⏱ ${foodDetail.createdAt?.toHourString()}"
                txtTitle.text = foodDetail.ingredients?.firstOrNull()?.name ?: "N/A"
                Glide.with(binding.imgFood)
                    .load(foodDetail.url)
                    .centerCrop()
                    .into(binding.imgFood)
                val nutritionItems = foodDetail.ingredients?.map {
                    NutritionItem(
                        it.name.orEmpty(),
                        it.calory.toString()
                    )
                }
                val adapter = nutritionItems?.let { NutritionAdapter(it) }
                binding.rvNutritions.layoutManager = GridLayoutManager(context, 2)
                binding.rvNutritions.adapter = adapter

                binding.txtCaloriesDesc.text = "${foodDetail.calories} kcal"
                binding.txtProteinDesc.text = "${foodDetail.protein} g"
                binding.txtCarbDesc.text = "${foodDetail.carbs} g"
                binding.txtFatDesc.text = "${foodDetail.fats} g"

                binding.apply {
                    progressBar.progress = (foodDetail.healthScore?.times(10))?.toInt() ?: 0
                    txtProgress.text = "${foodDetail.healthScore}/10"
                }
            }
        }

        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        sharedViewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success != null) {  // Only handle non-null values
                if (success) {
                    Snackbar.make(binding.root, "Food deleted successfully", Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack()
                } else {
                    Snackbar.make(binding.root, "Failed to delete food", Snackbar.LENGTH_SHORT).show()
                }
                // Clear the value after handling it
                sharedViewModel.clearDeleteResult()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnShare.setOnClickListener {
            shareScreenshot()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnFix.setOnClickListener {
            // Navigate to FixResultFragment
        }

        binding.btnSave.setOnClickListener {
            // Implement save functionality
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_food))
            .setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                deleteFood()
            }
            .show()
    }

    private fun deleteFood() {
        sharedViewModel.foodDetail.value?.id?.let { imageId ->
            sharedViewModel.deleteImage(imageId)
        }
    }

    private fun shareScreenshot() {
        val rootView = binding.root
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false

        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "screenshot.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share screenshot"))
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Failed to share screenshot", Snackbar.LENGTH_SHORT).show()
        }
    }
}