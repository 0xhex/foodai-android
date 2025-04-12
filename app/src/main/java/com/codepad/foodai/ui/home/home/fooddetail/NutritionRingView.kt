package com.codepad.foodai.ui.home.home.fooddetail

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.image.ImageData
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.random.Random

class NutritionRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Constants for colors matching iOS design
    private val PROTEIN_COLOR = R.color.proteinColor // Protein is red color
    private val CARBS_COLOR = R.color.carbsColor // Carbs are orange
    private val FATS_COLOR = R.color.fatsColor // Fats are blue
    
    private lateinit var pieChart: PieChart
    private lateinit var caloriesValue: TextView
    private lateinit var caloriesLabel: TextView
    private lateinit var editIcon: ImageView
    
    // MacroCard Views
    private lateinit var proteinCard: CardView
    private lateinit var carbsCard: CardView
    private lateinit var fatsCard: CardView
    
    // MacroCard TextViews
    private lateinit var proteinValue: TextView
    private lateinit var carbsValue: TextView
    private lateinit var fatsValue: TextView
    
    private var currentHighlightedMacro: String? = null
    private var animationSequenceRunning = false
    private var currentFoodDetail: ImageData? = null
    private var particles = mutableListOf<Particle>()
    private var particleAnimator: ValueAnimator? = null
    private val particleCount = 25 // Increased particle count for better effect
    private val handler = Handler(Looper.getMainLooper())
    
    // Animation variables for the highlight effect
    private var highlightProtein = false
    private var highlightCarbs = false
    private var highlightFats = false
    private var hasRunInitialAnimation = false
    private val highlightRunnable = Runnable { runConnectionAnimation() }
    
    // Store the data set instance to access it for animation
    private var currentDataSet: PieDataSet? = null
    
    private val particleView = object : View(context) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(160, 160, 200) // Lighter blue-white color for particles
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            particles.forEach { particle ->
                paint.alpha = (particle.alpha * 255).toInt()
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            }
            invalidate()
        }
    }

    init {
        orientation = HORIZONTAL
        initView()
        // We'll initialize particles in onSizeChanged instead of here
        // to ensure we have valid dimensions
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_nutrition_ring, this, true)
        setBackgroundColor(ContextCompat.getColor(context, R.color.ios_background))
        
        pieChart = findViewById(R.id.nutrition_pie_chart)
        caloriesValue = findViewById(R.id.calories_value)
        caloriesLabel = findViewById(R.id.calories_label)
        editIcon = findViewById(R.id.edit_icon)
        
        proteinValue = findViewById(R.id.protein_value)
        carbsValue = findViewById(R.id.carbs_value)
        fatsValue = findViewById(R.id.fats_value)
        
        proteinCard = findViewById(R.id.protein_card)
        carbsCard = findViewById(R.id.carbs_card)
        fatsCard = findViewById(R.id.fats_card)
        
        // Make sure the chart container is visible
        val chartContainer = findViewById<FrameLayout>(R.id.chart_container)
        chartContainer.visibility = View.VISIBLE
        
        setupPieChart()
        setupClickListeners()
        
        // Add the particle view behind the pie chart
        chartContainer.addView(particleView, 0)
    }

    private fun setupPieChart() {
        pieChart.apply {
            visibility = View.VISIBLE
            setUsePercentValues(false)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 80f // Further reduced hole radius for even thicker ring
            transparentCircleRadius = 80f // Match transparent circle to hole
            setDrawCenterText(false)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            setTouchEnabled(false)
            isRotationEnabled = false
            isHighlightPerTapEnabled = false
            setExtraOffsets(0f, 0f, 0f, 0f) // Remove offsets for maximum chart size
            minimumWidth = 180 // Add minimum width to ensure visibility
            minimumHeight = 180 // Add minimum height to ensure visibility
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClickListeners() {
        // Calories circle click handler
        findViewById<View>(R.id.calories_container).setOnClickListener {
            currentFoodDetail?.let { food ->
                navigateToEditCalories(food)
            }
        }
        
        // Protein card click handler 
        proteinCard.setOnClickListener {
            currentFoodDetail?.let { food -> 
                navigateToEditProtein(food)
            }
        }
        
        // Carbs card click handler
        carbsCard.setOnClickListener {
            currentFoodDetail?.let { food ->
                navigateToEditCarbs(food)
            }
        }
        
        // Fats card click handler
        fatsCard.setOnClickListener {
            currentFoodDetail?.let { food ->
                navigateToEditFats(food)
            }
        }
    }
    
    private fun navigateToEditCalories(foodDetail: ImageData) {
        // Create navigation bundle for EditCaloriesFragment
        val bundle = Bundle().apply {
            putString("title", "Calories")
            putString("textAreaTitle", "Enter Calories")
            putString("nutritionType", "calories")
            putString("value", foodDetail.calories?.toString() ?: "0")
            putInt("color", ContextCompat.getColor(context, R.color.carbsColor))
        }
        findNavController().navigate(R.id.action_navigation_new_food_detail_to_fixResultFragment, bundle)
    }
    
    private fun navigateToEditProtein(foodDetail: ImageData) {
        // Create navigation bundle for EditCaloriesFragment
        val bundle = Bundle().apply {
            putString("title", "Protein")
            putString("textAreaTitle", "Enter Protein")
            putString("nutritionType", "protein")
            putString("value", foodDetail.protein?.toString() ?: "0")
            putInt("color", ContextCompat.getColor(context, R.color.proteinColor))
        }
        findNavController().navigate(R.id.action_navigation_new_food_detail_to_fixResultFragment, bundle)
    }
    
    private fun navigateToEditCarbs(foodDetail: ImageData) {
        // Create navigation bundle for EditCaloriesFragment
        val bundle = Bundle().apply {
            putString("title", "Carbs")
            putString("textAreaTitle", "Enter Carbs")
            putString("nutritionType", "carbs")
            putString("value", foodDetail.carbs?.toString() ?: "0")
            putInt("color", ContextCompat.getColor(context, R.color.carbsColor))
        }
        findNavController().navigate(R.id.action_navigation_new_food_detail_to_fixResultFragment, bundle)
    }
    
    private fun navigateToEditFats(foodDetail: ImageData) {
        // Create navigation bundle for EditCaloriesFragment
        val bundle = Bundle().apply {
            putString("title", "Fats")
            putString("textAreaTitle", "Enter Fats")
            putString("nutritionType", "fats")
            putString("value", foodDetail.fats?.toString() ?: "0")
            putInt("color", ContextCompat.getColor(context, R.color.fatsColor))
        }
        findNavController().navigate(R.id.action_navigation_new_food_detail_to_fixResultFragment, bundle)
    }
    
    fun updateWithNutritionData(foodDetail: ImageData) {
        currentFoodDetail = foodDetail
        
        // Make sure the chart container is visible
        findViewById<FrameLayout>(R.id.chart_container).visibility = View.VISIBLE
        pieChart.visibility = View.VISIBLE
        
        // Update text values
        caloriesValue.text = foodDetail.calories?.toString() ?: "0"
        proteinValue.text = "${foodDetail.protein}g"
        carbsValue.text = "${foodDetail.carbs}g"
        fatsValue.text = "${foodDetail.fats}g"
        
        // First animation: show the pie chart animation (1000ms duration)
        // This matches the iOS animation which shows rings first with easeOut(duration: 1.0)
        updatePieChart(foodDetail)
        
        // Delay the card animation to match iOS timing (.easeOut(duration: 0.8).delay(0.3))
        handler.postDelayed({
            // Second animation: slide in the macro cards
            animateMacroCardsAppearance()
        }, 300)
        
        // Run the connection animation if not run yet, with exact iOS timing delay
        if (!hasRunInitialAnimation) {
            hasRunInitialAnimation = true
            
            // Set up the highlight animation with proper delay:
            // iOS uses initial delay of 0.7s after view appears
            // The total sequence should be: 
            // Ring animation (1.0s) + Additional delay before highlighting (0.7s)
            handler.postDelayed(highlightRunnable, 1700) // 1000ms + 700ms
        }
    }
    
    private fun updatePieChart(foodDetail: ImageData) {
        val protein = foodDetail.protein?.toFloat() ?: 0f
        val carbs = foodDetail.carbs?.toFloat() ?: 0f
        val fats = foodDetail.fats?.toFloat() ?: 0f
        val total = protein + carbs + fats
        
        // Create data entries with small gaps between segments
        val entries = mutableListOf<PieEntry>()
        
        if (total <= 0) {
            // If there's no data, just show empty ring
            entries.add(PieEntry(1f, "Empty"))
            val dataSet = PieDataSet(entries, "Nutrition")
            dataSet.colors = listOf(ContextCompat.getColor(context, R.color.color_graph_initial))
            dataSet.setDrawValues(false)
            dataSet.sliceSpace = 0f
            dataSet.selectionShift = 3f
            
            val pieData = PieData(dataSet)
            pieChart.data = pieData
            pieChart.invalidate()
            return
        }
        
        val segmentGap = 0.0f // Reduced gap between segments
        var totalWithGaps = 0f
        
        if (protein > 0) {
            entries.add(PieEntry(protein, "Protein"))
            totalWithGaps += protein
        }
        
        if (carbs > 0) {
            if (entries.isNotEmpty()) {
                entries.add(PieEntry(segmentGap, "Gap1"))
                totalWithGaps += segmentGap
            }
            entries.add(PieEntry(carbs, "Carbs"))
            totalWithGaps += carbs
        }
        
        if (fats > 0) {
            if (entries.isNotEmpty()) {
                entries.add(PieEntry(segmentGap, "Gap2"))
                totalWithGaps += segmentGap
            }
            entries.add(PieEntry(fats, "Fats"))
            totalWithGaps += fats
        }
        
        val dataSet = PieDataSet(entries, "Nutrition")
        currentDataSet = dataSet
        
        // Define colors based on the entry labels
        val colors = entries.map { entry ->
            when (entry.label) {
                "Protein" -> ContextCompat.getColor(context, PROTEIN_COLOR)
                "Carbs" -> ContextCompat.getColor(context, CARBS_COLOR)
                "Fats" -> ContextCompat.getColor(context, FATS_COLOR)
                else -> Color.TRANSPARENT // For gaps
            }
        }
        
        dataSet.colors = colors
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 0f
        dataSet.selectionShift = 3f
        
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        
        // Match iOS animation: easeOut(duration: 1.0)
        // 1. First set all segments to 0
        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i)
            val originalValue = entry.y
            entry.y = 0f // Set to 0 initially
            // Store original value for animation
            entry.data = originalValue
        }
        
        // Invalidate to redraw with empty values
        pieChart.invalidate()
        
        // Animate to actual values (without delay) - matches iOS behavior
        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i)
            entry.y = (entry.data as Float)
        }
        
        // Animate with 1 second duration, EaseOutQuad to match iOS .easeOut behavior
        pieChart.animateY(1400, Easing.EaseOutQuad)
    }
    
    private fun initParticles() {
        // Check if the view has a valid size, otherwise delay initialization
        if (width <= 0 || height <= 0) {
            // Post to handler to initialize after layout is ready
            post { initParticles() }
            return
        }
        
        particles.clear()
        for (i in 0 until particleCount) {
            particles.add(createRandomParticle())
        }
    }
    
    private fun createRandomParticle(): Particle {
        // The particles should be centered around the ring chart area, but moved to the left
        val centerX = width / 4f
        val centerY = height / 2f
        
        // Randomly generate particles across the entire chart area
        val x = Random.nextFloat() * (width / 2.5f)
        val y = Random.nextFloat() * height
        
        val particleSize = Random.nextFloat() * 2f + 0.8f  // Slightly smaller particles
        val alpha = Random.nextFloat() * 0.25f + 0.1f  // Reduced opacity for lighter effect
        
        // Random velocity in both directions
        val vx = (Random.nextFloat() * 0.5f - 0.25f) 
        val vy = (Random.nextFloat() * 0.5f - 0.25f)
        
        return Particle(x, y, particleSize, alpha, vx, vy)
    }
    
    private fun startParticleAnimation() {
        // Cancel any existing animation
        particleAnimator?.cancel()
        
        // If view isn't properly sized yet, skip animation
        if (width <= 0 || height <= 0 || particles.isEmpty()) {
            return
        }
        
        particleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 20  // Reduced from 40 to make animation faster
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            
            addUpdateListener {
                // Move particles in random directions
                for (i in particles.indices) {
                    val particle = particles[i]
                    
                    // Update position based on velocity - increased speed multiplier
                    particle.x += particle.vx * 1.5f
                    particle.y += particle.vy * 1.5f
                    
                    // Small random adjustment to velocity (Brownian motion) - increased for more dynamic movement
                    particle.vx += (Random.nextFloat() - 0.5f) * 0.15f
                    particle.vy += (Random.nextFloat() - 0.5f) * 0.15f
                    
                    // Dampen velocity slightly to prevent extreme speeds - reduced damping
                    particle.vx *= 0.98f
                    particle.vy *= 0.98f
                    
                    // Bounds checking - bounce off edges with slight randomization
                    if (particle.x < 0) {
                        particle.x = 0f
                        particle.vx = Math.abs(particle.vx) * (0.8f + Random.nextFloat() * 0.4f)
                    } else if (particle.x > width / 2f) {
                        particle.x = width / 2f
                        particle.vx = -Math.abs(particle.vx) * (0.8f + Random.nextFloat() * 0.4f)
                    }
                    
                    if (particle.y < 0) {
                        particle.y = 0f
                        particle.vy = Math.abs(particle.vy) * (0.8f + Random.nextFloat() * 0.4f)
                    } else if (particle.y > height.toFloat()) {
                        particle.y = height.toFloat()
                        particle.vy = -Math.abs(particle.vy) * (0.8f + Random.nextFloat() * 0.4f)
                    }
                    
                    // Randomly change alpha for twinkling effect - increased probability for more dynamic changes
                    if (Random.nextFloat() < 0.03) {
                        particle.alpha = Random.nextFloat() * 0.3f + 0.05f
                    }
                }
                particleView.invalidate()
            }
        }
        
        particleAnimator?.start()
    }
    
    private fun runConnectionAnimation() {
        // Match exact iOS animation timing parameters
        val initialDelay = 0L // We use 0 here since we already added the right delay when scheduling this runnable
        val animationDuration = 500L // Match iOS timing (0.5 seconds)
        val stateDisplayDuration = 550L // Match iOS timing (0.55 seconds)
        val transitionPause = 40L // Match iOS timing (0.04 seconds)
        
        // 1. Highlight protein with haptic feedback
        performHapticFeedback()
        animateHighlight(proteinCard, true, "Protein")
        
        // 2. End protein highlight after display duration
        handler.postDelayed({
            animateHighlight(proteinCard, false, null)
            
            // 3. Pause slightly before carbs highlight
            handler.postDelayed({
                // Highlight carbs with haptic feedback
                performHapticFeedback()
                animateHighlight(carbsCard, true, "Carbs")
                
                // 4. End carbs highlight after display duration
                handler.postDelayed({
                    animateHighlight(carbsCard, false, null)
                    
                    // 5. Pause slightly before fats highlight
                    handler.postDelayed({
                        // Highlight fats with haptic feedback
                        performHapticFeedback()
                        animateHighlight(fatsCard, true, "Fats")
                        
                        // 6. End fats highlight after display duration
                        handler.postDelayed({
                            animateHighlight(fatsCard, false, null)
                        }, stateDisplayDuration)
                    }, transitionPause)
                }, stateDisplayDuration)
            }, transitionPause)
        }, stateDisplayDuration)
    }
    
    private fun animateHighlight(card: CardView, highlight: Boolean, macroType: String?) {
        // iOS uses .easeInOut animation for all highlights
        // and duration of 0.5s for the transition
        
        // If highlighting (turning on), highlight the pie chart segment
        if (highlight) {
            // Also highlight the corresponding pie chart segment
            highlightPieChartSegment(macroType)
            
            // Apply a better glow effect based on the macro type - requires Android P (API 28+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                when (macroType) {
                    "Protein" -> {
                        // Enhanced protein glow
                        card.outlineProvider = ViewOutlineProvider.BACKGROUND
                        card.outlineSpotShadowColor = Color.parseColor("#5A6BFF")
                        card.outlineAmbientShadowColor = Color.parseColor("#5A6BFF")
                    }
                    "Carbs" -> {
                        // Enhanced carbs glow
                        card.outlineProvider = ViewOutlineProvider.BACKGROUND
                        card.outlineSpotShadowColor = Color.parseColor("#FF9500")
                        card.outlineAmbientShadowColor = Color.parseColor("#FF9500")
                    }
                    "Fats" -> {
                        // Enhanced fats glow
                        card.outlineProvider = ViewOutlineProvider.BACKGROUND
                        card.outlineSpotShadowColor = Color.parseColor("#4285F4")
                        card.outlineAmbientShadowColor = Color.parseColor("#4285F4")
                    }
                }
            }
        } else {
            // Reset pie chart highlights
            resetPieChartHighlights()
            
            // Reset glow effect - requires Android P (API 28+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                card.outlineProvider = ViewOutlineProvider.BACKGROUND
                card.outlineSpotShadowColor = Color.BLACK
                card.outlineAmbientShadowColor = Color.BLACK
            }
        }
        
        // Scale animation - match iOS exact scale factor of 1.08
        val scaleValue = if (highlight) 1.08f else 1.0f
        
        // Use Object Animator for smoother iOS-like animation
        ObjectAnimator.ofFloat(card, "scaleX", scaleValue).apply {
            duration = 350 // Match iOS timing
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        ObjectAnimator.ofFloat(card, "scaleY", scaleValue).apply {
            duration = 350 // Match iOS timing
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Elevation animation - adds depth similar to iOS shadow effect
        val elevationValue = if (highlight) 16f else 4f
        ObjectAnimator.ofFloat(card, "cardElevation", elevationValue).apply {
            duration = 350
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
    
    private fun highlightPieChartSegment(macroType: String?) {
        // Find the segment for the given macro type in the current data set
        currentDataSet?.let { dataSet ->
            // Find index of the macro in the data set
            var index = -1
            
            for (i in 0 until dataSet.entryCount) {
                val entry = dataSet.getEntryForIndex(i)
                if (entry.label == macroType) {
                    index = i
                    break
                }
            }
            
            if (index >= 0) {
                // Use the MPAndroidChart highlight mechanism
                pieChart.highlightValue(index.toFloat(), 0, false)
                
                // Animate the pie chart scaling to match iOS effect
                // iOS uses a subtle scale animation for the highlighted segment
                ObjectAnimator.ofFloat(pieChart, "scaleX", 1.0f, 1.05f).apply {
                    duration = 300 // Match iOS timing
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = 1
                    interpolator = AccelerateDecelerateInterpolator() 
                    start()
                }
                
                ObjectAnimator.ofFloat(pieChart, "scaleY", 1.0f, 1.05f).apply {
                    duration = 300 // Match iOS timing
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = 1
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }
        }
    }
    
    private fun resetPieChartHighlights() {
        pieChart.highlightValues(null)
    }
    
    private fun performHapticFeedback() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator)?.vibrate(
                android.os.VibrationEffect.createOneShot(20, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator)?.vibrate(20)
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        particleAnimator?.cancel()
        handler.removeCallbacks(highlightRunnable)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Initialize particles only when we have valid dimensions
        if (w > 0 && h > 0) {
            // Make sure chart is visible
            findViewById<FrameLayout>(R.id.chart_container).visibility = View.VISIBLE
            pieChart.visibility = View.VISIBLE
            
            initParticles()
            startParticleAnimation()
        }
    }
    
    private fun animateMacroCardsAppearance() {
        // First ensure the chart container is visible
        findViewById<FrameLayout>(R.id.chart_container).visibility = View.VISIBLE
        pieChart.visibility = View.VISIBLE
        
        // Make sure calories container is visible
        findViewById<FrameLayout>(R.id.calories_container).visibility = View.VISIBLE
        
        // Reset cards to initial state for animation
        proteinCard.alpha = 0f
        proteinCard.translationX = 20f
        
        carbsCard.alpha = 0f
        carbsCard.translationX = 20f
        
        fatsCard.alpha = 0f
        fatsCard.translationX = 20f
        
        // Match iOS spring animation with proper delays between cards
        // iOS: Animation.spring(response: 0.6, dampingFraction: 0.8).delay(animationDelay)
        
        // Animate protein card first - 0.2s delay
        proteinCard.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(600) // match iOS 0.6s spring response time
            .setInterpolator(AccelerateDecelerateInterpolator()) // As close to iOS spring as we can get
            .setStartDelay(200) // iOS 0.2s delay
            .start()
        
        // Animate carbs card next - 0.4s delay
        carbsCard.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(600) // match iOS 0.6s spring response time
            .setInterpolator(AccelerateDecelerateInterpolator()) // As close to iOS spring as we can get
            .setStartDelay(400) // iOS 0.4s delay
            .start()
        
        // Animate fats card last - 0.6s delay
        fatsCard.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(600) // match iOS 0.6s spring response time
            .setInterpolator(AccelerateDecelerateInterpolator()) // As close to iOS spring as we can get
            .setStartDelay(600) // iOS 0.6s delay
            .start()
    }
    
    // Data class for particle animation with velocity
    data class Particle(
        var x: Float,
        var y: Float,
        val radius: Float,
        var alpha: Float,
        var vx: Float = 0f,
        var vy: Float = 0f
    )
} 