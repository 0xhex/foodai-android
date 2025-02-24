package com.codepad.foodai.ui.home.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.domain.models.ErrorCode
import com.codepad.foodai.extensions.addIcon
import com.codepad.foodai.extensions.applyPaywallStyle
import com.codepad.foodai.extensions.applyStyle
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
import com.google.android.material.snackbar.Snackbar
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
        setupStreakView()
        setupEmptyView()

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

                is HomeViewModel.HomeEvent.OnImageUploadError -> {
                    val message = event.errorMessage
                    if (event.error?.errorCode == ErrorCode.DAILY_LIMIT_REACHED.code) {
                        showPaywallSnack(message)
                        sharedViewModel.clearErrorEvent()
                    } else {
                        showSnack(message)
                    }
                }

                HomeViewModel.HomeEvent.OnImageUploadStarted -> {}
                is HomeViewModel.HomeEvent.OnImageUploadSuccess -> {}
                is HomeViewModel.HomeEvent.OnImageFetchError -> {}
                is HomeViewModel.HomeEvent.OnImageFetchStarted -> {
                    val loadingItem = ImageItem.Loading(
                        image = event.bitmap,
                        statusMessages = listOf(
                            getString(R.string.detecting_ingredients),
                            getString(R.string.calculating_nutritional_values),
                            getString(R.string.finalizing_your_meal_summary)
                        )
                    )
                    val existingItems = imageAdapter.foodItems.filterNot { it is ImageItem.Loading }
                    imageAdapter.setItems(listOf(loadingItem) + existingItems)
                    updateEmptyViewVisibility()
                    startLoadingStatusUpdates()
                }

                is HomeViewModel.HomeEvent.OnImageFetchSuccess -> {
                    val existingItems = imageAdapter.foodItems.filterNot { it is ImageItem.Loading }
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
                    imageAdapter.setItems(items + existingItems)
                    updateEmptyViewVisibility()

                    if (sharedViewModel.shouldShowStreakView()) {
                        sharedViewModel.dailyStreak.value?.let { streak ->
                            findNavController().navigate(
                                HomeTabFragmentDirections.actionHomeTabToDailyStreakFragment(streak)
                            )
                        }
                    }
                }

                null -> {}
            }
        }

        viewModel.dailySummary.observe(viewLifecycleOwner) { dailySummary ->
            val meals = dailySummary.meals.orEmpty()
            val exercises = dailySummary.exercises.orEmpty()

            // Get any existing loading items
            val loadingItems = imageAdapter.foodItems.filterIsInstance<ImageItem.Loading>()

            // Create a list of all items with their timestamps
            val allItems = mutableListOf<Triple<Date?, ImageItem, Boolean>>()

            // Add meals with their timestamps
            meals.forEach { meal ->
                allItems.add(
                    Triple(
                        meal.createdAt,
                        ImageItem.Standard(
                            image = meal.url,
                            title = meal.ingredients?.joinToString { it.name.orEmpty() }.orEmpty(),
                            calories = meal.calories.toString(),
                            protein = meal.protein.toString(),
                            carb = meal.carbs.toString(),
                            fats = meal.fats.toString(),
                            hour = meal.createdAt?.toHourString().orEmpty()
                        ),
                        false // isLoading flag
                    )
                )
            }

            // Add exercises with their timestamps
            exercises.forEach { exercise ->
                allItems.add(
                    Triple(
                        exercise.createdAt,
                        ImageItem.Exercise(
                            exerciseData = exercise
                        ),
                        false // isLoading flag
                    )
                )
            }

            // Sort all items by timestamp (newest first)
            allItems.sortWith(compareByDescending<Triple<Date?, ImageItem, Boolean>> { it.first }
                .thenBy { it.third }) // Loading items go first if timestamps are equal

            // Combine loading items with sorted content items
            val finalItems = loadingItems + allItems.map { it.second }

            imageAdapter.setItems(finalItems)
            updateEmptyViewVisibility()
            sharedViewModel.fetchNutrition()
        }

        sharedViewModel.nutritions.observe(viewLifecycleOwner) { nutritionResponseData ->
            viewModel.updateAchievedPercents(nutritionResponseData)
        }

        sharedViewModel.dailyStreak.observe(viewLifecycleOwner) { streak ->
            binding.txtStreak.text = streak?.currentStreak?.toString() ?: "0"
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
        imageAdapter = ImageAdapter(
            foodItems = emptyList(),
            onItemClick = { item ->
                when (item) {
                    is ImageItem.Standard -> {
                        val foodDetail =
                            viewModel.dailySummary.value?.meals?.firstOrNull { it.url == item.image }
                        if (foodDetail != null) {
                            viewModel.setFoodDetail(foodDetail)
                            findNavController().navigate(R.id.action_home_tab_to_food_detail)
                        }
                    }

                    is ImageItem.Exercise -> {
                        when (item.exerciseData.exerciseType) {
                            "run", "weightlifting" -> {
                                val bundle = Bundle().apply {
                                    putParcelable("exerciseData", item.exerciseData)
                                }
                                findNavController().navigate(
                                    R.id.action_home_tab_to_exercise_detail,
                                    bundle
                                )
                            }

                            else -> {
                                val bundle = Bundle().apply {
                                    putParcelable("exerciseData", item.exerciseData)
                                }
                                findNavController().navigate(
                                    R.id.action_home_tab_to_describe_exercise_detail,
                                    bundle
                                )
                            }
                        }
                    }

                    else -> {
                        // Handle loading items if needed
                    }
                }
            }
        )
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

    private fun setupStreakView() {
        binding.cardStreak.setOnClickListener {
            sharedViewModel.dailyStreak.value?.let { streak ->
                findNavController().navigate(
                    HomeTabFragmentDirections.actionHomeTabToDailyStreakFragment(streak)
                )
            }
        }
    }

    private fun setupEmptyView() {
        binding.clEmptyView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_menuDialog)
        }
    }

    private fun showPaywallSnack(message: String) {
        val snack: Snackbar = Snackbar.make(
            binding.root, message, 3000
        )
        val iconPadding = resources.getDimensionPixelOffset(R.dimen.dimen_4dp)
        snack.addIcon(
            R.drawable.ic_sad,
            iconPadding,
            applyTint = true
        )
        snack.applyPaywallStyle()
        snack.show()
    }

    fun showSnack(message: String) {
        val snack: Snackbar = Snackbar.make(binding.root, message, 3000)
        val iconPadding = resources.getDimensionPixelOffset(R.dimen.dimen_4dp)
        val tintColor = resources.getColor(android.R.color.holo_red_dark, null)
        snack.addIcon(
            R.drawable.ic_close_circle, iconPadding, applyTint = true, tintColor = tintColor
        )
        snack.applyStyle()
        snack.show()
    }

}