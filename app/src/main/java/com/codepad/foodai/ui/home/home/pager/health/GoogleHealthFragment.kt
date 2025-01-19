package com.codepad.foodai.ui.home.home.pager.health

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.health.connect.client.HealthConnectClient
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoogleHealthBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.codepad.foodai.ui.home.settings.health.HealthConnectManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@AndroidEntryPoint
class GoogleHealthFragment : BaseFragment<FragmentGoogleHealthBinding>() {
    private val sharedViewModel: HomePagerViewModel by activityViewModels()
    private lateinit var healthConnectManager: HealthConnectManager

    override fun getLayoutId(): Int = R.layout.fragment_google_health

    override fun onReadyView() {
        if (isHealthConnectSupported() || isHealthConnectSDKAvailable()) {
            initHealthConnect()
            healthConnectManager.initContent(this)
            setupHealthConnectUI()
        } else {
            setupNotSupportedUI()
        }

        binding.authButton.setOnClickListener {
            if (isHealthConnectSupported() || isHealthConnectSDKAvailable()) {
                healthConnectManager.readData()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.install_health_connect_toast),
                    Toast.LENGTH_LONG
                ).show()
                val playStoreIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata&hl=en")
                )
                startActivity(playStoreIntent)
            }
        }

        sharedViewModel.dailySummary.observe(viewLifecycleOwner) { dailySummary ->
            binding.apply {
                progressBar.progress = (dailySummary.healthScore?.times(10))?.toInt() ?: 0
                txtProgress.text = "${dailySummary.healthScore}/10"

                // Calculate total calories burned from exercises
                val exerciseCalories = dailySummary.exercises?.sumOf { it.caloriesBurned ?: 0 } ?: 0

                // TODO: Add step count calories when available
                // For now, only show exercise calories
                binding.txtBurnedCalories.text =
                    getString(R.string.calories_burned_format, exerciseCalories)

                // Show exercise breakdown
                val runningCalories = dailySummary.exercises
                    ?.filter { it.exerciseType == "run" }
                    ?.sumOf { it.caloriesBurned ?: 0 } ?: 0

                val weightliftingCalories = dailySummary.exercises
                    ?.filter { it.exerciseType == "weightlifting" }
                    ?.sumOf { it.caloriesBurned ?: 0 } ?: 0

                val otherExerciseCalories = dailySummary.exercises
                    ?.filter { it.exerciseType != "run" && it.exerciseType != "weightlifting" }
                    ?.sumOf { it.caloriesBurned ?: 0 } ?: 0

                // Update exercise type views
                if (dailySummary.exercises?.isNotEmpty() == true) {
                    val firstExercise = dailySummary.exercises.last()
                    when (firstExercise.exerciseType) {
                        "run" -> {
                            txtFirstExerciseType.text = getString(R.string.running)
                            txtFirstExerciseCalories.text =
                                getString(R.string.calories_burned_format, runningCalories)
                        }

                        "weightlifting" -> {
                            txtFirstExerciseType.text = getString(R.string.weightlifting)
                            txtFirstExerciseCalories.text =
                                getString(R.string.calories_burned_format, weightliftingCalories)
                        }

                        else -> {
                            txtFirstExerciseType.text = firstExercise.exerciseType?.capitalize()
                                ?: getString(R.string.exercise)
                            txtFirstExerciseCalories.text =
                                getString(R.string.calories_burned_format, otherExerciseCalories)
                        }
                    }

                    // Show second exercise if exists and different type
                    if (dailySummary.exercises.size > 1) {
                        val secondExercise = dailySummary.exercises[dailySummary.exercises.size - 2]
                        if (secondExercise.exerciseType != firstExercise.exerciseType) {
                            secondExerciseLayout.visibility = View.VISIBLE
                            when (secondExercise.exerciseType) {
                                "run" -> {
                                    txtSecondExerciseType.text = getString(R.string.running)
                                    txtSecondExerciseCalories.text =
                                        getString(R.string.calories_burned_format, runningCalories)
                                }

                                "weightlifting" -> {
                                    txtSecondExerciseType.text = getString(R.string.weightlifting)
                                    txtSecondExerciseCalories.text = getString(
                                        R.string.calories_burned_format,
                                        weightliftingCalories
                                    )
                                }

                                else -> {
                                    txtSecondExerciseType.text =
                                        secondExercise.exerciseType?.capitalize()
                                            ?: getString(R.string.exercise)
                                    txtSecondExerciseCalories.text = getString(
                                        R.string.calories_burned_format,
                                        otherExerciseCalories
                                    )
                                }
                            }
                        } else {
                            secondExerciseLayout.visibility = View.GONE
                        }
                    } else {
                        secondExerciseLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupHealthConnectUI() {
        healthConnectManager.hasUnlockedIntegration { isConnected ->
            if (isConnected) {
                healthConnectManager.readData()
            }
        }
    }

    private fun setupNotSupportedUI() {
        binding.apply {
            clHealth.visibility = View.VISIBLE
            cardHealthCalories.visibility = View.GONE
            cardDailyScore.visibility = View.GONE
            authButton.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.install_health_connect_toast),
                    Toast.LENGTH_LONG
                ).show()
                val playStoreIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata&hl=en")
                )
                startActivity(playStoreIntent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isHealthConnectSupported() || isHealthConnectSDKAvailable()) {
            healthConnectManager.hasUnlockedIntegration { isConnected ->
                if (isConnected) {
                    healthConnectManager.readData()
                }
            }
        }
        binding.root.requestLayout()
    }

    private fun initHealthConnect() {
        val entryPoint = EntryPointAccessors.fromApplication(
            requireContext(), HealthConnectManagerEntryPoint::class.java
        )
        healthConnectManager = entryPoint.getHealthConnectManager()
    }

    private fun isHealthConnectSDKAvailable(): Boolean {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(requireContext(), "com.google.android.apps.healthdata")
        return availabilityStatus == HealthConnectClient.SDK_AVAILABLE
    }

    private fun isHealthConnectSupported(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HealthConnectManagerEntryPoint {
    fun getHealthConnectManager(): HealthConnectManager
}