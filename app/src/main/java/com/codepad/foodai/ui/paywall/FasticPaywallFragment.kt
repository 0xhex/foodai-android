package com.codepad.foodai.ui.paywall

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentFasticPaywallBinding
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.ui.core.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.Period
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FasticPaywallFragment : BaseFragment<FragmentFasticPaywallBinding>() {

    companion object {
        private const val TAG = "FasticPaywallFrag"
    }

    private val viewModel: FasticPaywallViewModel by viewModels()

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    private var lastPackages: List<Package>? = null

    override fun getLayoutId(): Int = R.layout.fragment_fastic_paywall

    override fun onReadyView() {
        android.util.Log.d(TAG, "onReadyView called")
        binding.viewModel = viewModel
        setupViews()
        setupClickListeners()
        observeViewModel()

        viewModel.logPaywallViewed()
    }

    private fun setupViews() {
        android.util.Log.d(TAG, "Setting up views")
        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                android.util.Log.d(TAG, "Starting offerings collection")
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    revenueCatManager.offerings.collect { offerings ->
                        android.util.Log.d(TAG, "Offerings received: ${offerings != null}")
                        
                        if (offerings == null) {
                            android.util.Log.w(TAG, "Offerings is null")
                            binding.errorText.apply {
                                text = getString(R.string.error)
                                isVisible = true
                            }
                            return@collect
                        }
                        
                        val packages = offerings.current?.availablePackages
                        android.util.Log.d(TAG, "Available packages: ${packages?.size}")
                        
                        if (packages.isNullOrEmpty()) {
                            android.util.Log.w(TAG, "No available packages")
                            binding.errorText.apply {
                                text = getString(R.string.error)
                                isVisible = true
                            }
                            return@collect
                        }
                        
                        if (packages == lastPackages) {
                            android.util.Log.d(TAG, "Packages unchanged, skipping update")
                            return@collect
                        }
                        
                        lastPackages = packages
                        val sortedPackages = viewModel?.sortPackages(packages)
                        // Hide any previous error
                        binding.errorText.isVisible = false
                        
                        // Update UI with whatever packages we have
                        if (sortedPackages?.isNotEmpty() == true) {
                            updateSubscriptionOptions(sortedPackages)
                        }
                    }
                }
            }

            setupFeatureRow(
                feature1,
                R.drawable.ic_check_circle,
                getString(R.string.a_plan_just_for_you),
                getString(R.string.stay_on_track_with_nutrition)
            )

            setupFeatureRow(
                feature2,
                R.drawable.ic_check_circle,
                getString(R.string.always_motivated),
                getString(R.string.feel_rewarded_everyday)
            )
        }
    }

    private fun arePackagesEqual(packages1: List<Package>?, packages2: List<Package>?): Boolean {
        if (packages1 == null || packages2 == null) return packages1 === packages2
        if (packages1.size != packages2.size) return false
        
        return packages1.zip(packages2).all { (p1, p2) ->
            p1.identifier == p2.identifier && 
            p1.product.price.amountMicros == p2.product.price.amountMicros
        }
    }

    private fun updateSubscriptionOptions(packages: List<Package>) {
        android.util.Log.d(TAG, "Updating subscription options with ${packages.size} packages")
        
        val options = listOf(binding.yearlyOption, binding.monthlyOption, binding.weeklyOption)
        
        // First, hide all options
        options.forEach { it.isVisible = false }
        
        // Then show and update only the available packages
        for (i in packages.indices) {
            try {
                val pkg = packages[i]
                val optionView = options.getOrNull(i) ?: continue
                
                optionView.isVisible = true
                android.util.Log.d(TAG, "Updating option $i with package ${pkg.identifier}")

                // Update tag text (Best Value, Most Popular, etc.)
                optionView.findViewById<TextView>(R.id.tagText)?.apply {
                    text = when (i) {
                        0 -> getString(R.string.best_value)
                        1 -> getString(R.string.most_popular)
                        else -> null
                    }
                    isVisible = text != null
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            when (i) {
                                0 -> R.color.custom_green
                                else -> R.color.orange
                            }
                        )
                    )
                }

                // Update period text
                optionView.findViewById<TextView>(R.id.periodText)?.apply {
                    text = when (pkg.product.period?.unit) {
                        Period.Unit.YEAR -> getString(R.string.twelve_months)
                        Period.Unit.MONTH -> getString(R.string.one_month)
                        Period.Unit.WEEK -> getString(R.string.one_week)
                        else -> getString(R.string.one_month)
                    }
                }

                // Update price text
                optionView.findViewById<TextView>(R.id.priceText)?.apply {
                    text = viewModel.calculateWeeklyPrice(pkg)
                }

                // Update savings text if applicable
                optionView.findViewById<TextView>(R.id.savingsText)?.apply {
                    viewModel.calculateSavings(pkg)?.let { savings ->
                        text = getString(R.string.save_format, savings)
                        isVisible = true
                    } ?: run {
                        isVisible = false
                    }
                }

                // Update selection indicator
                val isSelected = i == viewModel.selectedPackageIndex.value
                optionView.isSelected = isSelected
                optionView.findViewById<View>(R.id.selectionDot)?.isVisible = isSelected

                // Make the item clickable
                optionView.setOnClickListener {
                    viewModel.selectPackage(i)
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error updating option $i", e)
            }
        }
        
        // Update the subscription message based on the selected package
        updateSubscriptionMessage()
        
        android.util.Log.d(TAG, "Finished updating subscription options")
    }

    private fun updateSubscriptionMessage() {
        val selectedIndex = viewModel.selectedPackageIndex.value ?: return
        val selectedPackage = viewModel.selectedPackage.value ?: return
        
        binding.txtSubscriptionMessage.text = when (selectedPackage.product.period?.unit) {
            Period.Unit.YEAR -> {
                val messages = arrayOf(
                    getString(R.string.year_message_1),
                    getString(R.string.year_message_2),
                    getString(R.string.year_message_3)
                )
                messages.random()
            }
            Period.Unit.MONTH -> {
                val messages = arrayOf(
                    getString(R.string.monthly_message_1),
                    getString(R.string.monthly_message_2),
                    getString(R.string.monthly_message_3)
                )
                messages.random()
            }
            Period.Unit.WEEK -> {
                val messages = arrayOf(
                    getString(R.string.weekly_message_1),
                    getString(R.string.weekly_message_2),
                    getString(R.string.weekly_message_3)
                )
                messages.random()
            }
            else -> getString(R.string.monthly_message_1)
        }
    }

    private fun updateSelectedPackage(index: Int) {
        val options = listOf(binding.yearlyOption, binding.monthlyOption, binding.weeklyOption)
        
        options.forEachIndexed { i, view ->
            val isSelected = i == index
            view.isSelected = isSelected
            view.findViewById<View>(R.id.selectionDot)?.isVisible = isSelected
        }
        
        // Update subscription message when selection changes
        updateSubscriptionMessage()
    }

    private fun setupFeatureRow(
        view: View,
        iconResId: Int,
        title: String,
        description: String
    ) {
        view.apply {
            findViewById<ImageView>(R.id.icon)?.setImageResource(iconResId)
            findViewById<TextView>(R.id.title)?.text = title
            findViewById<TextView>(R.id.description)?.text = description
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnClose.setOnClickListener {
                findNavController().popBackStack()
            }

            btnContinue.setOnClickListener {
                val selectedPackage = viewModel?.selectedPackage?.value
                val selectedIndex = viewModel?.selectedPackageIndex?.value ?: 0
                
                if (selectedPackage == null) {
                    viewModel?.setErrorMessage(getString(R.string.select_subscription_option))
                    return@setOnClickListener
                }
                
                // Check if this is a weekly package
                val isWeeklyPackage = selectedPackage.product.period?.unit == Period.Unit.WEEK
                
                if (isWeeklyPackage) {
                    // For weekly packages, proceed directly to purchase
                    (requireActivity() as? AppCompatActivity)?.let { activity ->
                        viewModel?.purchase(activity)
                    }
                } else {
                    // For yearly and monthly packages, show confirmation sheet
                    showPurchaseConfirmationSheet()
                }
            }

            btnRestorePurchases.setOnClickListener {
                viewModel?.restorePurchases()
            }

            txtTerms.setOnClickListener {
                openUrl("https://www.food-ai-scanner.com/terms.html")
            }

            txtPrivacy.setOnClickListener {
                openUrl("https://www.food-ai-scanner.com/privacy.html")
            }

            // Set up subscription option clicks
            yearlyOption.setOnClickListener { handlePackageSelection(0) }
            monthlyOption.setOnClickListener { handlePackageSelection(1) }
            weeklyOption.setOnClickListener { handlePackageSelection(2) }
        }
    }

    private fun showPurchaseConfirmationSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.sheet_purchase_confirmation, null)
        dialog.setContentView(sheetView)

        // Set up sheet content
        sheetView.apply {
            // Populate package details
            viewModel.selectedPackage.value?.let { pkg ->
                findViewById<TextView>(R.id.txtPeriod)?.text = getPeriodText(pkg)
                findViewById<TextView>(R.id.txtWeeklyPrice)?.text = viewModel.calculateWeeklyPrice(pkg)
                findViewById<TextView>(R.id.txtTotalPrice)?.text = pkg.product.price.formatted

                // Show savings if applicable
                viewModel.calculateSavings(pkg)?.let { savings ->
                    findViewById<TextView>(R.id.txtSavings)?.apply {
                        text = getString(R.string.you_save_format, savings)
                        isVisible = true
                    }
                }
            }

            findViewById<View>(R.id.btnContinue)?.setOnClickListener {
                dialog.dismiss()
                (requireActivity() as? AppCompatActivity)?.let { activity ->
                    viewModel.purchase(activity)
                }
            }

            // Set up terms and privacy links
            findViewById<TextView>(R.id.txtTerms)?.setOnClickListener {
                openUrl("https://www.food-ai-scanner.com/terms.html")
            }

            findViewById<TextView>(R.id.txtPrivacy)?.setOnClickListener {
                openUrl("https://www.food-ai-scanner.com/privacy.html")
            }
        }

        dialog.show()
    }

    private fun getPeriodText(pkg: Package): String {
        return when (pkg.product.period?.unit) {
            Period.Unit.YEAR -> getString(R.string.twelve_months)
            Period.Unit.MONTH -> getString(R.string.one_month)
            Period.Unit.WEEK -> getString(R.string.one_week)
            else -> getString(R.string.one_month)
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun observeViewModel() {
        viewModel.apply {
            isPurchasing.observe(viewLifecycleOwner) { isPurchasing ->
                binding.progressOverlay.isVisible = isPurchasing
                binding.btnContinue.isEnabled = !isPurchasing
                binding.btnRestorePurchases.isEnabled = !isPurchasing
            }

            sortedPackages.observe(viewLifecycleOwner) { packages ->
                android.util.Log.d(TAG, "Received sorted packages update: ${packages.size}")
                if (packages.isNotEmpty()) {
                    updateSubscriptionOptions(packages)
                }
            }

            selectedPackageIndex.observe(viewLifecycleOwner) { index ->
                updateSelectedPackage(index)
            }

            subscriptionMessage.observe(viewLifecycleOwner) { message ->
                binding.txtSubscriptionMessage.text = message
            }

            errorMessage.observe(viewLifecycleOwner) { error ->
                error?.let {
                    showError(it)
                }
            }

            purchaseSuccess.observe(viewLifecycleOwner) { success ->
                if (success) {
                    showSuccessDialog()
                }
            }

            showPurchaseFailedAlert.observe(viewLifecycleOwner) { show ->
                if (show) {
                    showPurchaseFailedDialog()
                }
            }
        }
    }

    private fun animateStrokeWidth(view: View, targetWidth: Float) {
        val animator = ValueAnimator.ofFloat(view.elevation, targetWidth)
        animator.addUpdateListener { animation ->
            view.elevation = animation.animatedValue as Float
        }
        animator.duration = 300
        animator.start()
    }

    private fun showSuccessDialog() {
        PurchaseSuccessDialog(requireContext()) {
            findNavController().popBackStack()
        }.show()
    }

    private fun showPurchaseFailedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.purchase_failed_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.retry)) { _, _ ->
                (requireActivity() as? AppCompatActivity)?.let { activity ->
                    viewModel.retryPurchase(activity)
                }
            }
            .show()
    }

    private fun showError(message: String) {
        binding.errorText.apply {
            text = message
            isVisible = true
        }
    }

    private fun handlePackageSelection(index: Int) {
        viewModel.selectPackage(index)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.logPaywallClosed()
    }
} 