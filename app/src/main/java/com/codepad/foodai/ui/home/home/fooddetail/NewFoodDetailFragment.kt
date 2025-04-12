package com.codepad.foodai.ui.home.home.fooddetail

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentNewFoodDetailBinding
import com.codepad.foodai.domain.models.image.ImageData
import com.codepad.foodai.domain.models.recommendation.Recommendation
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.community.detail.adapter.IngredientsAdapter
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class NewFoodDetailFragment : BaseFragment<FragmentNewFoodDetailBinding>(),
    MoreOptionsBottomSheet.OptionsListener {

    companion object {
        private var hasRunInitialAnimations = false
        private var lastAnimatedFoodId: String? = null
    }

    private val sharedViewModel: HomePagerViewModel by activityViewModels()
    private var recommendationPollingHandler: Handler? = null
    private var recommendationPollingRunnable: Runnable? = null
    private var nutritionDetailsPollingHandler: Handler? = null
    private var nutritionDetailsPollingRunnable: Runnable? = null
    private var isShared = false
    private var isIngredientsExpanded = true

    private var loadingMessageIndex = 0
    private val recommendationLoadingMessages = arrayOf(
        R.string.analyzing_your_meal,
        R.string.fetching_personalized_insights,
        R.string.finalizing_your_unique_recommendations
    )

    private val nutritionLoadingMessages = arrayOf(
        R.string.analyzing_nutritional_content,
        R.string.calculating_vitamin_and_mineral_levels,
        R.string.preparing_your_nutrition_profile
    )

    // Constants
    private val HEALTH_SCORE_CONFETTI_THRESHOLD = 7.0

    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_new_food_detail

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Set up ingredient adapter with empty list initially
        binding.ingredientsGrid.layoutManager = GridLayoutManager(context, 2)
        binding.ingredientsGrid.adapter = IngredientsAdapter()

        return view
    }

    override fun onReadyView() {
        setupViews()
        setupObservers()
        setupClickListeners()
        setupFragmentResultListeners()
        checkIfShared()
    }

    private fun setupFragmentResultListeners() {
        // Listen for food nutrition updates
        setFragmentResultListener("updateFoodNutrition") { _, bundle ->
            val type = bundle.getString("type") ?: return@setFragmentResultListener
            val value = bundle.getInt("value")
            
            // Update the food detail in ViewModel
            sharedViewModel.foodDetail.value?.let { food ->
                val updatedFood = food.copy().apply {
                    when (type) {
                        "calories" -> calories = value
                        "protein" -> protein = value
                        "carbs" -> carbs = value
                        "fats" -> fats = value
                    }
                }
                sharedViewModel.updateFoodDetail(updatedFood)
            }
        }
    }

    private fun setupViews() {
        // Initial state setup
        binding.ingredientsContent.visibility = View.VISIBLE
        binding.recommendationCardView.visibility = View.VISIBLE
        binding.btnFixResult.visibility = View.VISIBLE

        // Setup recommendation card callbacks
        binding.recommendationCardView.apply {
            onGetRecommendationsClick = {
                sharedViewModel.requestRecommendations()
            }
            onTryAgainClick = {
                sharedViewModel.requestRecommendations()
            }
            onUpgradeClick = {
                revenueCatManager.triggerPaywall()
            }
        }
        
        // Setup nutrition details view callbacks
        binding.nutritionDetailsView.setOnNeedPremiumCallback {
            revenueCatManager.triggerPaywall()
        }
        
        // Connect nutrition details view to the ViewModel
        binding.nutritionDetailsView.connectToViewModel(sharedViewModel, viewLifecycleOwner)

        // Setup nutrition ring click listeners
        binding.nutritionRingView.apply {
            setOnCaloriesClickListener {
                val action = NewFoodDetailFragmentDirections.actionNewFoodDetailFragmentToEditCaloriesFragment(
                    title = getString(R.string.calories),
                    textAreaTitle = getString(R.string.calories),
                    nutritionType = "calories",
                    value = sharedViewModel.foodDetail.value?.calories?.toString() ?: "0",
                    color = ContextCompat.getColor(requireContext(), CARBS_COLOR)
                )
                findNavController().navigate(action)
            }

            setOnProteinClickListener {
                val action = NewFoodDetailFragmentDirections.actionNewFoodDetailFragmentToEditCaloriesFragment(
                    title = getString(R.string.protein),
                    textAreaTitle = getString(R.string.protein),
                    nutritionType = "protein",
                    value = sharedViewModel.foodDetail.value?.protein?.toString() ?: "0",
                    color = ContextCompat.getColor(requireContext(), PROTEIN_COLOR)
                )
                findNavController().navigate(action)
            }

            setOnCarbsClickListener {
                val action = NewFoodDetailFragmentDirections.actionNewFoodDetailFragmentToEditCaloriesFragment(
                    title = getString(R.string.carbs),
                    textAreaTitle = getString(R.string.carbs),
                    nutritionType = "carbs",
                    value = sharedViewModel.foodDetail.value?.carbs?.toString() ?: "0",
                    color = ContextCompat.getColor(requireContext(), CARBS_COLOR)
                )
                findNavController().navigate(action)
            }

            setOnFatsClickListener {
                val action = NewFoodDetailFragmentDirections.actionNewFoodDetailFragmentToEditCaloriesFragment(
                    title = getString(R.string.fats),
                    textAreaTitle = getString(R.string.fats),
                    nutritionType = "fats",
                    value = sharedViewModel.foodDetail.value?.fats?.toString() ?: "0",
                    color = ContextCompat.getColor(requireContext(), FATS_COLOR)
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun checkIfShared() {
        sharedViewModel.foodDetail.value?.id?.let { imageId ->
            isShared = requireContext().getSharedPreferences("food_prefs", Context.MODE_PRIVATE)
                .getBoolean("${imageId}_isShared", false)
        }
    }

    private fun setupObservers() {
        sharedViewModel.foodDetail.observe(viewLifecycleOwner) { foodDetail ->
            updateFoodDetailUI(foodDetail)

            // If this food already has a recommendation ID, auto-fetch it
            if (!foodDetail.recommendationId.isNullOrEmpty()) {
                sharedViewModel.requestRecommendations()
            }
        }

        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Only show loading for operations other than nutrition and recommendations
            if (isLoading && !sharedViewModel.isNutritionOrRecommendationLoading()) {
                binding.progressOverlay.visibility = View.VISIBLE
            } else {
                binding.progressOverlay.visibility = View.GONE
            }
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
                showRecommendationLoadingState()
                startPollingRecommendation()
            }
        }

        sharedViewModel.recommendation.observe(viewLifecycleOwner) { recommendation ->
            recommendation?.let {
                when (it.status) {
                    "completed" -> {
                        showRecommendationContent(it)
                        stopPollingRecommendation()
                    }

                    "failed" -> {
                        showRecommendationError("Recommendations generation failed.")
                        stopPollingRecommendation()
                    }
                }
            }
        }

        sharedViewModel.recommendationError.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it.errorCode.toString() == "PREMIUM_REQUIRED") {
                    showRecommendationPremiumRequired()
                } else {
                    showRecommendationError(it.message ?: "Unknown error occurred")
                }
                stopPollingRecommendation()
            }
        }

        sharedViewModel.createPostResult.observe(viewLifecycleOwner) { result ->
            binding.progressOverlay.isVisible = false
            // Save shared state
            sharedViewModel.foodDetail.value?.id?.let { imageId ->
                requireContext().getSharedPreferences("food_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("${imageId}_isShared", true)
                    .apply()
            }

            isShared = true
            Snackbar.make(
                binding.root,
                getString(R.string.shared_on_community),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateFoodDetailUI(foodDetail: ImageData) {
        // Load food image
        Glide.with(this)
            .load(foodDetail.url)
            .into(binding.imgFood)

        // Set meal name
        binding.txtMealName.text = foodDetail.mealName ?: "Food"

        // Run animations if this is a different food or first time
        val shouldAnimate = lastAnimatedFoodId != foodDetail.id
        binding.nutritionRingView.updateWithNutritionData(foodDetail, shouldAnimate)
        if (shouldAnimate) {
            lastAnimatedFoodId = foodDetail.id
        }

        // Set health score with appropriate color
        val healthScore = foodDetail.healthScore?.toDouble() ?: 0.0
        binding.txtHealthScore.text = String.format("%.1f", healthScore)
        
        // Get the health score color based on score value
        val healthScoreColor = getHealthScoreColor(healthScore)
        
        // Set text color
        binding.txtHealthScore.setTextColor(healthScoreColor)
        
        // Create a GradientDrawable for the inner stroke with the health score color
        val innerStroke = GradientDrawable()
        innerStroke.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_10dp).toFloat()
        innerStroke.setStroke(resources.getDimensionPixelSize(R.dimen.dimen_1dp), 
            Color.argb(38, Color.red(healthScoreColor), Color.green(healthScoreColor), Color.blue(healthScoreColor)))
        
        // Create a GradientDrawable for the outer blur stroke
        val outerStroke = GradientDrawable()
        outerStroke.cornerRadius = resources.getDimensionPixelSize(R.dimen.dimen_10dp).toFloat()
        outerStroke.setStroke(resources.getDimensionPixelSize(R.dimen.dimen_1dp), 
            Color.argb(64, Color.red(healthScoreColor), Color.green(healthScoreColor), Color.blue(healthScoreColor)))
        
        // Apply the drawable to the health score container
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // For newer versions, we can use layer drawable for more complex effects
            val layerDrawable = binding.healthScoreContainer.background as LayerDrawable
            
            // Get the third item (outer stroke) and set its color
            val outerStrokeShape = layerDrawable.getDrawable(2) as GradientDrawable
            outerStrokeShape.setStroke(resources.getDimensionPixelSize(R.dimen.dimen_1dp), 
                Color.argb(38, Color.red(healthScoreColor), Color.green(healthScoreColor), Color.blue(healthScoreColor)))
        }
        
        // Apply shadow effect using elevation to match iOS
        binding.healthScoreContainer.elevation = 8f
        
        // Apply outer shadow using TranslationZ for a glow effect
        binding.healthScoreContainer.translationZ = 1f
        
        // Create a shadow color based on health score color
        val shadowColor = Color.argb(
            38, // 15% opacity
            Color.red(healthScoreColor),
            Color.green(healthScoreColor),
            Color.blue(healthScoreColor)
        )
        
        // Apply the shadow color using OutlineProvider if available
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            binding.healthScoreContainer.outlineAmbientShadowColor = shadowColor
            binding.healthScoreContainer.outlineSpotShadowColor = shadowColor
        }
        
        // Set image ID for nutrition details view
        binding.nutritionDetailsView.setImageId(foodDetail.id)

        // Update ingredients
        foodDetail.ingredients?.let { ingredients ->
            (binding.ingredientsGrid.adapter as IngredientsAdapter).updateData(ingredients)
            binding.txtIngredientsCount.text = getString(R.string.items_format, ingredients.size)
        }

        // Check if health score is high enough to show confetti animation
        if (healthScore > HEALTH_SCORE_CONFETTI_THRESHOLD) {
            lifecycleScope.launch {
                delay(1500) // Wait for UI to settle
                binding.confettiView.visibility = View.VISIBLE
                binding.confettiView.playAnimation()
                
                // Provide haptic feedback
                requireView().performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                
                delay(3000) // Duration of animation
                binding.confettiView.visibility = View.GONE
            }
        }
    }

    private fun getHealthScoreColor(score: Double): Int {
        return when {
            score > 7.0 -> ContextCompat.getColor(requireContext(), R.color.green)
            score > 4.0 -> ContextCompat.getColor(requireContext(), R.color.yellow)
            else -> ContextCompat.getColor(requireContext(), R.color.red)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMore.setOnClickListener {
            val healthScore = sharedViewModel.foodDetail.value?.healthScore?.toDouble() ?: 0.0
            val bottomSheet = MoreOptionsBottomSheet.newInstance(isShared, healthScore)
            bottomSheet.show(childFragmentManager, "MoreOptionsBottomSheet")
        }

        binding.btnFixResult.setOnClickListener {
            findNavController().navigate(R.id.fixResultFragment)
        }

        binding.ingredientsHeader.setOnClickListener {
            toggleIngredients()
        }

        binding.imgExpandCollapse.setOnClickListener {
            toggleIngredients()
        }
    }

    private fun toggleIngredients() {
        isIngredientsExpanded = !isIngredientsExpanded
        binding.ingredientsContent.visibility = if (isIngredientsExpanded) View.VISIBLE else View.GONE
        binding.imgExpandCollapse.rotation = if (isIngredientsExpanded) 180f else 0f
    }

    // Recommendation methods
    private fun showRecommendationLoadingState() {
        binding.recommendationCardView.showLoading()
        startRecommendationLoadingMessages()
    }

    private fun showRecommendationError(errorMessage: String) {
        binding.recommendationCardView.showError(errorMessage)
    }

    private fun showRecommendationPremiumRequired() {
        // Reset recommendation card to initial state
        binding.recommendationCardView.resetToInitialState()
        
        // Show premium required dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.premium_required))
            .setMessage(getString(R.string.you_ve_reached_today_s_limit_unlock_unlimited_access_and_exclusive_features_by_upgrading_to_premium))
            .setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.upgrade)) { _, _ ->
                revenueCatManager.triggerPaywall()
            }
            .show()
    }

    private fun showRecommendationContent(recommendation: Recommendation) {
        binding.recommendationCardView.showRecommendations(recommendation)
    }

    private fun startRecommendationLoadingMessages() {
        loadingMessageIndex = 0
        
        recommendationPollingHandler = Handler(Looper.getMainLooper())
        recommendationPollingRunnable = object : Runnable {
            override fun run() {
                loadingMessageIndex = (loadingMessageIndex + 1) % recommendationLoadingMessages.size
                recommendationPollingHandler?.postDelayed(this, 2000)
            }
        }
        recommendationPollingRunnable?.let {
            recommendationPollingHandler?.postDelayed(it, 2000)
        }
    }

    private fun startPollingRecommendation() {
        recommendationPollingHandler?.let { handler ->
            handler.removeCallbacksAndMessages(null)
        }
        recommendationPollingHandler = Handler(Looper.getMainLooper())
        recommendationPollingRunnable = object : Runnable {
            override fun run() {
                sharedViewModel.getRecommendationdata()
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
            Snackbar.make(
                binding.root,
                getString(R.string.failed_to_share_screenshot),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareOnCommunity() {
        sharedViewModel.foodDetail.value?.id?.let { imageId ->
            UserSession.user?.id?.let { userId ->
                binding.progressOverlay.isVisible = true
                sharedViewModel.createCommunityPost(imageId)
            }
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

    override fun onShareSelected() {
        shareScreenshot()
    }

    override fun onShareCommunitySelected() {
        val canShareToCommunity = !isShared && (sharedViewModel.foodDetail.value?.healthScore?.toDouble() ?: 0.0) > 7.0
        
        if (canShareToCommunity) {
            shareOnCommunity()
        } else if (isShared) {
            Snackbar.make(binding.root, R.string.already_shared_to_community, Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(binding.root, R.string.need_high_health_score_to_share, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDeleteSelected() {
        showDeleteConfirmationDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPollingRecommendation()
    }
} 