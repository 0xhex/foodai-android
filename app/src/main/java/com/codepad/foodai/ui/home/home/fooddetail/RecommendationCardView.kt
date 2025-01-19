package com.codepad.foodai.ui.home.home.fooddetail

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.recommendation.Recommendation
import com.google.android.material.button.MaterialButton

class RecommendationCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val initialView: LinearLayout
    private val loadingView: LinearLayout
    private val recommendationsReadyView: LinearLayout
    private val errorView: LinearLayout
    private val heartIcon: ImageView
    private val healthScoreText: TextView
    private val loadingMessage: TextView
    private val recommendationsContainer: LinearLayout
    private val whatToChangeText: TextView
    private val cautionText: TextView
    private val errorMessage: TextView
    private val getRecommendationsButton: MaterialButton
    private val tryAgainButton: MaterialButton

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

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_recommendation_card, this, true)

        initialView = findViewById(R.id.initialView)
        loadingView = findViewById(R.id.loadingView)
        recommendationsReadyView = findViewById(R.id.recommendationsReadyView)
        errorView = findViewById(R.id.errorView)
        heartIcon = findViewById(R.id.heartIcon)
        healthScoreText = findViewById(R.id.healthScoreText)
        loadingMessage = findViewById(R.id.loadingMessage)
        recommendationsContainer = findViewById(R.id.recommendationsContainer)
        whatToChangeText = findViewById(R.id.whatToChangeText)
        cautionText = findViewById(R.id.cautionText)
        errorMessage = findViewById(R.id.errorMessage)
        getRecommendationsButton = findViewById(R.id.getRecommendationsButton)
        tryAgainButton = findViewById(R.id.tryAgainButton)

        setupClickListeners()
        startButtonAnimation()
    }

    private fun setupClickListeners() {
        getRecommendationsButton.setOnClickListener {
            showLoading()
            onGetRecommendationsClick?.invoke()
        }

        tryAgainButton.setOnClickListener {
            showLoading()
            onTryAgainClick?.invoke()
        }
    }

    fun setHealthScore(score: Double) {
        healthScoreText.text = context.getString(R.string.health_score_with_param, score.toString())
        val color = when {
            score <= 3 -> R.color.red
            score <= 6 -> R.color.yellow
            else -> R.color.green
        }
        heartIcon.setColorFilter(ContextCompat.getColor(context, color))
    }

    fun showLoading() {
        initialView.visibility = View.GONE
        recommendationsReadyView.visibility = View.GONE
        errorView.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
        startLoadingMessages()
    }

    fun showError(message: String) {
        stopLoadingMessages()
        initialView.visibility = View.GONE
        recommendationsReadyView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        errorMessage.text = message
    }

    fun showRecommendations(recommendation: Recommendation) {
        stopLoadingMessages()
        initialView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        recommendationsReadyView.visibility = View.VISIBLE

        // Clear previous recommendations
        recommendationsContainer.removeAllViews()

        // Add recommendations
        recommendation.recommendations?.forEach { rec ->
            val recView = createRecommendationItemView(rec)
            recommendationsContainer.addView(recView)
        }

        // Set what to change text
        recommendation.whatToChange?.let {
            whatToChangeText.text = it
        }

        // Set caution text
        recommendation.whatToBeCarefulAbout?.let {
            cautionText.text = it
        }
    }

    private fun createRecommendationItemView(recommendation: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, null)
        view.findViewById<TextView>(R.id.recommendationText).text = recommendation
        return view
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
        loadingMessage.text = loadingMessages[currentMessageIndex]
    }

    private fun startButtonAnimation() {
        getRecommendationsButton.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(1500)
            .withEndAction {
                getRecommendationsButton.animate()
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
        getRecommendationsButton.clearAnimation()
    }
} 