package com.codepad.foodai.ui.paywall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.helpers.FirebaseManager
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.RevenueCatManager
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.Period
import com.revenuecat.purchases.models.Price
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import java.math.BigDecimal
import java.math.RoundingMode

@HiltViewModel
class FasticPaywallViewModel @Inject constructor(
    private val revenueCatManager: RevenueCatManager,
    private val firebaseManager: FirebaseManager,
    private val resourceHelper: ResourceHelper
) : ViewModel() {

    companion object {
        private const val TAG = "FasticPaywallVM"
    }

    private val _isPurchasing = MutableLiveData(false)
    val isPurchasing: LiveData<Boolean> = _isPurchasing
    private var lastInputPackages: List<Package>? = null

    private val _purchaseSuccess = MutableLiveData(false)
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedPackageIndex = MutableLiveData(1)
    val selectedPackageIndex: LiveData<Int> = _selectedPackageIndex

    private val _selectedPackage = MutableLiveData<Package?>(null)
    val selectedPackage: LiveData<Package?> = _selectedPackage

    private val _subscriptionMessage = MutableLiveData<String>()
    val subscriptionMessage: LiveData<String> = _subscriptionMessage

    private var _sortedPackages = MutableLiveData<List<Package>>(emptyList())
    val sortedPackages: LiveData<List<Package>> = _sortedPackages

    private val _showPurchaseSheet = MutableLiveData(false)
    val showPurchaseSheet: LiveData<Boolean> = _showPurchaseSheet

    private val _showSuccessPopup = MutableLiveData(false)
    val showSuccessPopup: LiveData<Boolean> = _showSuccessPopup

    private val _showPurchaseFailedAlert = MutableLiveData(false)
    val showPurchaseFailedAlert: LiveData<Boolean> = _showPurchaseFailedAlert

    private var purchaseTimeoutJob: Job? = null
    private var lastFailedPackage: Package? = null

    init {
        android.util.Log.d(TAG, "Initializing ViewModel")
        revenueCatManager.init()
        revenueCatManager.fetchOfferings()
        viewModelScope.launch {
            revenueCatManager.offerings.collect { offerings ->
                processOfferings(offerings)
            }
        }
    }

    private fun processOfferings(offerings: com.revenuecat.purchases.Offerings?) {
        if (offerings == null) {
            android.util.Log.w(TAG, "Offerings is null")
            return
        }

        val packages = offerings.current?.availablePackages
        if (packages.isNullOrEmpty()) {
            android.util.Log.w(TAG, "No available packages")
            return
        }

        android.util.Log.d(TAG, "Processing offerings with ${packages.size} packages")
        android.util.Log.d(TAG, "Package IDs: ${packages.map { "${it.identifier} (${it.product.id})" }}")
        android.util.Log.d(TAG, "Package periods: ${packages.map { "${it.identifier}: ${it.product.period?.unit}" }}")

        if (packages == lastInputPackages) {
            android.util.Log.d(TAG, "Packages unchanged, skipping update")
            return
        }

        lastInputPackages = packages
        val sorted = sortPackages(packages)
        android.util.Log.d(TAG, "Sorted ${sorted.size} packages")
        _sortedPackages.value = sorted
        selectInitialPackage()
    }

    fun sortPackages(packages: List<Package>): List<Package> {
        // Return cached packages if input hasn't changed
        if (_sortedPackages.value != null && !_sortedPackages.value.isNullOrEmpty() && packages == lastInputPackages) {
            android.util.Log.d(TAG, "Returning cached packages")
            return _sortedPackages.value?.toList()!!
        }

        android.util.Log.d(TAG, "Sorting packages, input size: ${packages.size}")
        android.util.Log.d(TAG, "Available package IDs: ${packages.map { "${it.identifier} (${it.product.id})" }}")
        
        val packagesByPeriod = packages.groupBy { it.product.period?.unit }
        android.util.Log.d(TAG, "Packages by period: ${packagesByPeriod.keys}")

        val sortedPackages = mutableListOf<Package>()
        
        // Try to find yearly package
        val yearlyPackage = packages.firstOrNull { 
            it.product.id == "foodai_yearly_standart:yearly" || 
            it.identifier == "foodai_yearly_standart:yearly" ||
            it.product.id.startsWith("foodai_yearly_standart")
        } ?: packagesByPeriod[Period.Unit.YEAR]?.firstOrNull()
        
        if (yearlyPackage != null) {
            sortedPackages.add(yearlyPackage)
            android.util.Log.d(TAG, "Added yearly package: ${yearlyPackage.identifier}, ID: ${yearlyPackage.product.id}")
        } else {
            android.util.Log.w(TAG, "No yearly package found. Available yearly packages: ${packagesByPeriod[Period.Unit.YEAR]?.map { "${it.identifier} (${it.product.id})" }}")
        }

        // Try to find monthly package
        val monthlyPackage = packages.firstOrNull { 
            it.product.id == "foodai_monthly_low:monthly-low" || 
            it.identifier == "foodai_monthly_low:monthly-low" ||
            it.product.id.startsWith("foodai_monthly_low")
        } ?: packagesByPeriod[Period.Unit.MONTH]?.firstOrNull()
        
        if (monthlyPackage != null) {
            sortedPackages.add(monthlyPackage)
            android.util.Log.d(TAG, "Added monthly package: ${monthlyPackage.identifier}, ID: ${monthlyPackage.product.id}")
        } else {
            android.util.Log.w(TAG, "No monthly package found. Available monthly packages: ${packagesByPeriod[Period.Unit.MONTH]?.map { "${it.identifier} (${it.product.id})" }}")
        }

        // Try to find weekly package
        val weeklyPackage = packages.firstOrNull { 
            it.product.id == "foodai_weekly_tier3:weekly-tier3" || 
            it.identifier == "foodai_weekly_tier3:weekly-tier3" ||
            it.product.id.startsWith("foodai_weekly_tier3")
        } ?: packagesByPeriod[Period.Unit.WEEK]?.firstOrNull()
        
        if (weeklyPackage != null) {
            sortedPackages.add(weeklyPackage)
            android.util.Log.d(TAG, "Added weekly package: ${weeklyPackage.identifier}, ID: ${weeklyPackage.product.id}")
        } else {
            android.util.Log.w(TAG, "No weekly package found. Available weekly packages: ${packagesByPeriod[Period.Unit.WEEK]?.map { "${it.identifier} (${it.product.id})" }}")
        }

        android.util.Log.d(TAG, "Final sorted packages size: ${sortedPackages.size}")
        android.util.Log.d(TAG, "Final sorted package IDs: ${sortedPackages.map { "${it.identifier} (${it.product.id})" }}")
        
        // Cache the results
        _sortedPackages.value = sortedPackages
        lastInputPackages = packages
        
        return sortedPackages
    }

    private fun selectInitialPackage() {
        android.util.Log.d(TAG, "Selecting initial package")
        val packages = revenueCatManager.offerings.value?.current?.availablePackages
        android.util.Log.d(TAG, "Available packages: ${packages?.size ?: 0}")
        
        packages?.let { pkgs ->
            val sorted = sortPackages(pkgs)
            android.util.Log.d(TAG, "Sorted packages size: ${sorted.size}")
            
            if (sorted.isNotEmpty()) {
                // Select the first available package if current selection is invalid
                val currentIndex = _selectedPackageIndex.value ?: 1
                val validIndex = if (currentIndex >= sorted.size) 0 else currentIndex
                _selectedPackageIndex.value = validIndex
                _selectedPackage.value = sorted[validIndex]
                android.util.Log.d(TAG, "Selected package: ${_selectedPackage.value?.identifier}")
                updateSubscriptionMessage()
            }
        }
    }

    fun selectPackage(index: Int) {
        val packages = revenueCatManager.offerings.value?.current?.availablePackages
        packages?.let { pkgs ->
            val sorted = sortPackages(pkgs)
            if (index < sorted.size) {
                _selectedPackageIndex.value = index
                _selectedPackage.value = sorted[index]
                updateSubscriptionMessage()
            }
        }
    }

    fun logPaywallViewed() {
        firebaseManager.logEvent("fastic_paywall_viewed", Bundle())
    }

    fun logPaywallClosed() {
        firebaseManager.logEvent("fastic_paywall_closed", Bundle())
    }

    private fun updateSubscriptionMessage() {
        _selectedPackage.value?.let { pkg ->
            val messages = when (pkg.product.period?.unit) {
                Period.Unit.YEAR -> arrayOf(
                    resourceHelper.getString(R.string.yearly_message_1),
                    resourceHelper.getString(R.string.yearly_message_2),
                    resourceHelper.getString(R.string.yearly_message_3)
                )
                Period.Unit.MONTH -> arrayOf(
                    resourceHelper.getString(R.string.monthly_message_1),
                    resourceHelper.getString(R.string.monthly_message_2),
                    resourceHelper.getString(R.string.monthly_message_3)
                )
                Period.Unit.WEEK -> arrayOf(
                    resourceHelper.getString(R.string.weekly_message_1),
                    resourceHelper.getString(R.string.weekly_message_2),
                    resourceHelper.getString(R.string.weekly_message_3)
                )
                else -> arrayOf(resourceHelper.getString(R.string.monthly_message_1))
            }
            
            val randomMessage = messages[Random.nextInt(messages.size)]
            _subscriptionMessage.value = randomMessage
        }
    }

    fun purchase(activity: AppCompatActivity) {
        val packageToPurchase = _selectedPackage.value ?: run {
            _errorMessage.value = resourceHelper.getString(R.string.select_subscription_option)
            return
        }

        // Process the purchase directly - the decision to show sheet is in the fragment
        processPurchase(activity, packageToPurchase)
    }

    fun processPurchase(activity: AppCompatActivity, packageToPurchase: Package) {
        logPurchaseStarted(packageToPurchase)
        startPurchaseTimeout()

        _isPurchasing.value = true
        _errorMessage.value = null
        _purchaseSuccess.value = false
        lastFailedPackage = null

        revenueCatManager.purchase(activity, packageToPurchase) { success ->
            cancelPurchaseTimeout()
            _isPurchasing.value = false

            if (success) {
                handlePurchaseSuccess(packageToPurchase)
            } else {
                lastFailedPackage = packageToPurchase
                _showPurchaseFailedAlert.value = true
                
                firebaseManager.logEvent(
                    "purchase_failed",
                    Bundle().apply {
                        putString("type", "fastic")
                        putString("package", packageToPurchase.identifier)
                    }
                )
            }
        }
    }

    private fun handlePurchaseSuccess(packageToPurchase: Package) {
        _purchaseSuccess.value = true
        _showSuccessPopup.value = true
        revenueCatManager.checkSubscriptionStatus()

        firebaseManager.logEvent(
            "fastic_purchase_completed",
            Bundle().apply {
                putString("package", packageToPurchase.identifier)
            }
        )
    }

    fun retryPurchase(activity: AppCompatActivity) {
        lastFailedPackage?.let { pkg ->
            processPurchase(activity, pkg)
        }
    }

    fun restorePurchases() {
        _isPurchasing.value = true
        _errorMessage.value = null
        
        firebaseManager.logEvent("restore_started", Bundle())

        revenueCatManager.restorePurchases { success ->
            _isPurchasing.value = false
            if (success) {
                _purchaseSuccess.value = true
                _showSuccessPopup.value = true
                firebaseManager.logEvent("restore_successful", Bundle())
            } else {
                _errorMessage.value = resourceHelper.getString(R.string.restore_purchases_failed)
            }
        }
    }

    fun dismissSuccessPopup() {
        _showSuccessPopup.value = false
    }

    private fun startPurchaseTimeout() {
        purchaseTimeoutJob?.cancel()
        purchaseTimeoutJob = viewModelScope.launch {
            delay(30000) // 30 seconds timeout
            if (_isPurchasing.value == true) {
                _isPurchasing.value = false
                _errorMessage.value = resourceHelper.getString(R.string.purchase_timeout)
            }
        }
    }

    private fun cancelPurchaseTimeout() {
        purchaseTimeoutJob?.cancel()
        purchaseTimeoutJob = null
    }

    private fun logPurchaseStarted(packageToPurchase: Package) {
        firebaseManager.logEvent(
            "fastic_purchase_started",
            Bundle().apply {
                putString("package", packageToPurchase.identifier)
            }
        )
    }

    fun calculateWeeklyPrice(pkg: Package): String {
        val price = pkg.product.price
        val priceAmount = BigDecimal(price.amountMicros) / BigDecimal(1_000_000)
        
        val weeklyPrice = when (pkg.product.period?.unit) {
            Period.Unit.YEAR -> {
                // For yearly, divide total price by 52 weeks
                priceAmount.divide(BigDecimal(52), 2, RoundingMode.HALF_UP)
            }
            Period.Unit.MONTH -> {
                // For monthly, divide total price by 4 weeks
                priceAmount.divide(BigDecimal(4), 2, RoundingMode.HALF_UP)
            }
            Period.Unit.WEEK -> {
                // For weekly, use the price as is
                priceAmount
            }
            else -> priceAmount
        }

        // Format the price using the currency symbol from the original price
        val currencySymbol = price.formatted.replace(Regex("[0-9.,]"), "").trim()
        return "$currencySymbol${formatPrice(weeklyPrice)}"
    }

    fun calculateSavings(pkg: Package): Int? {
        val packages = revenueCatManager.offerings.value?.current?.availablePackages ?: return null
        val sortedPackages = sortPackages(packages)
        
        // Find weekly package for comparison
        val weeklyPackage = sortedPackages.firstOrNull { it.product.period?.unit == Period.Unit.WEEK } ?: return null
        
        // Calculate yearly cost for both packages
        val currentYearlyCost = when (pkg.product.period?.unit) {
            Period.Unit.YEAR -> BigDecimal(pkg.product.price.amountMicros)
            Period.Unit.MONTH -> BigDecimal(pkg.product.price.amountMicros).multiply(BigDecimal(12))
            Period.Unit.WEEK -> BigDecimal(pkg.product.price.amountMicros).multiply(BigDecimal(52))
            else -> return null
        }

        val weeklyYearlyCost = BigDecimal(weeklyPackage.product.price.amountMicros).multiply(BigDecimal(52))

        // Only calculate savings if the current package is cheaper per year
        if (currentYearlyCost >= weeklyYearlyCost) {
            return null
        }

        // Calculate savings percentage
        val savings = ((weeklyYearlyCost - currentYearlyCost)
            .divide(weeklyYearlyCost, 4, RoundingMode.HALF_UP))
            .multiply(BigDecimal(100))
        
        return savings.toInt()
    }

    private fun formatPrice(price: BigDecimal): String {
        return String.format("%.2f", price)
    }

    private fun Price.toDecimal(): BigDecimal {
        return BigDecimal(this.formatted.replace(Regex("[^0-9.]"), ""))
    }

    override fun onCleared() {
        super.onCleared()
        cancelPurchaseTimeout()
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }
} 