package com.codepad.foodai.ui.paywall

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView
import com.codepad.foodai.R

class PurchaseSuccessPopup(context: Context, private val onContinue: () -> Unit) : Dialog(context) {

    init {
        setContentView(R.layout.dialog_purchase_success)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.playAnimation()

        val continueButton: Button = findViewById(R.id.continueButton)
        continueButton.setOnClickListener {
            dismiss()
            onContinue()
        }
    }
}
