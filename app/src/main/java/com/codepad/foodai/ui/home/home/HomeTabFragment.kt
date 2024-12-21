package com.codepad.foodai.ui.home.home

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.home.calendar.CalendarAdapter
import com.codepad.foodai.ui.home.home.calendar.CalendarUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedCalendarPosition: Pair<Int, Int>? = null
    private var selectedCalendarItem: Triple<Date, Int, String>? = null

    override fun getLayoutId(): Int = R.layout.fragment_home_tab

    override fun onReadyView() {
        setupCalendarView()
    }

    private fun setupCalendarView() {
        val calendarView = binding.rvCalendar
        calendarView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val weeks = CalendarUtils.generateMonthDays()
        selectedCalendarPosition = CalendarUtils.findCurrentDayPosition(weeks)
        calendarAdapter = CalendarAdapter(weeks, selectedCalendarPosition) { mainPosition, subPosition, item ->
            selectedCalendarPosition = Pair(mainPosition, subPosition)
            selectedCalendarItem = item
            calendarAdapter.updateSelectedPosition(selectedCalendarPosition)
        }
        calendarView.adapter = calendarAdapter

        PagerSnapHelper().attachToRecyclerView(calendarView)

        selectedCalendarPosition?.let {
            calendarView.scrollToPosition(it.first)
        }
    }
}