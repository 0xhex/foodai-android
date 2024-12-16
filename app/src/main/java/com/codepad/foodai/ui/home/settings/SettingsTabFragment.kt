package com.codepad.foodai.ui.home.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.BuildConfig
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentSettingsBinding
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// TODO: localizations
@AndroidEntryPoint
class SettingsTabFragment : BaseFragment<FragmentSettingsBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun onReadyView() {
        setupUI()
        viewModel.fetchUserData()
    }

    private fun setupUI() {
        binding.itemPersonalDetails.setOnClickListener {
            findNavController().navigate(SettingsTabFragmentDirections.actionNavigationSettingsToPersonalFragment())
        }

        binding.itemAdjustGoals.setOnClickListener {

        }

        binding.itemTermsAndConditions.setOnClickListener {
            openURL("https://www.food-ai-scanner.com/terms.html")
        }

        binding.itemPrivacyPolicy.setOnClickListener {
            openURL("https://www.food-ai-scanner.com/privacy.html")
        }

        binding.itemSupportEmail.setOnClickListener {
            sendSupportEmail()
        }

        binding.itemDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        // TODO: if (!RevenueCatManager.isUserSubscribed) {
        //     binding.cardPremium.visibility = View.VISIBLE
        //     binding.cardPremium.setOnClickListener {
        //         RevenueCatManager.triggerPaywall()
        //         FirebaseManager.logEvent("paywall_open_settings", null)
        //     }
        // }

        viewModel.userDataResponse.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                binding.txtAgeValue.text = calculateAge(parseDate(userData.dateOfBirth.orEmpty())).toString()
                binding.txtHeightValue.text = formatHeight(userData)
                binding.txtCurrentWeightValue.text = formatWeight(userData)
            }
        }
    }

    private fun calculateAge(dateOfBirth: Date?): Int {
        if (dateOfBirth == null) return 0
        val calendar = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { time = dateOfBirth }
        var age = calendar.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (calendar.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun formatHeight(userData: User): String {
        return if (userData.isMetric == true) {
            "${userData.height?.toInt()} cm"
        } else {
            val totalInches = (userData.height ?: 0.0) / 2.54
            val feet = (totalInches / 12).toInt()
            val inches = (totalInches % 12).toInt()
            "$feet ft $inches in"
        }
    }

    private fun formatWeight(userData: User): String {
        return if (userData.isMetric == true) {
            "${userData.weight?.toInt()} kg"
        } else {
            "${userData.weight?.toInt()} lb"
        }
    }

    private fun openURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun sendSupportEmail() {
        val email = "help@codepad.studio"
        val subject = "Support Request - App Version ${BuildConfig.VERSION_NAME}"
        val body = """
            Hello Support Team,

            I am experiencing an issue with the app. My Problem...

            Details:
            - User ID: ${UserSession.user?.id}
            - App Version: ${BuildConfig.VERSION_NAME}
            - Device: ${android.os.Build.MODEL}
            - Android Version: ${android.os.Build.VERSION.RELEASE}

            Please assist.

            Best regards,
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(intent)
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Account Deletion")
            .setMessage("Are you sure you want to delete your account? This action is irreversible and will permanently remove all your data from our system.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes, Delete My Account") { dialog, _ ->
                deleteAccount()
            }
            .show()
    }

    private fun deleteAccount() {

    }

    fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(dateString)
    }
}