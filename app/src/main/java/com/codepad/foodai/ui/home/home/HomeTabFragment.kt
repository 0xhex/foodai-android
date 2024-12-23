package com.codepad.foodai.ui.home.home

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.extensions.getFormattedDate
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.calendar.CalendarAdapter
import com.codepad.foodai.ui.home.home.calendar.CalendarUtils
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.codepad.foodai.ui.home.home.pager.ViewPagerAdapter
import com.codepad.foodai.ui.home.home.pager.goals.GoalViewFragment
import com.codepad.foodai.ui.home.home.pager.health.GoogleHealthFragment
import com.codepad.foodai.ui.home.home.pager.recipe.FoodRecipesFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding>() {
    private val viewModel: HomePagerViewModel by activityViewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedCalendarPosition: Pair<Int, Int>? = null
    private var selectedCalendarItem: Triple<Date, Int, String>? = null

    override fun getLayoutId(): Int = R.layout.fragment_home_tab

    override fun onReadyView() {
        setupCalendarView()
        setupViewPager()
    }

    private fun setupCalendarView() {
        val calendarView = binding.rvCalendar
        calendarView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val weeks = CalendarUtils.generateMonthDays()
        selectedCalendarPosition = CalendarUtils.findCurrentDayPosition(weeks)
        selectedCalendarItem = weeks[selectedCalendarPosition!!.first][selectedCalendarPosition!!.second]
        calendarAdapter =
            CalendarAdapter(weeks, selectedCalendarPosition) { mainPosition, subPosition, item ->
                selectedCalendarPosition = Pair(mainPosition, subPosition)
                selectedCalendarItem = item
                calendarAdapter.updateSelectedPosition(selectedCalendarPosition)
                fetchDataForSelectedDate(item.first)
            }
        calendarView.adapter = calendarAdapter

        PagerSnapHelper().attachToRecyclerView(calendarView)

        selectedCalendarPosition?.let {
            calendarView.scrollToPosition(it.first)
            fetchDataForSelectedDate(selectedCalendarItem?.first)
        }
    }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = binding.viewPager

        val fragments = listOf(
            GoalViewFragment(), GoogleHealthFragment(), FoodRecipesFragment()
        )

        viewPager.adapter = ViewPagerAdapter(requireActivity(), fragments)
        binding.dotsIndicator.attachTo(viewPager)
    }

    private fun fetchDataForSelectedDate(date: Date?) {
        date?.let {
            viewModel.fetchDailySummary(
                UserSession.user?.id.orEmpty(),
                getFormattedDate(it)
            )
        }
    }
}