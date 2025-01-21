package com.codepad.foodai.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.restorePurchasesWith
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseManager: FirebaseManager,
) : UpdatedCustomerInfoListener {

    val offerings = MutableLiveData<Offerings?>()
    val isUserSubscribed = MutableLiveData(false)
    val showPaywall = MutableLiveData(false)

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var sharedPreferences: SharedPreferences

    fun init() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        sharedPreferences = this.context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        Purchases.configure(
            PurchasesConfiguration.Builder(this.context, "goog_qxGMiwXmMnmrenCIyQYYOdaRuYT").build()
        )
        Purchases.sharedInstance.updatedCustomerInfoListener = this

        fetchOfferings()
        checkSubscriptionStatus()
    }

    fun fetchOfferings() {
        Purchases.sharedInstance.getOfferingsWith({ error ->
            Log.e("RevenueCat", "Error fetching offerings: ${error.message}")
            firebaseManager.logEvent("fetchOfferingsError")
            val params = Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
            }
            Sentry.captureException(Throwable("RevenueCat fetchOfferings error: ${error.message}"))
        }) { offerings ->
            this.offerings.postValue(offerings)
            offerings.current?.let { current ->
                Log.d("RevenueCat", "Current offering identifier: ${current.identifier}")
                Log.d("RevenueCat", "Available packages count: ${current.availablePackages.size}")
            } ?: run {
                Log.d("RevenueCat", "No current offering found.")
            }
        }
    }

    fun checkSubscriptionStatus() {
        val isSubscribed = sharedPreferences.getBoolean("isUserSubscribed", false)
        updateUserSubscribedState(isSubscribed)

        Purchases.sharedInstance.getCustomerInfoWith({ error ->
            Log.e("RevenueCat", "Error fetching customer info: ${error.message}")
            firebaseManager.logEvent("checkSubscriptionStatusError")
            val params = Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
            }
            Sentry.captureException(Throwable("RevenueCat checkSubscriptionStatus error: ${error.message}"))
        }) { customerInfo ->
            val isSubscribedNow = customerInfo.entitlements["premium_access"]?.isActive == true
            updateUserSubscribedState(isSubscribedNow)
            Log.d("RevenueCat", "Has user premium? $isSubscribedNow")
            firebaseAnalytics.setUserProperty("isPremium", isSubscribedNow.toString())
            sharedPreferences.edit().putBoolean("isUserSubscribed", isSubscribedNow).apply()
        }
    }

    fun purchase(activity: Activity, packageToPurchase: Package, completion: (Boolean) -> Unit) {
        val purchaseParams = PurchaseParams.Builder(activity, packageToPurchase).build()
        Purchases.sharedInstance.purchase(purchaseParams, object : PurchaseCallback {
            override fun onCompleted(
                storeTransaction: StoreTransaction,
                customerInfo: CustomerInfo,
            ) {
                val isSubscribedNow =
                    customerInfo.entitlements.get("premium_access")?.isActive == true
                updateUserSubscribedState(isSubscribedNow)
                fetchOfferings()
                completion(true)
            }

            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                Log.e("RevenueCat", "Purchase failed: ${error.message}")
                firebaseManager.logEvent("purchaseError")
                if (userCancelled) {
                    firebaseManager.logEvent("purchaseCancelled")
                }
                val params = Bundle().apply {
                    putString(AnalyticsParameter.errorMessage, error.message)
                    putString(AnalyticsParameter.packageID, packageToPurchase.identifier)
                }
                Sentry.captureException(Throwable("RevenueCat purchase error: ${error.message} code ${error.code}"))
                completion(false)
            }


        })
    }

    fun logInUser(userID: String, completion: (Boolean) -> Unit) {
        Purchases.sharedInstance.logInWith(userID, { error ->
            Log.e("RevenueCat", "RevenueCat logIn error: ${error.message}")
            firebaseManager.logEvent("logInError")
            val params = Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
                putString(AnalyticsParameter.userID, userID)
            }
            Sentry.captureException(Throwable("RevenueCat logIn error: ${error.message} code ${error.code}"))
            completion(false)
        }) { customerInfo, created ->
            Log.d("RevenueCat", "Logged into RevenueCat with userID: $userID")
            if (created) {
                Log.d("RevenueCat", "New RevenueCat user created.")
                val params = Bundle().apply {
                    putString(AnalyticsParameter.userID, userID)
                }
                firebaseManager.logEvent("new_revenuecat_user_created")
            }
            val isSubscribedNow = customerInfo.entitlements["premium_access"]?.isActive == true
            updateUserSubscribedState(isSubscribedNow)
            offerings.postValue(null)
            fetchOfferings()
            firebaseAnalytics.setUserId(userID)
            completion(true)
        }
    }

    fun restorePurchases(completion: (Boolean) -> Unit) {
        Purchases.sharedInstance.restorePurchasesWith({ error ->
            Log.e("RevenueCat", "Restore failed: ${error.message}")
            completion(false)
        }) { customerInfo ->
            val isSubscribedNow = customerInfo.entitlements["premium_access"]?.isActive == true
            updateUserSubscribedState(isSubscribedNow)
            fetchOfferings()
            completion(isSubscribedNow)
        }
    }

    fun triggerPaywall() {
        showPaywall.postValue(true)
    }

    override fun onReceived(customerInfo: CustomerInfo) {
        val isSubscribedNow = customerInfo.entitlements["premium_access"]?.isActive == true
        updateUserSubscribedState(isSubscribedNow)
    }

    private fun updateUserSubscribedState(isSubscribedNow: Boolean) {
        isUserSubscribed.postValue(isSubscribedNow)
        UserSession.isPremiumUser = isSubscribedNow
    }
}

object AnalyticsParameter {
    const val errorMessage = "error_message"
    const val userID = "user_id"
    const val packageID = "package_id"
}
