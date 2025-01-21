package com.codepad.foodai.ui.home.analytics

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentAnalyticsTabBinding
import com.codepad.foodai.domain.models.user.WeightLogData
import com.codepad.foodai.ui.core.BaseFragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import dagger.hilt.android.AndroidEntryPoint
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.view.ViewStub

@AndroidEntryPoint
class AnalyticsTabFragment : BaseFragment<FragmentAnalyticsTabBinding>() {

    private val viewModel: AnalyticsViewModel by viewModels()
    override fun getLayoutId(): Int = R.layout.fragment_analytics_tab

    private var inflatedEmptyView: View? = null

    override fun onReadyView() {
        setupViews()
        setupTimeRangeTabs()
        setupWeightChart()
        setupObservers()
        viewModel.fetchUserData()
    }

    private fun setupViews() {
        binding.apply {
            btnUpdateGoal.setOnClickListener {
                findNavController().navigate(R.id.action_analytics_to_goal_weight_update)
            }

            btnUpdateWeight.setOnClickListener {
                findNavController().navigate(R.id.action_analytics_to_weight_update)
            }

            btnBmiInfo.setOnClickListener {
                openBmiInfoUrl()
            }

            tvLearnMore.setOnClickListener {
                openBmiInfoUrl()
            }
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.apply {
                tvGoalWeight.text = getString(
                    R.string.goal_weight,
                    "${userData.targetWeight} ${if (userData.isMetric == true) "kg" else "lb"}"
                )
                tvCurrentWeight.text = getString(
                    R.string.current_weight_param,
                    "${userData.weight?.toInt()} ${if (userData.isMetric == true) "kg" else "lb"}"
                )

                val bmi = viewModel.calculateBMI()
                tvBmiValue.text = String.format("%.1f", bmi)
                updateBmiCategory(bmi)
                updateBmiIndicator(bmi)
            }
        }

        viewModel.filteredWeightLogs.observe(viewLifecycleOwner) { logs ->
            updateChart(logs)
        }

        viewModel.completedRatio.observe(viewLifecycleOwner) { ratio ->
            binding.tvCompletedPercentage.text = getString(R.string.completed_percentage, "%$ratio")
            binding.tvCompletedPercentage.setTextColor(getProgressColor(ratio))
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state if needed
        }
    }

    private fun updateBmiCategory(bmi: Double) {
        binding.tvBmiCategory.apply {
            val (category, color) = when {
                bmi < 18.5 -> getString(R.string.underweight) to R.color.blue
                bmi < 25 -> getString(R.string.healthy) to R.color.green
                bmi < 30 -> getString(R.string.overweight) to R.color.orange
                else -> getString(R.string.obese) to R.color.red
            }
            text = category
            setTextColor(requireContext().getColor(color))
        }
    }

    private fun updateBmiIndicator(bmi: Double) {
        // Clamp BMI between 18.5 and 33 for the indicator position
        val clampedBmi = bmi.coerceIn(18.5, 33.0)
        val ratio = (clampedBmi - 18.5) / (33.0 - 18.5)

        binding.bmiProgressBackground.post {
            val progressWidth = binding.bmiProgressBackground.width
            val indicatorWidth = binding.bmiIndicator.width
            val offset = (progressWidth - indicatorWidth) * ratio
            binding.bmiIndicator.translationX = offset.toFloat()
        }
    }

