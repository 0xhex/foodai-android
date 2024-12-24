package com.codepad.foodai.ui.home.home

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.extensions.getFormattedDate
import com.codepad.foodai.extensions.toHourString
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.MenuOption
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
    private val sharedViewModel: HomeViewModel by activityViewModels()

    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedCalendarPosition: Pair<Int, Int>? = null
    private var selectedCalendarItem: Triple<Date, Int, String>? = null
    private lateinit var imageAdapter: ImageAdapter

    override fun getLayoutId(): Int = R.layout.fragment_home_tab

    override fun onReadyView() {
        setupCalendarView()
        setupViewPager()
        setupRecyclerView()

        sharedViewModel.homeEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is HomeViewModel.HomeEvent.OnMenuOptionSelected -> {
                    when (event.option) {
                        MenuOption.SCAN_FOOD -> {

                        }

                        MenuOption.LOG_FOOD -> {

                        }
                    }
                }

                is HomeViewModel.HomeEvent.OnImageUploadError -> {}
                HomeViewModel.HomeEvent.OnImageUploadStarted -> {}
                is HomeViewModel.HomeEvent.OnImageUploadSuccess -> {}
                is HomeViewModel.HomeEvent.OnImageFetchError -> {}
                is HomeViewModel.HomeEvent.OnImageFetchStarted -> {
                    val loadingItem = ImageItem.Loading(
                        image = event.bitmap,
                        statusMessages = listOf(
                            "Detecting ingredients...",
                            "Calculating nutritional values...",
                            "Finalizing your meal summary..."
                        )
                    )
                    val existingItems = imageAdapter.foodItems
                    if (existingItems.none { it is ImageItem.Loading }) {
                        imageAdapter.setItems(listOf(loadingItem) + existingItems)
                        updateEmptyViewVisibility()
                        startLoadingStatusUpdates()
                    }
                }

                is HomeViewModel.HomeEvent.OnImageFetchSuccess -> {
                    val existingItems = imageAdapter.foodItems
                    val items = listOf(
                        ImageItem.Standard(
                            image = event.response.url,
                            title = event.response.ingredients?.joinToString { it.name.orEmpty() }
                                .orEmpty(),
                            calories = event.response.calories.toString(),
                            protein = event.response.protein.toString(),
                            carb = event.response.carbs.toString(),
                            fats = event.response.fats.toString(),
                            hour = event.response.createdAt?.toHourString().toString()
                        )
                    )
                    imageAdapter.setItems((items + existingItems).filter { it !is ImageItem.Loading })
                    updateEmptyViewVisibility()
                    // findNavController().navigate(R.id.action_homeFragment_to_streakViewFragment)
                }
            }
        }

        viewModel.dailySummary.observe(viewLifecycleOwner) { dailySummary ->
            val meals = dailySummary.meals.orEmpty()
            val items = meals.sortedByDescending { it.createdAt }.map {
                ImageItem.Standard(
                    image = it.url,
                    title = it.ingredients?.joinToString { it.name.orEmpty() }.orEmpty(),
                    calories = it.calories.toString(),
                    protein = it.protein.toString(),
                    carb = it.carbs.toString(),
                    fats = it.fats.toString(),
                    hour = it.createdAt?.toHourString().orEmpty()
                )
            }
            imageAdapter.setItems(items)
            updateEmptyViewVisibility()
            sharedViewModel.fetchNutrition()
        }

        sharedViewModel.nutritions.observe(viewLifecycleOwner) { nutritionResponseData ->
            viewModel.updateAchievedPercents(nutritionResponseData)
        }
    }

    private fun setupCalendarView() {
        val calendarView = binding.rvCalendar
        calendarView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val weeks = CalendarUtils.generateMonthDays()
        selectedCalendarPosition = CalendarUtils.findCurrentDayPosition(weeks)
        selectedCalendarItem =
            weeks[selectedCalendarPosition!!.first][selectedCalendarPosition!!.second]
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
                UserSession.user?.id.orEmpty(), getFormattedDate(it)
            )
        }
    }

    private fun setupRecyclerView() {
        binding.rvFood.layoutManager = LinearLayoutManager(requireContext())
        imageAdapter = ImageAdapter(emptyList())
        binding.rvFood.adapter = imageAdapter
    }

    private fun updateEmptyViewVisibility() {
        if (imageAdapter.itemCount > 0) {
            binding.clEmptyView.visibility = View.GONE
        } else {
            binding.clEmptyView.visibility = View.VISIBLE
        }
    }

    private fun startLoadingStatusUpdates() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val loadingPosition =
                    imageAdapter.foodItems.indexOfFirst { it is ImageItem.Loading }
                if (loadingPosition != -1) {
                    imageAdapter.updateLoadingStatus(loadingPosition)
                    handler.postDelayed(this, 2000)
                }
            }
        }
        handler.post(runnable)
    }

}