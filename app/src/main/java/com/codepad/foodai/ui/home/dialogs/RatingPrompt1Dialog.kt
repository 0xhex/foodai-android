package com.codepad.foodai.ui.home.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import com.codepad.foodai.R
import com.codepad.foodai.databinding.ViewRatingPrompt1Binding
import com.codepad.foodai.ui.core.BaseDialogFragment

class RatingPrompt1Dialog : BaseDialogFragment<ViewRatingPrompt1Binding>() {
    var onYesClicked: (() -> Unit)? = null
    var onNoClicked: (() -> Unit)? = null

    override fun getTheme() = R.style.RoundedCornersDialog
    override val layoutResourcesId = R.layout.view_rating_prompt_1

    override fun onInitView() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners
        binding.btnYes.setOnClickListener {
            onYesClicked?.invoke()
            animateAndDismiss()
        }

        binding.btnNo.setOnClickListener {
            onNoClicked?.invoke()
            animateAndDismiss()
        }

        // Background click dismisses with "No" action
        binding.root.setOnClickListener {
            onNoClicked?.invoke()
            animateAndDismiss()
        }

        // Prevent card clicks from dismissing
        binding.cardView.setOnClickListener { }

        // Initial state
        binding.root.alpha = 0f
        binding.cardView.scaleX = 0.9f
        binding.cardView.scaleY = 0.9f

        // Animate in
        binding.root.animate()
            .alpha(1f)
            .setDuration(250)
            .start()

        binding.cardView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(250)
            .start()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun animateAndDismiss() {
        binding.root.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                dismiss()
            }
            .start()

        binding.cardView.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(200)
            .start()
    }
} 