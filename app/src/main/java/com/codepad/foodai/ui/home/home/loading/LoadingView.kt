package com.codepad.foodai.ui.home.home.loading

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.ViewLoadingBinding
import com.codepad.foodai.ui.user_property.loading.LoadingType

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewLoadingBinding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)
    private var index: Int = 0
    private var settingUpItems: List<String> = emptyList()
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var loadingType: LoadingType

    init {
        setupView()
    }

    private fun setupView() {
        binding.progressBar.max = 100
    }

    fun setLoadingType(loadingType: LoadingType) {
        this.loadingType = loadingType
        setupLoadingItems()
        startLoading()
    }

    fun stopLoading() {
        handler.removeCallbacksAndMessages(null)
        binding.lottieAnimationView.cancelAnimation()
        visibility = View.GONE
    }

    private val animationName: Int
        get() = when (loadingType) {
            LoadingType.UPLOAD_FILE -> R.raw.upload_file
            LoadingType.USER_CUSTOMIZATION -> R.raw.user
            LoadingType.EDITING_FOOD -> R.raw.food_calorie
            LoadingType.LOADING_DEFAULT -> R.raw.loading
        }

    private fun setupLoadingItems() {
        settingUpItems = when (loadingType) {
            LoadingType.UPLOAD_FILE -> listOf("Uploading...", "Processing...", "Finalizing...")
            LoadingType.USER_CUSTOMIZATION -> listOf("Estimating your metabolic age...", "Processing diet type", "Applying BMR formula...", "Finalizing results...")
            LoadingType.EDITING_FOOD -> listOf("Analyzing food...", "Calculating nutrients...", "Updating database...", "Finalizing...")
            LoadingType.LOADING_DEFAULT -> listOf("3%", "35%", "50%", "65%", "Finalizing...")
        }
        binding.textView.text = displayText
        binding.lottieAnimationView.setAnimation(animationName)
        binding.lottieAnimationView.playAnimation()
    }

    private val displayText: String
        get() = when (loadingType) {
            LoadingType.UPLOAD_FILE -> "Uploading Image..."
            LoadingType.USER_CUSTOMIZATION -> "We're setting everything up for you"
            LoadingType.EDITING_FOOD -> "Processing your food edits..."
            LoadingType.LOADING_DEFAULT -> "Loading..."
        }

    private fun startLoading() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (index < settingUpItems.size) {
                    binding.textViewDetail.text = settingUpItems[index]
                    binding.progressBar.progress = ((index + 1) / settingUpItems.size.toFloat() * 100).toInt()
                    index++
                    handler.postDelayed(this, 1200)
                } else {
                    handler.removeCallbacks(this)
                    // Navigate to next screen or perform any other action
                }
            }
        }, 1200)
    }
}