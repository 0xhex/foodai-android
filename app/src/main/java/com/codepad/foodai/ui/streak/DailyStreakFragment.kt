package com.codepad.foodai.ui.streak

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentDailyStreakBinding
import com.codepad.foodai.ui.core.BaseFragment
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
        setupHeaderEffects()
        setupWeekCalendar()
        setupStreakEffects()
        setupNavigation()
    }

    private fun setupHeaderEffects() {
        // Create and apply gradient to app title
        val paint = binding.appTitle.paint
        paint.isFakeBoldText = true  // Force bold rendering
        val width = paint.measureText(binding.appTitle.text.toString())
        val textShader = LinearGradient(
            0f, 0f, width, binding.appTitle.textSize,
            intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.orange_start),
                ContextCompat.getColor(requireContext(), R.color.orange_end),
                ContextCompat.getColor(requireContext(), R.color.orange_start)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = textShader
    }

    private fun setupNavigation() {
        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateBack()
        }

        // Handle continue button with animation
        binding.continueButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            navigateBack()
                        }
                        .start()
                }
                .start()
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

            init {
                view.setOnClickListener {
                    // Optional: Add click animation
                    view.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(100)
                        .withEndAction {
                            view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                        }
                        .start()
                }
            }
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
                    
                    if (isSelected) {
                        container.dayText.setTextColor(Color.WHITE)
                        container.dayText.alpha = 1f
                    } else {
                        container.dayText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.gray)
                        )
                        container.dayText.alpha = 0.8f
                    }
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

    private fun setupStreakEffects() {
        // Setup flame icon animation
        val scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(
            binding.flameIcon,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.02f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.02f)
        ).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()
        }
        scaleAnimation.start()

        // Setup streak text gradient (similar to app title)
        val paint = binding.streakText.paint
        paint.isFakeBoldText = true  // Force bold rendering
        val width = paint.measureText(binding.streakText.text.toString())
        val textShader = LinearGradient(
            0f, 0f, width, binding.streakText.textSize,
            intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.orange_start),
                ContextCompat.getColor(requireContext(), R.color.orange_end),
                ContextCompat.getColor(requireContext(), R.color.orange_start)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = textShader

        // Calculate flame size based on streak count
        val baseSize = 200f
        val sizeFactor = viewModel.currentStreak.value?.toFloat()?.div(3f) ?: 0f
        val params = binding.flameIcon.layoutParams
        params.width = (baseSize * sizeFactor).toInt()
        params.height = (baseSize * sizeFactor).toInt()
        binding.flameIcon.layoutParams = params
    }
} 