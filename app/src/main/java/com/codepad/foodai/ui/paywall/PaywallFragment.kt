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
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentPaywallBinding
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.ui.core.BaseFragment
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaywallFragment : BaseFragment<FragmentPaywallBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var cardTitles: List<String>
    private lateinit var cardDescriptions: List<String>
    private var selectedCard: MaterialCardView? = null

    private val cardAnimations = listOf(R.raw.confetti, R.raw.scan_text, R.raw.decrease_graph)
    private var currentIndex = 0

    private val viewModel: PaywallViewModel by activityViewModels()

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    override fun getLayoutId(): Int = R.layout.fragment_paywall

    override fun onReadyView() {
        binding.viewModel = viewModel
        setupCardContent()
        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnimationsAndHandlers()
        viewModel.logPaywallClosed()
        revenueCatManager.resetPaywallTrigger()
    }

    private fun setupCardContent() {
        cardTitles = listOf(
            getString(R.string.start_now),
            getString(R.string.ai_solutions),
            getString(R.string.track_progress)
        )
        cardDescriptions = listOf(
            getString(R.string.start_now_by_unlimited_access),
            getString(R.string.scan_food_and_get_the_macro_micro_details_of_your_food),
            getString(R.string.on_analytics_section)
        )
    }

    private fun setupViews() {
        binding.txtRestorePurchases.paintFlags =
            binding.txtRestorePurchases.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        setupCardView()
        startCardAnimationLoop()
    }

    private fun setupClickListeners() {
        binding.imgClose.setOnClickListener {
            revenueCatManager.resetPaywallTrigger()
            findNavController().popBackStack()
        }

        binding.cardOption1.setOnClickListener {
            onCardSelected(binding.cardOption1)
            viewModel.selectPackage(viewModel.firstPackage.value)
            updateCardStates(true)
        }

        binding.cardOption2.setOnClickListener {
            onCardSelected(binding.cardOption2)
            viewModel.selectPackage(viewModel.secondPackage.value)
            updateCardStates(false)
        }

        binding.buttonContinue.setOnClickListener {
            if (viewModel.selectedPackage.value == null) {
                binding.errorMessage.text = getString(R.string.select_subscription_option)
                binding.errorMessage.isVisible = true
                return@setOnClickListener
            }
            binding.errorMessage.isVisible = false
            viewModel.purchase(activity as AppCompatActivity)
        }

        binding.txtRestorePurchases.setOnClickListener {
            binding.errorMessage.isVisible = false
            viewModel.restorePurchases()
        }

        binding.txtTermsOfUse.setOnClickListener {
            openUrl("https://www.food-ai-scanner.com/terms.html")
        }

        binding.txtPrivacyPolicy.setOnClickListener {
            openUrl("https://www.food-ai-scanner.com/privacy.html")
        }

        // Select first package by default
        binding.cardOption1.performClick()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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
                handler.postDelayed(this, 3000)
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

    private fun updateCardStates(isFirstCardSelected: Boolean) {
        binding.cardOption1.isSelected = isFirstCardSelected
        binding.cardOption2.isSelected = !isFirstCardSelected
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
        viewModel.isPurchasing.observe(viewLifecycleOwner) { isPurchasing ->
            binding.progressBar.isVisible = isPurchasing
            binding.buttonContinue.isEnabled = !isPurchasing
            binding.txtRestorePurchases.isEnabled = !isPurchasing
        }

        viewModel.purchaseSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.successMessage.isVisible = true
                binding.errorMessage.isVisible = false
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            binding.errorMessage.isVisible = errorMessage != null
            binding.errorMessage.text = errorMessage
            binding.successMessage.isVisible = false
        }

        viewModel.showSuccessPopup.observe(viewLifecycleOwner) { show ->
            if (show) {
                PurchaseSuccessPopup(requireContext()) {
                    viewModel.dismissSuccessPopup()
                    findNavController().popBackStack()
                }.show()
            }
        }

        viewModel.showSpecialPaywall.observe(viewLifecycleOwner) { showSpecial ->
            // Update UI for special paywall if needed
        }

        viewModel.selectedPackage.observe(viewLifecycleOwner) { selectedPackage ->
            binding.buttonContinue.isEnabled = selectedPackage != null
        }
    }
}