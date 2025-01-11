package com.codepad.foodai.ui.streak

import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentDailyStreakBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeFragment
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class DailyStreakFragment : BaseFragment<FragmentDailyStreakBinding>() {
    private val viewModel: DailyStreakViewModel by viewModels()
    private val args: DailyStreakFragmentArgs by navArgs()
    override val hideBottomNavBar: Boolean = true

    override fun getLayoutId() = R.layout.fragment_daily_streak

    override fun onReadyView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.setStreakData(args.streakData)
        setupWeekCalendar()
        setupNavigation()
    }

    private fun setupNavigation() {
        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateBack()
        }

        // Handle continue button
        binding.continueButton.setOnClickListener {
            navigateBack()
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun setupWeekCalendar() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val dayText = view.findViewById<TextView>(R.id.day_text)
            val circleBackground = view.findViewById<View>(R.id.circle_background)
            lateinit var day: WeekDay
        }

        binding.weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.day = data
                val dayIndex = data.date.dayOfWeek.value % 7
                container.dayText.text = viewModel.getDaySymbol(dayIndex)

                viewModel.selectedDays.value?.let { selectedDays ->
                    val isSelected = selectedDays.getOrNull(dayIndex) ?: false
                    container.circleBackground.isSelected = isSelected
                    container.dayText.setTextColor(
                        requireContext().getColor(
                            if (isSelected) R.color.white else R.color.gray
                        )
                    )
                }
            }
        }

        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(12).atStartOfMonth()
        val endDate = currentMonth.plusMonths(12).atEndOfMonth()
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.weekCalendarView.setup(startDate, endDate, firstDayOfWeek)
        binding.weekCalendarView.scrollToWeek(currentDate)
    }
} 