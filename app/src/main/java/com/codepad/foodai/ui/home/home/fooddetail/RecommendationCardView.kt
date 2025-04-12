package com.codepad.foodai.ui.home.home.fooddetail

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.recommendation.Recommendation
import com.google.android.material.button.MaterialButton

class RecommendationCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    // Views for different states
    private var initialView: View
    private lateinit var loadingView: View
    private var errorView: View
    private var premiumView: View
    private var contentView: View
    private var glowEffect: View

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
    private var loadingAnimation: LottieAnimationView

    private val loadingMessages = listOf(
        context.getString(R.string.analyzing_your_meal),
        context.getString(R.string.fetching_personalized_insights),
        context.getString(R.string.finalizing_your_unique_recommendations)
    )
    private var currentMessageIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var messageRunnable: Runnable? = null
    private var glowAnimator: ObjectAnimator? = null

    var onGetRecommendationsClick: (() -> Unit)? = null
    var onTryAgainClick: (() -> Unit)? = null
    var onUpgradeClick: (() -> Unit)? = null

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_recommendation_card, this, true)

        // Initialize all views
        initialView = view.findViewById(R.id.recommendationInitialView)
        loadingView = view.findViewById(R.id.recommendationLoadingView)
        errorView = view.findViewById(R.id.recommendationErrorView)
        premiumView = view.findViewById(R.id.recommendationPremiumView)
        contentView = view.findViewById(R.id.recommendationContentView)
        glowEffect = view.findViewById(R.id.glowEffect)

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
        loadingAnimation = view.findViewById(R.id.loadingAnimation)

        // Set initial state
        showInitialView()

        setupClickListeners()
        startGlowAnimation()
    }

    private fun setupClickListeners() {
        btnGetRecommendations.setOnClickListener {
            animateButtonClick(btnGetRecommendations) {
                showLoading()
                onGetRecommendationsClick?.invoke()
            }
        }

        btnRecommendationTryAgain.setOnClickListener {
            animateButtonClick(btnRecommendationTryAgain) {
                showLoading()
                onTryAgainClick?.invoke()
            }
        }

        btnRecommendationUpgrade.setOnClickListener {
            animateButtonClick(btnRecommendationUpgrade) {
                onUpgradeClick?.invoke()
            }
        }
    }

    private fun animateButtonClick(button: MaterialButton, onComplete: () -> Unit) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        onComplete()
                    }
                    .start()
            }
            .start()
    }

    private fun startGlowAnimation() {
        glowAnimator?.cancel()
        glowAnimator = ObjectAnimator.ofFloat(glowEffect, "alpha", 0.3f, 0.8f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun setHealthScore(score: Double) {
        // Implementation of setHealthScore method
    }

    fun showInitialView() {
        crossFadeToView(initialView)
        startGlowAnimation()
    }

    fun showLoading() {
        crossFadeToView(loadingView)
        startLoadingMessages()
        loadingAnimation.playAnimation()
    }

    fun showError(message: String) {
        stopLoadingMessages()
        crossFadeToView(errorView)
        txtRecommendationError.text = message
    }

    fun showPremium() {
        crossFadeToView(premiumView)
    }

    fun showRecommendations(recommendation: Recommendation) {
        stopLoadingMessages()
        crossFadeToView(contentView)

        // Set up recommendations list with animation
        val recommendationsList = recommendation.recommendations ?: emptyList()
        this.recommendationsList.layoutManager = LinearLayoutManager(context)
        this.recommendationsList.adapter = RecommendationsAdapter(recommendationsList)

        // Animate sections
        recommendation.whatToChange?.let { whatToChange ->
            changeSection.alpha = 0f
            changeSection.isVisible = true
            txtWhatToChange.text = whatToChange
            changeSection.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(200)
                .start()
        } ?: run {
            changeSection.isVisible = false
        }

        recommendation.whatToBeCarefulAbout?.let { whatToBeCareful ->
            cautionSection.alpha = 0f
            cautionSection.isVisible = true
            txtWhatToBeCareful.text = whatToBeCareful
            cautionSection.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(400)
                .start()
        } ?: run {
            cautionSection.isVisible = false
        }
    }

    fun resetToInitialState() {
        stopLoadingMessages()
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        premiumView.visibility = View.GONE
        initialView.visibility = View.VISIBLE
        startGlowAnimation()
    }

    private fun crossFadeToView(targetView: View) {
        val views = listOf(initialView, loadingView, errorView, premiumView, contentView)

        views.forEach { view ->
            if (view == targetView) {
                view.alpha = 0f
                view.visibility = View.VISIBLE
                view.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            } else {
                if (view.isVisible) {
                    view.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            view.visibility = View.GONE
                        }
                        .start()
                }
            }
        }
    }

    private fun startLoadingMessages() {
        currentMessageIndex = 0
        updateLoadingMessage()

        messageRunnable?.let { handler.removeCallbacks(it) }
        messageRunnable = object : Runnable {
            override fun run() {
                currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
                updateLoadingMessage()
                handler.postDelayed(this, 2000)
            }
        }
        messageRunnable?.let { handler.postDelayed(it, 2000) }
    }

    private fun updateLoadingMessage() {
        txtRecommendationLoading.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                txtRecommendationLoading.text = loadingMessages[currentMessageIndex]
                txtRecommendationLoading.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun stopLoadingMessages() {
        messageRunnable?.let { handler.removeCallbacks(it) }
        messageRunnable = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoadingMessages()
        glowAnimator?.cancel()
    }
} 