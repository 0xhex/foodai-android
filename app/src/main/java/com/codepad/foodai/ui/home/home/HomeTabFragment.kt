package com.codepad.foodai.ui.home.home

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentHomeTabBinding
import com.codepad.foodai.databinding.ViewWeightUpdateBannerBinding
import com.codepad.foodai.domain.models.ErrorCode
import com.codepad.foodai.extensions.addIcon
import com.codepad.foodai.extensions.applyPaywallStyle
import com.codepad.foodai.extensions.applyStyle
import com.codepad.foodai.extensions.getFormattedDate
import com.codepad.foodai.extensions.toHourString
import com.codepad.foodai.helpers.FirebaseManager
import com.codepad.foodai.helpers.HealthConnectStatus
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.MenuOption
import com.codepad.foodai.ui.home.dialogs.RatingPrompt1Dialog
import com.codepad.foodai.ui.home.dialogs.RatingPrompt2Dialog
import com.codepad.foodai.ui.home.home.adapter.WaterGlassAdapter
import com.codepad.foodai.ui.home.home.calendar.CalendarAdapter
import com.codepad.foodai.ui.home.home.calendar.CalendarUtils
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.codepad.foodai.ui.home.home.pager.ViewPagerAdapter
import com.codepad.foodai.ui.home.home.pager.goals.GoalViewFragment
import com.codepad.foodai.ui.home.home.pager.health.GoogleHealthFragment
import com.codepad.foodai.ui.home.home.pager.recipe.FoodRecipesFragment
import com.codepad.foodai.ui.home.home.view.BodyWeightView
import com.codepad.foodai.ui.home.home.view.DailyNoteView
import com.codepad.foodai.ui.home.settings.HealthConnectManagerEntryPoint
import com.codepad.foodai.ui.home.settings.health.HealthConnectManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding>() {
    private val viewModel: HomePagerViewModel by activityViewModels()
    private val sharedViewModel: HomeViewModel by activityViewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var firebaseManager: FirebaseManager

    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedCalendarPosition: Pair<Int, Int>? = null
    private var selectedCalendarItem: Triple<Date, Int, String>? = null
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var waterGlassAdapter: WaterGlassAdapter
    private var selectedDate = Date()
    private var weightUpdateBanner: ViewWeightUpdateBannerBinding? = null

    private val playReviewManager by lazy {
        ReviewManagerFactory.create(requireContext())
    }

    private var ratingProbability: Double
        get() = sharedPreferences.getFloat("ratingProbability", 1.0f).toDouble()
        set(value) = sharedPreferences.edit { putFloat("ratingProbability", value.toFloat()) }

    private var appRated: Boolean
        get() = sharedPreferences.getBoolean("appRated", false)
        set(value) = sharedPreferences.edit { putBoolean("appRated", value) }

    override fun getLayoutId(): Int = R.layout.fragment_home_tab

    override fun onReadyView() {
        setupCalendarView()
        setupViewPager()
        setupRecyclerView()
        setupStreakView()
        setupEmptyView()
        setupDailyMissions()

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
                        image = event.bitmap, statusMessages = listOf(
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
                    val items = listOf(ImageItem.Standard(image = event.response.url,
                        title = event.response.ingredients?.joinToString { it.name.orEmpty() }
                            .orEmpty(),
                        calories = event.response.calories.toString(),
                        protein = event.response.protein.toString(),
                        carb = event.response.carbs.toString(),
                        fats = event.response.fats.toString(),
                        hour = event.response.createdAt?.toHourString().toString()))
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
                        meal.createdAt, ImageItem.Standard(
                            image = meal.url,
                            title = meal.ingredients?.joinToString { it.name.orEmpty() }.orEmpty(),
                            calories = meal.calories.toString(),
                            protein = meal.protein.toString(),
                            carb = meal.carbs.toString(),
                            fats = meal.fats.toString(),
                            hour = meal.createdAt?.toHourString().orEmpty()
                        ), false // isLoading flag
                    )
                )
            }

            // Add exercises with their timestamps
            exercises.forEach { exercise ->
                allItems.add(
                    Triple(
                        exercise.createdAt, ImageItem.Exercise(
                            exerciseData = exercise
                        ), false // isLoading flag
                    )
                )
            }

            // Sort all items by timestamp (newest first)
            allItems.sortWith(compareByDescending<Triple<Date?, ImageItem, Boolean>> { it.first }.thenBy { it.third }) // Loading items go first if timestamps are equal

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

        viewModel.showWeightUpdateBanner.observe(viewLifecycleOwner) { show ->
            if (show) {
                showWeightUpdateBanner()
            } else {
                hideWeightUpdateBanner()
            }
        }

        checkForRateUs()
    }

    private fun setupCalendarView() {
        val calendarView = binding.rvCalendar
        calendarView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val weeks = CalendarUtils.generateMonthDays()
        selectedCalendarPosition = CalendarUtils.findCurrentDayPosition(weeks)
        selectedCalendarItem =
            weeks[selectedCalendarPosition!!.first][selectedCalendarPosition!!.second]
        calendarAdapter = CalendarAdapter(weeks,
            selectedCalendarPosition,
            onSubItemSelected = { mainPosition, subPosition, item ->
                selectedCalendarPosition = Pair(mainPosition, subPosition)
                selectedCalendarItem = item
                calendarAdapter.updateSelectedPosition(selectedCalendarPosition)
                fetchDataForSelectedDate(item.first)

                selectedDate = item.first
                viewModel.getWaterIntakeForDate(selectedDate)
                viewModel.loadNoteForDate(selectedDate)
            })

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
        imageAdapter = ImageAdapter(foodItems = emptyList(), onItemClick = { item ->
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
                                R.id.action_home_tab_to_exercise_detail, bundle
                            )
                        }

                        else -> {
                            val bundle = Bundle().apply {
                                putParcelable("exerciseData", item.exerciseData)
                            }
                            findNavController().navigate(
                                R.id.action_home_tab_to_describe_exercise_detail, bundle
                            )
                        }
                    }
                }

                else -> {
                    // Handle loading items if needed
                }
            }
        })
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
            sharedViewModel.launchFoodLogDialog.value = true
        }
    }

    private fun showPaywallSnack(message: String) {
        val snack: Snackbar = Snackbar.make(
            binding.root, message, 3000
        )
        val iconPadding = resources.getDimensionPixelOffset(R.dimen.dimen_4dp)
        snack.addIcon(
            R.drawable.ic_sad, iconPadding, applyTint = true
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

    private fun setupDailyMissions() {
        setupWaterIntake()
        setupDailySteps()
        setupBodyWeight()
        setupDailyNote()
    }

    private fun setupWaterIntake() {
        val binding = binding.dailyWaterIntakeView

        waterGlassAdapter = WaterGlassAdapter(
            glassCount = 18, currentGlasses = 0
        ) { position ->
            viewModel.incrementWaterIntake(selectedDate)
        }

        binding.rvWaterGlasses.apply {
            layoutManager = GridLayoutManager(context, 6)
            adapter = waterGlassAdapter
        }

        // Load initial water intake data
        viewModel.getWaterIntakeForDate(selectedDate)

        viewModel.waterIntake.observe(viewLifecycleOwner) { glasses ->
            waterGlassAdapter.updateCurrentGlasses(glasses)
            binding.txtCurrentWater.text = String.format(" Current: %.2f L", glasses * 0.25)

            viewModel.targetWaterIntake.value?.let { target ->
                val progress = (glasses * 0.25 / target * 100).toInt()
                binding.progressWater.progress = progress
            }
        }

        viewModel.targetWaterIntake.observe(viewLifecycleOwner) { target ->
            binding.txtTargetWater.text = String.format("Target: %.2f L", target)
        }

        UserSession.user?.let { user ->
            viewModel.calculateTargetWaterIntake(user)
        }
    }

    private fun setupDailySteps() {
        val binding = binding.dailyStepsView

        if (viewModel.isHealthConnectSupported() || viewModel.isHealthConnectSDKAvailable(
                requireContext()
            )
        ) {
            val entryPoint = EntryPointAccessors.fromApplication(
                requireContext(), HealthConnectManagerEntryPoint::class.java
            )
            healthConnectManager = entryPoint.getHealthConnectManager()
            healthConnectManager.initContent(this)
            viewModel.initHealthConnect(healthConnectManager)
            viewModel.checkHealthConnectStatus(healthConnectManager) {
                // Connected callback
            }
        }

        binding.notConnectedView.btnAuthorize.setOnClickListener {
            if (viewModel.isHealthConnectSupported() || viewModel.isHealthConnectSDKAvailable(
                    requireContext()
                )
            ) {
                viewModel.requestHealthConnect(healthConnectManager)
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

        viewModel.healthConnectStatus.observe(viewLifecycleOwner) { status ->
            binding.connectedView.isVisible = status == HealthConnectStatus.CONNECTED
            binding.notConnectedView.root.isVisible = status != HealthConnectStatus.CONNECTED
        }

        viewModel.currentSteps.observe(viewLifecycleOwner) { steps ->
            binding.txtStepsCount.text = "$steps/${viewModel.targetSteps.value ?: 0}"

            viewModel.targetSteps.value?.let { target ->
                val progress = (steps.toFloat() / target * 100).toInt()
                binding.progressSteps.progress = progress
            }

            val distance = viewModel.stepsDistance.value ?: 0.0
            val calories = viewModel.stepsBurnedCalories.value ?: 0
        }

        UserSession.user?.let { user ->
            viewModel.calculateTargetSteps(user)
        }
    }

    private fun setupBodyWeight() {
        UserSession.user?.let { user ->
            (binding.bodyWeightView as BodyWeightView).apply {
                updateWeight(
                    currentWeight = user.weight ?: 70.0,
                    targetWeight = user.targetWeight ?: 70,
                    isMetric = user.isMetric ?: true
                )

                setOnWeightChangeListener(
                    onIncrease = { newWeight ->
                        // Send update to backend
                        viewModel.updateWeight(newWeight)
                    },
                    onDecrease = { newWeight ->
                        // Send update to backend
                        viewModel.updateWeight(newWeight)
                    }
                )
            }
        }
    }

    private fun setupDailyNote() {
        (binding.dailyNoteView as DailyNoteView).apply {
            // Clear any existing note first
            updateNote(null)

            setOnStartJournalingClickListener {
                navigateToNoteEditor()
            }

            setOnNoteClickListener {
                navigateToNoteEditor()
            }

            // Observe note changes for the selected date
            viewModel.currentNote.observe(viewLifecycleOwner) { note ->
                // Ensure we're on the main thread
                view?.post {
                    updateNote(note)
                }
            }

            // Load note for initial date
            viewModel.loadNoteForDate(selectedDate)
        }
    }

    private fun navigateToNoteEditor() {
        findNavController().navigate(
            HomeTabFragmentDirections.actionHomeTabToNoteEditor(selectedDate)
        )
    }

    private fun navigateToWeightUpdate() {
        findNavController().navigate(R.id.action_home_tab_to_weight_update)
    }

    override fun onResume() {
        super.onResume()
        // Refresh both water and note data using same selectedDate
        viewModel.getWaterIntakeForDate(selectedDate)
        viewModel.loadNoteForDate(selectedDate)

        // Existing health connect check
        if (viewModel.isHealthConnectSupported() || viewModel.isHealthConnectSDKAvailable(
                requireContext()
            )
        ) {
            viewModel.checkHealthConnectStatus(healthConnectManager) {
                // Connected callback
            }
        }
    }

    private fun showWeightUpdateBanner() {
        if (weightUpdateBanner == null) {
            weightUpdateBanner = ViewWeightUpdateBannerBinding.inflate(layoutInflater)
            weightUpdateBanner?.apply {
                root.alpha = 0f
                root.translationY = -100f

                btnLater.setOnClickListener { hideWeightUpdateBanner() }
                btnUpdateNow.setOnClickListener {
                    hideWeightUpdateBanner()
                    navigateToWeightUpdate()
                }

                // Add banner to the top of the screen
                (binding.root as ViewGroup).addView(root, 0)

                // Animate banner in
                root.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()

                // Auto dismiss after 12 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    hideWeightUpdateBanner()
                }, 12000)
            }
        }
    }

    private fun hideWeightUpdateBanner() {
        weightUpdateBanner?.let { banner ->
            banner.root.animate()
                .alpha(0f)
                .translationY(-100f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    (binding.root as ViewGroup).removeView(banner.root)
                    weightUpdateBanner = null
                }
                .start()
        }
    }

    private fun checkForRateUs() {
        if (!appRated) {
            viewModel.dailySummary.observe(viewLifecycleOwner) { summary ->
                if (!summary.meals.isNullOrEmpty()) {
                    val randomNumber = Random.nextDouble()
                    if (randomNumber < ratingProbability) {
                        if (Random.nextDouble() < 0.5) {
                            showRatingPrompt1()
                        } else {
                            showRatingPrompt2()
                        }
                    }
                }
            }
        }
    }

    private fun showRatingPrompt1() {
        RatingPrompt1Dialog().apply {
            onYesClicked = {
                openPlayStoreRating()
                appRated = true
                firebaseManager.logEvent("yes_rating1_prompt")
                dismiss()
            }
            onNoClicked = {
                ratingProbability *= 0.5
                firebaseManager.logEvent("no_rating1_prompt")
                dismiss()
            }
        }.show(childFragmentManager, "RatingPrompt1")
    }

    private fun showRatingPrompt2() {
        RatingPrompt2Dialog().apply {
            onRateNowClicked = {
                openPlayStoreRating()
                appRated = true
                firebaseManager.logEvent("yes_rating2_prompt")
                dismiss()
            }
            onNotNowClicked = {
                ratingProbability *= 0.5
                firebaseManager.logEvent("no_rating2_prompt")
                dismiss()
            }
        }.show(childFragmentManager, "RatingPrompt2")
    }

    private fun openPlayStoreRating() {
        val request = playReviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                playReviewManager.launchReviewFlow(requireActivity(), reviewInfo)
            }
        }
    }
}