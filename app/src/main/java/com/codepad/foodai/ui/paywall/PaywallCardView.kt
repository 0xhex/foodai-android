package com.codepad.foodai.ui.paywall

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.codepad.foodai.databinding.ViewPaywallCardBinding

class PaywallCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewPaywallCardBinding = ViewPaywallCardBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setTitle(title: String) {
        binding.txtTitle.text = title
    }

    fun setDescription(description: String) {
        binding.txtDescription.text = description
    }

    fun setAnimation(animationRes: Int) {
        binding.lottieAnimation.setAnimation(animationRes)
    }

    fun cancelAnimations() {
        binding.lottieAnimation.cancelAnimation()
    }
}