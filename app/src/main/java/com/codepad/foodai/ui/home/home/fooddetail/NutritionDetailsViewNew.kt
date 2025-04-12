package com.codepad.foodai.ui.home.home.fooddetail

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.ErrorCode
import com.codepad.foodai.domain.models.nutrition.NutritionDetailsData
import com.codepad.foodai.helpers.ModelPreferencesManager
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class NutritionDetailsViewNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var initialView: LinearLayout
    private lateinit var loadingView: LinearLayout
    private lateinit var errorView: LinearLayout
    private lateinit var premiumRequiredView: LinearLayout
    private lateinit var fiberCard: ConstraintLayout
    
    private lateinit var txtFiberEmoji: TextView
    private lateinit var txtFiberAmount: TextView
    private lateinit var txtFiberPercentage: TextView
    private lateinit var txtFiberProgressValue: TextView
    private lateinit var fiberCircularProgress: CircularProgressView
    
    private lateinit var iconGlow: View
    private lateinit var txtLoadingMessage: TextView
    private lateinit var txtErrorMessage: TextView
    private lateinit var btnAnalyzeNutrition: MaterialButton
    private lateinit var btnTryAgain: MaterialButton
    private lateinit var btnUpgrade: MaterialButton
    
    private lateinit var vitaminIndicator: View
    private lateinit var mineralIndicator: View
    private lateinit var fiberIndicator: View
    
    private var viewPagerAdapter: NutritionViewPagerAdapter? = null
    private var imageId: String? = null
    private var nutritionDetails: NutritionDetailsData? = null
    private var needPremiumCallBack: (() -> Unit)? = null
    private var lifecycleOwner: LifecycleOwner? = null
    
    // ViewModel reference
    private var viewModel: HomePagerViewModel? = null
    
    private val loadingMessages = arrayOf(
        R.string.analyzing_nutritional_content,
        R.string.calculating_vitamin_mineral_levels,
        R.string.preparing_nutrition_profile
    )
    private var currentLoadingMessageIndex = 0
    private val loadingMessageHandler = Handler(Looper.getMainLooper())
    private val loadingMessageRunnable = object : Runnable {
        override fun run() {
            if (currentLoadingMessageIndex < loadingMessages.size - 1) {
                currentLoadingMessageIndex++
                txtLoadingMessage.setText(loadingMessages[currentLoadingMessageIndex])
                loadingMessageHandler.postDelayed(this, 2000)
            }
        }
    }
    
    private val pollingHandler = Handler(Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            pollNutritionDetails()
        }
    }

    private lateinit var detailsContentContainer: ConstraintLayout

    init {
        initView()
        setupListeners()
        startGlowAnimation()
        setupInitialIndicators()
    }
    
    /**
     * Connects this view to the ViewModel and lifecycleOwner
     * Must be called before using this view
     */
    fun connectToViewModel(viewModel: HomePagerViewModel, lifecycleOwner: LifecycleOwner) {
        this.viewModel = viewModel
        this.lifecycleOwner = lifecycleOwner
        
        // Set up observers for nutrition details
        viewModel.nutritionDetails.observe(lifecycleOwner) { details ->
            if (details != null) {
                nutritionDetails = details
                if (details.status == "completed") {
                    imageId?.let { id -> saveNutritionDetailsData(id, details) }
                    showDetailsView()
                } else if (details.status == "failed") {
                    showErrorView("Nutrition details analysis failed")
                }
            }
        }
        
        viewModel.nutritionDetailsError.observe(lifecycleOwner) { error ->
            error?.let {
                stopPolling()
                if (error.errorCode == ErrorCode.PREMIUM_REQUIRED.toString()) {
                    showPremiumRequiredView()
                } else {
                    showErrorView(error.message ?: "Unknown error occurred")
                }
            }
        }
    }

    private fun initView() {
        // Inflate the layout
        LayoutInflater.from(context)
            .inflate(R.layout.view_nutrition_details_new, this, true)
        
        // Reference all views
        detailsContentContainer = findViewById(R.id.detailsContentContainer)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        initialView = findViewById(R.id.initialView)
        loadingView = findViewById(R.id.loadingView)
        errorView = findViewById(R.id.errorView)
        premiumRequiredView = findViewById(R.id.premiumRequiredView)
        fiberCard = findViewById(R.id.fiberCard)
        
        txtFiberEmoji = findViewById(R.id.txtFiberEmoji)
        txtFiberAmount = findViewById(R.id.txtFiberAmount)
        txtFiberPercentage = findViewById(R.id.txtFiberPercentage)
        txtFiberProgressValue = findViewById(R.id.txtFiberProgressValue)
        fiberCircularProgress = findViewById(R.id.fiberCircularProgress)
        
        iconGlow = findViewById(R.id.iconGlow)
        txtLoadingMessage = findViewById(R.id.txtLoadingMessage)
        txtErrorMessage = findViewById(R.id.txtErrorMessage)
        btnAnalyzeNutrition = findViewById(R.id.btnAnalyzeNutrition)
        btnTryAgain = findViewById(R.id.btnTryAgain)
        btnUpgrade = findViewById(R.id.btnUpgrade)
        
        vitaminIndicator = findViewById(R.id.vitaminIndicator)
        mineralIndicator = findViewById(R.id.mineralIndicator)
        fiberIndicator = findViewById(R.id.fiberIndicator)
        
        // Set the initial state
        detailsContentContainer.visibility = View.GONE
        initialView.visibility = View.VISIBLE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumRequiredView.visibility = View.GONE
        fiberCard.visibility = View.GONE
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE
        
        // Setup ViewPager
        viewPagerAdapter = NutritionViewPagerAdapter(viewPager)
        viewPager.adapter = viewPagerAdapter
        
        // Connect TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> context.getString(R.string.vitamins)
                1 -> context.getString(R.string.minerals)
                else -> ""
            }
        }.attach()
    }
    
    private fun setupListeners() {
        btnAnalyzeNutrition.setOnClickListener {
            requestNutritionDetails()
        }
        
        btnTryAgain.setOnClickListener {
            errorView.visibility = View.GONE
            requestNutritionDetails()
        }
        
        btnUpgrade.setOnClickListener {
            needPremiumCallBack?.invoke()
        }
    }
    
    private fun setupInitialIndicators() {
        // Setup vitamin indicator
        val vitaminEmoji = vitaminIndicator.findViewById<TextView>(R.id.txtEmoji)
        val vitaminLabel = vitaminIndicator.findViewById<TextView>(R.id.txtLabel)
        vitaminEmoji.text = "💊"
        vitaminLabel.text = context.getString(R.string.vitamins)
        
        // Setup mineral indicator
        val mineralEmoji = mineralIndicator.findViewById<TextView>(R.id.txtEmoji)
        val mineralLabel = mineralIndicator.findViewById<TextView>(R.id.txtLabel)
        mineralEmoji.text = "🔬"
        mineralLabel.text = context.getString(R.string.minerals)
        
        // Setup fiber indicator
        val fiberEmoji = fiberIndicator.findViewById<TextView>(R.id.txtEmoji)
        val fiberLabel = fiberIndicator.findViewById<TextView>(R.id.txtLabel)
        fiberEmoji.text = "🥦"
        fiberLabel.text = context.getString(R.string.dietary_fiber)
    }
    
    private fun startGlowAnimation() {
        val glowAnimator = ValueAnimator.ofFloat(0.3f, 0.8f)
        glowAnimator.duration = 1500
        glowAnimator.repeatCount = ValueAnimator.INFINITE
        glowAnimator.repeatMode = ValueAnimator.REVERSE
        glowAnimator.interpolator = AccelerateDecelerateInterpolator()
        glowAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            iconGlow.alpha = value
        }
        glowAnimator.start()
    }
    
    fun setImageId(imageId: String) {
        this.imageId = imageId
        loadSavedNutritionDetails()
    }
    
    fun setOnNeedPremiumCallback(callback: () -> Unit) {
        this.needPremiumCallBack = callback
    }
    
    private fun loadSavedNutritionDetails() {
        val savedData = getNutritionDetailsData(imageId ?: return)
        if (savedData != null && savedData.status == "completed") {
            nutritionDetails = savedData
            showDetailsView()
        }
    }
    
    private fun requestNutritionDetails() {
        imageId?.let { id ->
            showLoadingView()
            
            val userId = UserSession.user?.id
            if (userId.isNullOrEmpty()) {
                showErrorView("User ID not found")
                return
            }
            
            // Use the ViewModel to request nutrition details
            viewModel?.let { vm ->
                vm.createNutritionDetails(id, userId)
                startPolling()
            } ?: showErrorView("ViewModel not set. Call connectToViewModel first.")
        } ?: run {
            showErrorView("Image ID not set")
        }
    }
    
    private fun pollNutritionDetails() {
        imageId?.let { id ->
            viewModel?.let { vm ->
                vm.getNutritionDetails(id)
                // Continue polling until the ViewModel returns a completed result
                pollingHandler.postDelayed(pollingRunnable, 3000)
            } ?: run {
                stopPolling()
                showErrorView("ViewModel not set. Call connectToViewModel first.")
            }
        }
    }
    
    private fun startPolling() {
        pollingHandler.postDelayed(pollingRunnable, 3000)
    }
    
    private fun stopPolling() {
        pollingHandler.removeCallbacks(pollingRunnable)
    }
    
    private fun showLoadingView() {
        currentLoadingMessageIndex = 0
        txtLoadingMessage.setText(loadingMessages[currentLoadingMessageIndex])
        
        detailsContentContainer.visibility = View.GONE
        initialView.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        premiumRequiredView.visibility = View.GONE
        fiberCard.visibility = View.GONE
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE
        
        // Start cycling through loading messages
        loadingMessageHandler.postDelayed(loadingMessageRunnable, 2000)
    }
    
    private fun showDetailsView() {
        loadingMessageHandler.removeCallbacks(loadingMessageRunnable)
        
        detailsContentContainer.visibility = View.VISIBLE
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumRequiredView.visibility = View.GONE
        fiberCard.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        viewPager.visibility = View.VISIBLE
        
        // Update fiber card
        nutritionDetails?.let { details ->
            txtFiberEmoji.text = details.fiberEmoji
            txtFiberAmount.text = "${details.fiber}g"
            txtFiberPercentage.text = context.getString(
                R.string.percentage_daily_value_format,
                details.fiberDailyPercentage
            )
            txtFiberPercentage.setTextColor(ContextCompat.getColor(context, R.color.green))
            fiberCircularProgress.setProgress(details.fiberDailyPercentage, ContextCompat.getColor(context, R.color.green))
            txtFiberProgressValue.text = "${details.fiberDailyPercentage}%"
            
            // Update ViewPager content
            viewPagerAdapter?.updateData(details.vitamins, details.minerals)
        }
    }
    
    private fun showErrorView(errorMessage: String) {
        loadingMessageHandler.removeCallbacks(loadingMessageRunnable)
        
        detailsContentContainer.visibility = View.GONE
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        premiumRequiredView.visibility = View.GONE
        fiberCard.visibility = View.GONE
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE
        
        txtErrorMessage.text = errorMessage
    }
    
    private fun showPremiumRequiredView() {
        loadingMessageHandler.removeCallbacks(loadingMessageRunnable)
        
        detailsContentContainer.visibility = View.GONE
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumRequiredView.visibility = View.VISIBLE
        fiberCard.visibility = View.GONE
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE
        
        // Trigger callback
        needPremiumCallBack?.invoke()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loadingMessageHandler.removeCallbacks(loadingMessageRunnable)
        stopPolling()
    }
    
    // Helper methods for storing and retrieving nutrition details data
    private fun getNutritionDetailsData(imageId: String): NutritionDetailsData? {
        val key = "nutrition_details_$imageId"
        return ModelPreferencesManager.get<NutritionDetailsData>(key)
    }
    
    private fun saveNutritionDetailsData(imageId: String, data: NutritionDetailsData) {
        val key = "nutrition_details_$imageId"
        ModelPreferencesManager.put(data, key)
    }
} 