package com.codepad.foodai.ui.home.home.pager.health

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentGoogleHealthBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.codepad.foodai.ui.home.settings.health.HealthConnectManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            setupStepChart()

            // Move the observer setup inside the Health Connect supported block
            healthConnectManager.onGoogleFitBodyDataRead = { stepData ->
                val (currentDaySteps, previousDaySteps) = stepData
                viewLifecycleOwner.lifecycleScope.launch {
                    binding.notConnectedView.visibility = View.GONE
                    binding.connectedView.visibility = View.VISIBLE
                    
                    // Always update the chart, even with empty data
                    updateStepData(
                        currentDaySteps.ifEmpty { listOf(0) },
                        previousDaySteps.ifEmpty { List(6) { 0 } }
                    )
                }
            }
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

        // Keep existing observers
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

    private fun setupStepChart() {
        binding.stepChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawValueAboveBar(false)
            minimumHeight = 100 // Even smaller height
            
            // Triple the previous margin values
            setViewPortOffsets(144f, 24f, 48f, 96f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = Color.parseColor("#20FFFFFF")
                textColor = Color.parseColor("#FFFFFF")
                textSize = 10f
                valueFormatter = DayAxisValueFormatter()
                granularity = 1f
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#20FFFFFF")
                yOffset = 24f // Triple the offset
                labelCount = 7
                spaceMin = 0.2f
                spaceMax = 0.2f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#20FFFFFF")
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#20FFFFFF")
                textColor = Color.parseColor("#FFFFFF")
                textSize = 10f
                axisMinimum = 0f
                axisMaximum = 4000f
                labelCount = 5
                setDrawLabels(true)
                spaceTop = 24f // Triple the space
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            }

            axisRight.isEnabled = false
            
            // Triple the extra offsets
            setExtraOffsets(12f, 12f, 12f, 12f)
        }
    }

    private fun updateStepData(currentDaySteps: List<Int>, previousDaySteps: List<Int>) {
        // Update step count with only today's steps
        binding.txtStepCount.text = currentDaySteps.lastOrNull()?.toString() ?: "0"

        // Update chart
        val entries = mutableListOf<BarEntry>()
        val allSteps = previousDaySteps + currentDaySteps

        // Ensure we have exactly 7 days of data
        val paddedSteps = when {
            allSteps.size > 7 -> allSteps.takeLast(7)
            allSteps.size < 7 -> List(7 - allSteps.size) { 0 } + allSteps
            else -> allSteps
        }

        // Create entries with actual values
        paddedSteps.forEachIndexed { index, steps ->
            entries.add(BarEntry(index.toFloat(), steps.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Steps").apply {
            color = Color.parseColor("#037aff")
            setDrawValues(false)
            setDrawIcons(false)
            highLightColor = Color.TRANSPARENT
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        binding.stepChart.apply {
            data = barData
            animateY(500)
            invalidate()
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
            cardHealthCalories.visibility = View.VISIBLE
            cardDailyScore.visibility = View.VISIBLE
            
            // Show not connected state
            notConnectedView.visibility = View.VISIBLE
            connectedView.visibility = View.GONE
            
            // Setup initial values
            txtStepCount.text = "0"
            txtBurnedCalories.text = getString(R.string.calories_burned_format, 0)
            progressBar.progress = 0
            txtProgress.text = "0/10"
            
            // Setup empty chart
            setupStepChart()
            updateStepData(listOf(0), List(6) { 0 })
            
            // Setup auth button for Play Store
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

    inner class DayAxisValueFormatter : IndexAxisValueFormatter() {
        private val dayLetters = arrayOf("M", "T", "W", "T", "F", "S", "S")
        
        override fun getFormattedValue(value: Float): String {
            return dayLetters[value.toInt()]
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HealthConnectManagerEntryPoint {
    fun getHealthConnectManager(): HealthConnectManager
}