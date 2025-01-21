package com.codepad.foodai.ui.paywall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.codepad.foodai.R
import com.codepad.foodai.helpers.FirebaseManager
import com.codepad.foodai.helpers.FirebaseRemoteConfigManager
import com.codepad.foodai.helpers.ResourceHelper
import com.codepad.foodai.helpers.RevenueCatManager
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.GoogleStoreProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val revenueCatManager: RevenueCatManager,
    private val firebaseManager: FirebaseManager,
    private val resourceHelper: ResourceHelper,
) :
    ViewModel() {
    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: MutableLiveData<String?> = _errorMessage

    private val _selectedPackage = MutableStateFlow<Package?>(null)
    val selectedPackage: StateFlow<Package?> = _selectedPackage

    private val _showSuccessPopup = MutableStateFlow(false)
    val showSuccessPopup: StateFlow<Boolean> = _showSuccessPopup

    val firstPackage = MutableLiveData<Package>()
    val secondPackage = MutableLiveData<Package>()

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
        if (packageProductName?.lowercase() == "monthly") {
            resourceHelper.getString(R.string.monthly)
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
        if (firstProductHasFreeTrial.value == true && selectedPackage == firstPackage.value) {
            resourceHelper.getString(R.string.try_3_days_free)
        } else {
            resourceHelper.getString(R.string.continuee)
        }
    }.asLiveData()

    init {
        revenueCatManager.init()
        revenueCatManager.fetchOfferings()
        selectInitialPackage()

        viewModelScope.launch {
            revenueCatManager.offerings.asFlow().collect { offerings ->
                selectInitialPackage()
            }
        }
    }

    private fun selectInitialPackage() {
        if (_selectedPackage.value == null) {
            val packages = revenueCatManager.offerings.value?.current?.availablePackages
            packages?.let {
                it.firstOrNull { packageItem ->
                    val productID = packageItem.product.purchasingData.productId
                    productID == FirebaseRemoteConfigManager.getString("firstProduct")
                }?.let { firstProduct ->
                    firstPackage.value = firstProduct
                    _selectedPackage.value = firstPackage.value
                }

                it.firstOrNull { packageItem ->
                    val productID = packageItem.product.purchasingData.productId
                    productID == FirebaseRemoteConfigManager.getString("secondProduct")
                }?.let { secondProduct ->
                    secondPackage.value = secondProduct
                }
            }
        }
    }

    fun selectPackage(packageItem: Package?) {
        _selectedPackage.value = packageItem
    }

    fun purchase(activity: AppCompatActivity) {
        val packageToPurchase = _selectedPackage.value ?: run {
            _errorMessage.value = "Please select a subscription option."
            return
        }
        val bundle = Bundle().apply {
            putString("package", packageToPurchase.product.title)
        }
        firebaseManager.logEvent("purchase_pressed", bundle)
        
        _isPurchasing.value = true
        _errorMessage.value = null
        _purchaseSuccess.value = false
        revenueCatManager.purchase(activity, packageToPurchase) { success ->
            _isPurchasing.value = false
            if (success) {
                _purchaseSuccess.value = true
                _showSuccessPopup.value = true
                revenueCatManager.checkSubscriptionStatus()
                val successBundle = Bundle().apply {
                    putString("package", packageToPurchase.identifier)
                }
                firebaseManager.logEvent("purchase_successful", successBundle)
            } else {
                _errorMessage.value = "Purchase failed. Please try again."
            }
        }
    }

    fun restorePurchases() {
        _isPurchasing.value = true
        revenueCatManager.restorePurchases { success ->
            _isPurchasing.value = false
            if (success) {
                _purchaseSuccess.value = true
                _showSuccessPopup.value = true
                val bundle = Bundle().apply {
                    putString("status", "success")
                }
                firebaseManager.logEvent("purchases_restored", bundle)
            } else {
                _errorMessage.value = "Failed to restore purchases."
            }
        }
    }

    fun dismissSuccessPopup() {
        _showSuccessPopup.value = false
    }
}
