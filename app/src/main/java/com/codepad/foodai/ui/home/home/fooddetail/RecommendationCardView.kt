package com.codepad.foodai.ui.home.home.fooddetail

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.recommendation.Recommendation
import com.google.android.material.button.MaterialButton

class RecommendationCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Views for different states
    private var initialView: View
    private var loadingView: View
    private var errorView: View
    private var premiumView: View
    private var contentView: View
    
    // Content views
    private var recommendationsList: RecyclerView
    private var changeSection: ConstraintLayout
    private var cautionSection: ConstraintLayout
    private var txtWhatToChange: TextView
    private var txtWhatToBeCareful: TextView
    private var txtRecommendationLoading: TextView
    private var txtRecommendationError: TextView
    
    // Buttons
    private var btnGetRecommendations: MaterialButton
    private var btnRecommendationTryAgain: MaterialButton
    private var btnRecommendationUpgrade: MaterialButton

    private val loadingMessages = listOf(
        "Analyzing your meal...",
        "Fetching personalized insights...",
        "Finalizing your unique recommendations..."
    )
    private var currentMessageIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var messageRunnable: Runnable? = null

    var onGetRecommendationsClick: (() -> Unit)? = null
    var onTryAgainClick: (() -> Unit)? = null
    var onUpgradeClick: (() -> Unit)? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_recommendation_card, this, true)
        
        // Initialize all views
        initialView = view.findViewById(R.id.recommendationInitialView)
        loadingView = view.findViewById(R.id.recommendationLoadingView)
        errorView = view.findViewById(R.id.recommendationErrorView)
        premiumView = view.findViewById(R.id.recommendationPremiumView)
        contentView = view.findViewById(R.id.recommendationContentView)
        
        recommendationsList = view.findViewById(R.id.rvRecommendations)
        changeSection = view.findViewById(R.id.changeSection)
        cautionSection = view.findViewById(R.id.cautionSection)
        txtWhatToChange = view.findViewById(R.id.txtWhatToChange)
        txtWhatToBeCareful = view.findViewById(R.id.txtWhatToBeCareful)
        txtRecommendationLoading = view.findViewById(R.id.txtRecommendationLoading)
        txtRecommendationError = view.findViewById(R.id.txtRecommendationError)
        
        btnGetRecommendations = view.findViewById(R.id.btnGetRecommendations)
        btnRecommendationTryAgain = view.findViewById(R.id.btnRecommendationTryAgain)
        btnRecommendationUpgrade = view.findViewById(R.id.btnRecommendationUpgrade)
        
        // Set initial state
        showInitialView()

        setupClickListeners()
        startButtonAnimation()
    }
    
    private fun setupClickListeners() {
        btnGetRecommendations.setOnClickListener {
            showLoading()
            onGetRecommendationsClick?.invoke()
        }

        btnRecommendationTryAgain.setOnClickListener {
            showLoading()
            onTryAgainClick?.invoke()
        }

        btnRecommendationUpgrade.setOnClickListener {
            showPremium()
            onUpgradeClick?.invoke()
        }
    }

    fun setHealthScore(score: Double) {
        // Implementation of setHealthScore method
    }

    fun showInitialView() {
        initialView.visibility = View.VISIBLE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumView.visibility = View.GONE
        contentView.visibility = View.GONE
    }
    
    fun showLoading() {
        initialView.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        premiumView.visibility = View.GONE
        contentView.visibility = View.GONE
        startLoadingMessages()
    }
    
    fun showError(message: String) {
        stopLoadingMessages()
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        premiumView.visibility = View.GONE
        contentView.visibility = View.GONE
        txtRecommendationError.text = message
    }
    
    fun showPremium() {
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
    }
    
    fun showRecommendations(recommendation: Recommendation) {
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumView.visibility = View.GONE
        contentView.visibility = View.VISIBLE
        
        // Set up recommendations list
        val recommendationsList = recommendation.recommendations ?: emptyList()
        this.recommendationsList.layoutManager = LinearLayoutManager(context)
        this.recommendationsList.adapter = RecommendationsAdapter(recommendationsList)
        
        // "What to change" section
        recommendation.whatToChange?.let { whatToChange ->
            changeSection.visibility = View.VISIBLE
            txtWhatToChange.text = whatToChange
        } ?: run {
            changeSection.visibility = View.GONE
        }
        
        // "What to be careful about" section
        recommendation.whatToBeCarefulAbout?.let { whatToBeCareful ->
            cautionSection.visibility = View.VISIBLE
            txtWhatToBeCareful.text = whatToBeCareful
        } ?: run {
            cautionSection.visibility = View.GONE
        }
    }
    
    fun resetToInitialState() {
        stopLoadingMessages()
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumView.visibility = View.GONE
        initialView.visibility = View.VISIBLE
        startButtonAnimation()
    }

    private fun startLoadingMessages() {
        currentMessageIndex = 0
        updateLoadingMessage()

        messageRunnable = object : Runnable {
            override fun run() {
                if (currentMessageIndex < loadingMessages.size - 1) {
                    currentMessageIndex++
                    updateLoadingMessage()
                    handler.postDelayed(this, 2000)
                }
            }
        }
        messageRunnable?.let { handler.postDelayed(it, 2000) }
    }

    private fun stopLoadingMessages() {
        messageRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun updateLoadingMessage() {
        txtRecommendationLoading.text = loadingMessages[currentMessageIndex]
    }

    private fun startButtonAnimation() {
        btnGetRecommendations.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(1500)
            .withEndAction {
                btnGetRecommendations.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1500)
                    .withEndAction { startButtonAnimation() }
                    .start()
            }
            .start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoadingMessages()
        btnGetRecommendations.clearAnimation()
    }
} 