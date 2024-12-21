package com.codepad.foodai.ui.home.home

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.home.calendar.CalendarAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var mainAdapter: CalendarAdapter
    private var selectedPosition: Pair<Int, Int>? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_home_tab
    }

    override fun onReadyView() {
        val calendarView = binding.rvCalendar
        calendarView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        generateMonthDays()
        mainAdapter = CalendarAdapter(generateMonthDays()) { mainPosition, subPosition ->
            selectedPosition = Pair(mainPosition, subPosition)
            mainAdapter.notifyDataSetChanged()
        }
        calendarView.adapter = mainAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(calendarView)

    }

    fun generateMonthDays(): List<List<Triple<Date, Int, String>>> {
        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
        val today = Calendar.getInstance()
        val monthDays = mutableListOf<Triple<Date, Int, String>>()

        for (offset in -26..0) {
            val date = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.DAY_OF_YEAR, offset)
            }.time
            val day = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()
            val weekdayIndex = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK) - 1
            monthDays.add(Triple(date, day, weekdays[weekdayIndex]))
        }

        val weeks = monthDays.chunked(7).toMutableList()

        // Add a future day if the last week has 6 days
        if (weeks.isNotEmpty() && weeks.last().size == 6) {
            val futureDate = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.DAY_OF_YEAR, 1)
            }.time
            val futureDay = SimpleDateFormat("d", Locale.getDefault()).format(futureDate).toInt()
            val futureWeekdayIndex = Calendar.getInstance().apply { time = futureDate }.get(Calendar.DAY_OF_WEEK) - 1
            weeks[weeks.size - 1] = weeks.last() + Triple(futureDate, futureDay, weekdays[futureWeekdayIndex])
        }

        return weeks
    }
}