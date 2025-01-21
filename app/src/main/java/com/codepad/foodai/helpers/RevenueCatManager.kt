package com.codepad.foodai.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

/**
 * RevenueCatManager handles all in-app purchase and subscription functionality
 * using the RevenueCat SDK. It manages offerings, subscriptions, purchases,
 * and user authentication with RevenueCat.
 */
@Singleton
class RevenueCatManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseManager: FirebaseManager,
) : UpdatedCustomerInfoListener {

    val offerings = MutableLiveData<Offerings?>()
    val isUserSubscribed = MutableLiveData(false)
    val showPaywall = MutableLiveData(false)
    val showSpecialPaywall = MutableLiveData(false)

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
            firebaseManager.logEvent(AnalyticsEvent.fetchOfferingsError, Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
            })
            Sentry.captureException(Throwable("RevenueCat fetchOfferings error: ${error.message}"))
        }) { offerings ->
            this.offerings.postValue(offerings)
            if (offerings.current != null) {
                Log.d("RevenueCat", "Current offering identifier: ${offerings.current!!.identifier}")
                Log.d("RevenueCat", "Available packages count: ${offerings.current!!.availablePackages.size}")
            } else {
                val unknownErrorDescription = "No current offering found."
                Log.d("RevenueCat", unknownErrorDescription)
                firebaseManager.logEvent(AnalyticsEvent.fetchOfferingsError, Bundle().apply {
                    putString(AnalyticsParameter.errorMessage, unknownErrorDescription)
                })
            }
        }
    }

    fun checkSubscriptionStatus() {
        val isSubscribed = sharedPreferences.getBoolean("isUserSubscribed", false)
        updateUserSubscribedState(isSubscribed)

        Purchases.sharedInstance.getCustomerInfoWith({ error ->
            Log.e("RevenueCat", "Error fetching customer info: ${error.message}")
            firebaseManager.logEvent(AnalyticsEvent.checkSubscriptionStatusError, Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
            })
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
                val isSubscribedNow = customerInfo.entitlements["premium_access"]?.isActive == true
                updateUserSubscribedState(isSubscribedNow)
                fetchOfferings()
                completion(true)
            }

            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                Log.e("RevenueCat", "Purchase failed: ${error.message}")
                if (userCancelled) {
                    firebaseManager.logEvent(AnalyticsEvent.purchaseCancelled, Bundle().apply {
                        putString(AnalyticsParameter.packageID, packageToPurchase.identifier)
                    })
                } else {
                    firebaseManager.logEvent(AnalyticsEvent.purchaseError, Bundle().apply {
                        putString(AnalyticsParameter.errorMessage, error.message)
                        putString(AnalyticsParameter.packageID, packageToPurchase.identifier)
                    })
                }
                Sentry.captureException(Throwable("RevenueCat purchase error: ${error.message} code ${error.code}"))
                completion(false)
            }
        })
    }

    fun logInUser(userID: String, completion: (Boolean) -> Unit) {
        Purchases.sharedInstance.logInWith(userID, { error ->
            Log.e("RevenueCat", "RevenueCat logIn error: ${error.message}")
            firebaseManager.logEvent(AnalyticsEvent.logInError, Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
                putString(AnalyticsParameter.userID, userID)
            })
            Sentry.captureException(Throwable("RevenueCat logIn error: ${error.message} code ${error.code}"))
            completion(false)
        }) { customerInfo, created ->
            Log.d("RevenueCat", "Logged into RevenueCat with userID: $userID")
            if (created) {
                Log.d("RevenueCat", "New RevenueCat user created.")
                firebaseManager.logEvent("new_revenuecat_user_created", Bundle().apply {
                    putString(AnalyticsParameter.userID, userID)
                })
            } else {
                firebaseManager.logEvent("revenue_cat_login", Bundle().apply {
                    putString(AnalyticsParameter.userID, userID)
                })
                Handler(Looper.getMainLooper()).postDelayed({
                    checkAndShowSpecialPaywallIfNeeded()
                }, 1000)
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
            firebaseManager.logEvent(AnalyticsEvent.restorePurchasesError, Bundle().apply {
                putString(AnalyticsParameter.errorMessage, error.message)
            })
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

    fun triggerSpecialPaywall() {
        showSpecialPaywall.postValue(true)
    }

    fun checkAndShowSpecialPaywallIfNeeded() {
        val user = UserSession.currentUser ?: return
        val isSubscribed = isUserSubscribed.value ?: false
        val isSpecialEvent = firebaseManager.isSpecialEventDay

        val sixHoursAgo = System.currentTimeMillis() - (6 * 60 * 60 * 1000)
        val userCreatedAt = user.createdAt?.time ?: return

        if (!isSubscribed && isSpecialEvent && userCreatedAt < sixHoursAgo) {
            triggerSpecialPaywall()
        }
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

object AnalyticsEvent {
    const val fetchOfferingsError = "fetch_offerings_error"
    const val checkSubscriptionStatusError = "check_subscription_status_error"
    const val purchaseError = "purchase_error"
    const val purchaseCancelled = "purchase_cancelled"
    const val logInError = "log_in_error"
    const val restorePurchasesError = "restore_purchases_error"
}

object AnalyticsParameter {
    const val errorMessage = "error_message"
    const val userID = "user_id"
    const val packageID = "package_id"
    const val additionalInfo = "additional_info"
}
