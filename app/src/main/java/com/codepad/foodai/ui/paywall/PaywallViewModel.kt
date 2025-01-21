package com.codepad.foodai.ui.paywall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.helpers.FirebaseManager
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.RevenueCatManager
import com.codepad.foodai.helpers.UserSession
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.GoogleStoreProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

object AnalyticsEvent {
    const val FETCH_OFFERINGS_ERROR = "fetch_offerings_error"
    const val CHECK_SUBSCRIPTION_STATUS_ERROR = "check_subscription_status_error"
    const val PURCHASE_ERROR = "purchase_error"
    const val PURCHASE_CANCELLED = "purchase_cancelled"
    const val LOGIN_ERROR = "log_in_error"
    const val RESTORE_PURCHASES_ERROR = "restore_purchases_error"
    const val PAYWALL_OPENED = "paywall_opened"
    const val PAYWALL_CLOSED = "paywall_closed"
    const val SPECIAL_PAYWALL_SHOWN = "special_paywall_shown"
    const val PURCHASE_STARTED = "purchase_started"
    const val PURCHASE_SUCCESSFUL = "purchase_successful"
    const val RESTORE_STARTED = "restore_started"
    const val RESTORE_SUCCESSFUL = "restore_successful"
}

object AnalyticsParameter {
    const val ERROR_MESSAGE = "error_message"
    const val USER_ID = "user_id"
    const val PACKAGE_ID = "package_id"
    const val ADDITIONAL_INFO = "additional_info"
    const val SUBSCRIPTION_TYPE = "subscription_type"
    const val PRICE = "price"
    const val CURRENCY = "currency"
    const val IS_SPECIAL_OFFER = "is_special_offer"
    const val ERROR_CODE = "error_code"
}

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val revenueCatManager: RevenueCatManager,
    private val firebaseManager: FirebaseManager,
    private val resourceHelper: ResourceHelper,
) : ViewModel() {
    private val _isPurchasing = MutableLiveData(false)
    val isPurchasing = _isPurchasing

    private val _purchaseSuccess = MutableLiveData(false)
    val purchaseSuccess = _purchaseSuccess

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage = _errorMessage

    private val _selectedPackage = MutableLiveData<Package?>(null)
    val selectedPackage = _selectedPackage

    private val _showSuccessPopup = MutableLiveData(false)
    val showSuccessPopup = _showSuccessPopup

    private val _showSpecialPaywall = MutableLiveData(false)
    val showSpecialPaywall = _showSpecialPaywall

    val firstPackage = MutableLiveData<Package>()
    val secondPackage = MutableLiveData<Package>()

    private var purchaseTimeoutJob: Job? = null

    val firstProductName = firstPackage.map {
        val packageProductName = (it.product as? GoogleStoreProduct)?.productDetails?.name
        if (packageProductName?.lowercase() == "weekly") {
            resourceHelper.getString(R.string.weekly)
        } else {
            packageProductName
        }
    }

    val secondProductName = secondPackage.map {
        val packageProductName = (it.product as? GoogleStoreProduct)?.productDetails?.name
        if (packageProductName?.lowercase() == "yearly") {
            resourceHelper.getString(R.string.yearly)
        } else {
            packageProductName
        }
    }

    val firstProductData = firstPackage.map { it?.product }
    val secondProductData = secondPackage.map { it?.product }

    val firstProductHasFreeTrial = firstPackage.map {
        it?.product?.subscriptionOptions?.freeTrial != null
    }

    val buttonText = selectedPackage.map { selectedPackage ->
        if (!firebaseManager.isPassedStore.value) {
            resourceHelper.getString(R.string.continuee)
        } else if (firstProductHasFreeTrial.value == true && selectedPackage == firstPackage.value) {
            resourceHelper.getString(R.string.try_3_days_free)
        } else {
            resourceHelper.getString(R.string.continuee)
        }
    }

    init {
        revenueCatManager.init()
        revenueCatManager.fetchOfferings()
        selectInitialPackage()
        logPaywallOpened()
        checkAndShowSpecialPaywallIfNeeded()
    }

    private fun logPaywallOpened() {
        val bundle = Bundle().apply {
            putString(AnalyticsParameter.USER_ID, UserSession.userID)
            putBoolean(AnalyticsParameter.IS_SPECIAL_OFFER, _showSpecialPaywall.value == true)
        }
        firebaseManager.logEvent(AnalyticsEvent.PAYWALL_OPENED, bundle)
    }

    fun logPaywallClosed() {
        val bundle = Bundle().apply {
            putString(AnalyticsParameter.USER_ID, UserSession.userID)
            putBoolean(AnalyticsParameter.IS_SPECIAL_OFFER, _showSpecialPaywall.value == true)
        }
        firebaseManager.logEvent(AnalyticsEvent.PAYWALL_CLOSED, bundle)
    }

    private fun selectInitialPackage() {
        if (_selectedPackage.value == null) {
            val packages = revenueCatManager.offerings.value?.current?.availablePackages
            packages?.let {
                it.firstOrNull { packageItem ->
                    val productID = packageItem.product.purchasingData.productId
                    productID == firebaseManager.firstProduct.value
                }?.let { firstProduct ->
                    firstPackage.value = firstProduct
                    _selectedPackage.value = firstPackage.value
                }

                it.firstOrNull { packageItem ->
                    val productID = packageItem.product.purchasingData.productId
                    productID == firebaseManager.secondProduct.value
                }?.let { secondProduct ->
                    secondPackage.value = secondProduct
                }
            }
        }
    }

    private fun checkAndShowSpecialPaywallIfNeeded() {
        val user = UserSession.currentUser ?: return
        val isSubscribed = revenueCatManager.isSubscribed.value
        val isSpecialEvent = firebaseManager.specialEventDay.value

        user.createdAt?.let { createdAt ->
            val sixHoursAgo = Date(System.currentTimeMillis() - (6 * 60 * 60 * 1000))
            if (!isSubscribed && isSpecialEvent && createdAt.before(sixHoursAgo)) {
                _showSpecialPaywall.value = true
                val bundle = Bundle().apply {
                    putString(AnalyticsParameter.USER_ID, UserSession.userID)
                }
                firebaseManager.logEvent(AnalyticsEvent.SPECIAL_PAYWALL_SHOWN, bundle)
            }
        }
    }

    fun selectPackage(packageItem: Package?) {
        _selectedPackage.value = packageItem
    }

    fun purchase(activity: AppCompatActivity) {
        val packageToPurchase = _selectedPackage.value ?: run {
            _errorMessage.value = resourceHelper.getString(R.string.select_subscription_option)
            return
        }

        logPurchaseStarted(packageToPurchase)
        startPurchaseTimeout()

        _isPurchasing.value = true
        _errorMessage.value = null
        _purchaseSuccess.value = false

        revenueCatManager.purchase(activity, packageToPurchase) { success ->
            cancelPurchaseTimeout()
            _isPurchasing.value = false

            if (success) {
                handlePurchaseSuccess(packageToPurchase)
            }
        }
    }

    private fun logPurchaseStarted(packageToPurchase: Package) {
        val product = packageToPurchase.product as? GoogleStoreProduct
        val bundle = Bundle().apply {
            putString(AnalyticsParameter.PACKAGE_ID, packageToPurchase.identifier)
            putString(AnalyticsParameter.SUBSCRIPTION_TYPE, product?.productDetails?.name)
            putString(AnalyticsParameter.PRICE, product?.price?.formatted)
            putString(AnalyticsParameter.CURRENCY, product?.price?.currencyCode)
            putBoolean(AnalyticsParameter.IS_SPECIAL_OFFER, _showSpecialPaywall.value == true)
        }
        firebaseManager.logEvent(AnalyticsEvent.PURCHASE_STARTED, bundle)
    }

    private fun handlePurchaseSuccess(packageToPurchase: Package) {
        _purchaseSuccess.value = true
        _showSuccessPopup.value = true
        revenueCatManager.checkSubscriptionStatus()

        val product = packageToPurchase.product as? GoogleStoreProduct
        val bundle = Bundle().apply {
            putString(AnalyticsParameter.PACKAGE_ID, packageToPurchase.identifier)
            putString(AnalyticsParameter.SUBSCRIPTION_TYPE, product?.productDetails?.name)
            putString(AnalyticsParameter.PRICE, product?.price?.formatted)
            putString(AnalyticsParameter.CURRENCY, product?.price?.currencyCode)
            putBoolean(AnalyticsParameter.IS_SPECIAL_OFFER, _showSpecialPaywall.value == true)
        }
        firebaseManager.logEvent(AnalyticsEvent.PURCHASE_SUCCESSFUL, bundle)
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

    fun restorePurchases() {
        _isPurchasing.value = true
        firebaseManager.logEvent(AnalyticsEvent.RESTORE_STARTED, Bundle())

        revenueCatManager.restorePurchases { success ->
            _isPurchasing.value = false
            if (success) {
                _purchaseSuccess.value = true
                _showSuccessPopup.value = true
                firebaseManager.logEvent(AnalyticsEvent.RESTORE_SUCCESSFUL, Bundle())
            } else {
                _errorMessage.value = resourceHelper.getString(R.string.restore_purchases_failed)
            }
        }
    }

    fun dismissSuccessPopup() {
        _showSuccessPopup.value = false
    }

    fun getPlanNameLocalized(period: SubscriptionType?): String {
        return when (period) {
            SubscriptionType.WEEKLY -> resourceHelper.getString(R.string.weekly)
            SubscriptionType.WEEKLY_NO_TRIAL -> resourceHelper.getString(R.string.weekly)
            SubscriptionType.ANNUAL -> resourceHelper.getString(R.string.yearly)
            SubscriptionType.SPECIAL -> resourceHelper.getString(R.string.special)
            else -> ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        logPaywallClosed()
        cancelPurchaseTimeout()
    }
}