    private fun openBmiInfoUrl() {
        val url = "https://www.who.int/news-room/fact-sheets/detail/obesity-and-overweight"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setupWeightChart() {
        binding.weightChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            setBackgroundColor(Color.parseColor("#323233"))

            xAxis.apply {
                textColor = Color.parseColor("#b8b8b9")
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textSize = 12f
                yOffset = 20f
                labelCount = 4
                spaceMin = 0.2f
                spaceMax = 0.2f
                valueFormatter = object : ValueFormatter() {
                    private val dateFormat = when (viewModel.selectedTimeRange.value) {
                        TimeRange.LAST_30_DAYS -> SimpleDateFormat("MMM d", Locale.getDefault())
                        else -> SimpleDateFormat("MMM yy", Locale.getDefault())
                    }

                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
            }

            axisLeft.isEnabled = false

            axisRight.apply {
                textColor = Color.parseColor("#b8b8b9")
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textSize = 12f
                xOffset = 15f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
                setLabelCount(5, true)
                granularity = 10f
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { entry ->
                        val date = Date(entry.x.toLong())
                        val weight = entry.y.toInt()
                        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        binding.tvWeightDate.text =
                            getString(R.string.weight_date_format, weight, dateFormat.format(date))
                    }
                }

                override fun onNothingSelected() {
                    binding.tvWeightDate.text = getString(R.string.drag_chart_hint)
                }
            })

            // Set minimum offsets for the chart
            setMinOffset(0f)

            // Significantly increase viewport offsets (left, top, right, bottom)
            setViewPortOffsets(80f, 30f, 80f, 90f)

            // Add extra padding
            extraLeftOffset = 20f
            extraRightOffset = 20f
            extraTopOffset = 20f
            extraBottomOffset = 25f

            isHighlightPerDragEnabled = false
            isHighlightPerTapEnabled = true
        }
    }

    private fun setupTimeRangeTabs() {
        with(binding.timeRangeTabs) {
            removeAllTabs()
            TimeRange.entries.forEach { timeRange ->
                addTab(newTab().setText(getString(timeRange.stringResId)))
            }

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        viewModel.setTimeRange(TimeRange.entries[it.position])
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun updateChart(weightLogs: List<WeightLogData>) {
        setupWeightProgressView(weightLogs.isEmpty())
        if (weightLogs.isEmpty()) {
            binding.weightChart.clear()
            binding.tvWeightDate.text = getString(R.string.drag_chart_hint)
            return
        }

        val entries = weightLogs.mapNotNull { log ->
            log.date?.let { date ->
                log.weight?.let { weight ->
                    Entry(date.time.toFloat(), weight.toFloat())
                }
            }
        }.sortedBy { it.x }

        if (entries.isEmpty()) {
            binding.weightChart.clear()
            binding.tvWeightDate.text = getString(R.string.drag_chart_hint)
            return
        }

        // Calculate min and max for better scaling
        val weights = entries.map { it.y }
        val minWeight = (weights.minOrNull() ?: 0f).toInt()
        val maxWeight = (weights.maxOrNull() ?: 0f).toInt()

        // Determine the interval based on the range
        val range = maxWeight - minWeight
        val interval = if (maxWeight >= 100) 100 else 10

        // Calculate proper range with appropriate intervals
        val rangeMin = (minWeight / interval) * interval // Round down to nearest interval
        val rangeMax =
            ((maxWeight + interval - 1) / interval) * interval // Round up to nearest interval

        // Ensure minimum range for proper display
        val minRange = if (interval == 100) 400 else 40
        val finalMin = if (rangeMax - rangeMin < minRange) rangeMax - minRange else rangeMin
        val finalMax = if (rangeMax - rangeMin < minRange) rangeMax else rangeMax

        val dataSet = LineDataSet(entries, "Weight").apply {
            color = Color.WHITE
            setDrawCircles(true)
            setDrawCircleHole(true)
            circleRadius = 4f
            circleHoleRadius = 2f
            circleColors = listOf(Color.WHITE)
            circleHoleColor = Color.parseColor("#323233") // Same as chart background
            lineWidth = 1.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER

            setDrawFilled(false)
            setDrawValues(false)
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(false)
            highLightColor = Color.TRANSPARENT
        }

        binding.weightChart.apply {
            data = LineData(dataSet)

            axisRight.apply {
                axisMinimum = finalMin.toFloat()
                axisMaximum = finalMax.toFloat()
                granularity = interval.toFloat()
                setLabelCount((finalMax - finalMin) / interval + 1, true)
                spaceTop = 20f
                spaceBottom = 20f
            }

            // Set viewport to show full height with padding
            setVisibleYRange(
                finalMin.toFloat() - interval,
                finalMax.toFloat() + interval,
                YAxis.AxisDependency.RIGHT
            )

            // Ensure the chart fills the available height
            setScaleMinima(1f, 1f)

            animateX(500)

            // Set minimum offsets for the chart
            setMinOffset(0f)

            // Significantly increase viewport offsets (left, top, right, bottom)
            setViewPortOffsets(80f, 30f, 80f, 90f)

            // Add extra padding
            extraLeftOffset = 20f
            extraRightOffset = 20f
            extraTopOffset = 20f
            extraBottomOffset = 25f

            // Force refresh
            invalidate()
        }
    }

    private fun getProgressColor(ratio: Int): Int {
        return when {
            ratio <= 50 -> {
                val hue = (ratio / 50f) * 60f
                Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
            }

            else -> {
                val hue = 60f + ((ratio - 50f) / 50f) * 60f
                Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
            }
        }
    }

    private fun setupWeightProgressView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyWeightProgress.root.visibility = View.VISIBLE
        } else {
            binding.emptyWeightProgress.root.visibility = View.GONE
        }
    }
}