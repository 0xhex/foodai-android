package com.codepad.foodai.ui.paywall

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentPaywallBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaywallFragment : BaseFragment<FragmentPaywallBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var cardTitles: List<String>
    private lateinit var cardDescriptions: List<String>
    private var selectedCard: MaterialCardView? = null

    private val cardAnimations = listOf(R.raw.confetti, R.raw.scan_text, R.raw.scan_text)
    private var currentIndex = 0

    private val viewModel: PaywallViewModel by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fragment_paywall

    override fun onReadyView() {
        binding.viewModel = viewModel
        cardTitles = listOf(
            getString(R.string.start_now),
            getString(R.string.ai_solutions), getString(R.string.track_progress)
        )
        cardDescriptions = listOf(
            getString(R.string.start_now_by_unlimited_access),
            getString(R.string.scan_food_and_get_the_macro_micro_details_of_your_food),
            getString(R.string.on_analytics_section)
        )

        setupCardView()
        startCardAnimationLoop()
        binding.txtRestorePurchases.paintFlags =
            binding.txtRestorePurchases.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.imgClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardOption1.setOnClickListener {
            onCardSelected(binding.cardOption1)
            viewModel.selectPackage(viewModel.firstPackage.value)
        }
        binding.cardOption2.setOnClickListener {
            onCardSelected(binding.cardOption2)
            viewModel.selectPackage(viewModel.secondPackage.value)
        }
        binding.cardOption1.performClick()

        binding.buttonContinue.setOnClickListener {
            viewModel.purchase(activity as AppCompatActivity)
        }

        binding.txtRestorePurchases.setOnClickListener {
            viewModel.restorePurchases()
        }

        binding.txtTermsOfUse.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.quicknotes-ai.com/terms.html"))
            startActivity(intent)
        }

        binding.txtPrivacyPolicy.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.quicknotes-ai.com/privacy.html"))
            startActivity(intent)
        }

        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnimationsAndHandlers()
    }

    private fun setupCardView() {
        binding.paywallCardView.setTitle(cardTitles[currentIndex])
        binding.paywallCardView.setDescription(cardDescriptions[currentIndex])
        binding.paywallCardView.setAnimation(cardAnimations[currentIndex])
    }

    private fun animateCard() {
        val cardView = binding.paywallCardView
        val screenWidth = resources.displayMetrics.widthPixels
        val nextIndex = (currentIndex + 1) % cardTitles.size

        cardView.animate()
            .translationX(screenWidth.toFloat())
            .setDuration(300)
            .withEndAction {
                cardView.translationX = screenWidth.toFloat()
                cardView.alpha = 0f
                cardView.setTitle(cardTitles[nextIndex])
                cardView.setDescription(cardDescriptions[nextIndex])
                cardView.setAnimation(cardAnimations[nextIndex])
                cardView.isVisible = true

                cardView.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }
            .start()

        currentIndex = nextIndex
    }

    private fun startCardAnimationLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                animateCard()
                handler.postDelayed(this, 3000) // Adjust delay for animation loop
            }
        }, 3000)
    }

    private fun stopAnimationsAndHandlers() {
        handler.removeCallbacksAndMessages(null)
        binding.paywallCardView.animate().cancel()
    }

    private fun onCardSelected(card: MaterialCardView) {
        selectedCard?.let { animateStrokeWidth(it, 2f, 0f) }
        animateStrokeWidth(card, 0f, 2f)
        selectedCard = card
    }

    private fun animateStrokeWidth(card: MaterialCardView, from: Float, to: Float) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = 1200
            addUpdateListener { animation ->
                card.strokeWidth = (animation.animatedValue as Float).dpToPx()
            }
            start()
        }
    }

    private fun Float.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isPurchasing.collectLatest { isPurchasing ->
                binding.progressBar.isVisible = isPurchasing
            }
        }

        lifecycleScope.launch {
            viewModel.purchaseSuccess.collectLatest { success ->
                if (success) {
                    binding.successMessage.isVisible = true
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                errorMessage?.let {
                    binding.errorMessage.text = it
                    binding.errorMessage.isVisible = true
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedPackage.collectLatest { selectedPackage ->
                selectedPackage?.let {
                    // Update the selected card UI
                }
            }
        }

        lifecycleScope.launch {
            viewModel.showSuccessPopup.collectLatest { show ->
                if (show) {
                    PurchaseSuccessPopup(requireContext()) {
                        findNavController().popBackStack()
                    }.show()
                }
            }
        }
    }
}