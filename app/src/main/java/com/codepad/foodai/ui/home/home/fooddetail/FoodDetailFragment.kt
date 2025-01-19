package com.codepad.foodai.ui.home.home.fooddetail

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class FoodDetailFragment : BaseFragment<FragmentFoodDetailBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()
    private var recommendationPollingHandler: Handler? = null
    private var recommendationPollingRunnable: Runnable? = null
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_food_detail

    override fun onReadyView() {
        setupObservers()
        setupClickListeners()
        setupRecommendationCard()
    }

    private fun setupRecommendationCard() {
        binding.recommendationCard.apply {
            onGetRecommendationsClick = {
                sharedViewModel.requestRecommendations()
            }
            onTryAgainClick = {
                sharedViewModel.requestRecommendations()
            }
        }
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

                binding.recommendationCard.setHealthScore(foodDetail.healthScore?.toDouble() ?: 0.0)
                
                // Only auto-request recommendation if this food already has one
                if (foodDetail.recommendationId != null) {
                    sharedViewModel.requestRecommendations()
                }
            }
        }

        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        sharedViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it) {
                    Snackbar.make(binding.root, R.string.delete_success, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack()
                } else {
                    Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT).show()
                }
                sharedViewModel.clearDeleteResult()
            }
        }

        sharedViewModel.fixResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it) {
                    Snackbar.make(binding.root, R.string.fix_result_success, Snackbar.LENGTH_SHORT)
                        .show()
                } else {
                    Snackbar.make(binding.root, R.string.fix_result_error, Snackbar.LENGTH_SHORT)
                        .show()
                }
                sharedViewModel.clearFixResult()
            }
        }

        sharedViewModel.recommendationId.observe(viewLifecycleOwner) { recommendationId ->
            recommendationId?.let {
                startPollingRecommendation()
            }
        }

        sharedViewModel.recommendation.observe(viewLifecycleOwner) { recommendation ->
            recommendation?.let {
                when (it.status) {
                    "completed" -> {
                        binding.recommendationCard.showRecommendations(it)
                        stopPollingRecommendation()
                    }

                    "failed" -> {
                        binding.recommendationCard.showError("Recommendations generation failed.")
                        stopPollingRecommendation()
                    }
                }
            }
        }

        sharedViewModel.recommendationError.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it.errorCode.toString() == "PREMIUM_REQUIRED") {
                    // Show premium required dialog
                    showPremiumRequiredDialog()
                } else {
                    binding.recommendationCard.showError(it.message ?: "Unknown error occurred")
                }
                stopPollingRecommendation()
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
            findNavController().navigate(R.id.fixResultFragment)
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

    private fun showFixResultDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_fix_result, null)
        val promptInput = dialogView.findViewById<TextInputEditText>(R.id.prompt_input)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.fix_result_submit) { dialog, _ ->
                val prompt = promptInput.text?.toString()
                if (!prompt.isNullOrBlank()) {
                    sharedViewModel.foodDetail.value?.id?.let { imageId ->
                        sharedViewModel.fixImageResults(imageId, prompt)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.fix_result_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startPollingRecommendation() {
        recommendationPollingHandler = Handler(Looper.getMainLooper())
        recommendationPollingRunnable = object : Runnable {
            override fun run() {
                sharedViewModel.getRecommendation()
                recommendationPollingHandler?.postDelayed(this, 3000)
            }
        }
        recommendationPollingRunnable?.let {
            recommendationPollingHandler?.post(it)
        }
    }

    private fun stopPollingRecommendation() {
        recommendationPollingRunnable?.let {
            recommendationPollingHandler?.removeCallbacks(it)
        }
        recommendationPollingHandler = null
        recommendationPollingRunnable = null
    }

    private fun showPremiumRequiredDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Premium Required")
            .setMessage("You've Reached Today's Limit! Unlock unlimited access and exclusive features by upgrading to Premium.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Upgrade") { _, _ ->
                // TODO: Implement premium upgrade flow
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPollingRecommendation()
    }
}