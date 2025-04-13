package com.codepad.foodai.ui.paywall

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R

class PurchaseSuccessDialog(
    context: Context,
    private val onDismiss: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_purchase_success)

        // Make dialog background transparent
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Start confetti animation
        findViewById<LottieAnimationView>(R.id.confettiAnimation).apply {
            setAnimation(R.raw.confetti)
            playAnimation()
        }

        // Set up close button
        findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
            onDismiss()
        }

        // Set up continue button
        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            dismiss()
            onDismiss()
        }

        // Make dialog non-cancelable
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
} 