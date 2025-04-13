package com.codepad.foodai.ui.user_property.result

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.codepad.foodai.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

/**
 * A custom view that displays floating particles animation
 */
class ParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random()
    private var centerX = 0f
    private var centerY = 0f
    private val particleAnimators = mutableMapOf<UUID, ValueAnimator>()
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Flag to control if animation should run
    var shouldRunAnimation = true
    
    // Colors for particles
    private val colors = listOf(
        Color.parseColor("#e0f2ff"),  // Light blue
        Color.parseColor("#ccf2cc"),   // Light green
        Color.parseColor("#fff099"),   // Light yellow
        Color.parseColor("#cce5ff"),    // Sky blue
        Color.parseColor("#faebf2"), // Light pink
        ContextCompat.getColor(context, R.color.orange),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.blue),
        ContextCompat.getColor(context, R.color.red),
        ContextCompat.getColor(context, R.color.yellow)
    )

    init {
        // Make view invisible initially
        alpha = 0f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h * 0.35f // Approximate position of the calorie circle
        
        // Create particles after we know the view size
        createParticles()
        
        // Start animation sequence only if shouldRunAnimation is true
        if (shouldRunAnimation) {
            startAnimationSequence()
        } else {
            // Just make the view invisible if we shouldn't animate
            alpha = 0f
        }
    }

    private fun startAnimationSequence() {
        scope.launch {
            // Make particles visible
            delay(600)
            animate().alpha(1f).setDuration(300).start()
            
            // Start particle animations
            delay(100)
            startParticleAnimations()
            
            // Clean up after animations complete
            delay(4000)
            clearParticles()
        }
    }

    private fun createParticles() {
        particles.clear()
        
        for (i in 0 until 150) {
            // Smaller particle sizes
            val particleSize = random.nextFloat() * 7f + 2f // 2-9dp
            
            // All particles start near the center (calorie circle)
            // Add slight randomness to make it look like they're coming from the whole circle area
            val startX = centerX + (random.nextFloat() * 80f - 40f)
            val startY = centerY + (random.nextFloat() * 80f - 40f)
            
            // End position - spread outward and downward
            val angle = random.nextFloat() * (2 * Math.PI).toFloat()
            val distance = random.nextFloat() * 
                    (width.coerceAtLeast(height)).toFloat() + 100f
            
            // Calculate end position (mostly downward, but some spread in all directions)
            // More weight to going downward with the y component
            val endX = startX + distance * cos(angle) * 0.5f
            val endY = startY + distance * sin(angle) + (random.nextFloat() * 200f + 100f) // Extra force downward
            
            val randomColor = colors[random.nextInt(colors.size)]
            val animationDelay = (i % 15) * 10L // Very quick staggered start
            val animationDuration = random.nextInt(2000) + 800L // 0.8-2.8 seconds
            val fadeOutDelay = animationDuration + animationDelay + random.nextInt(1000) + 500L
            
            particles.add(
                Particle(
                    startPosition = PointF(startX, startY),
                    endPosition = PointF(endX, endY),
                    size = particleSize,
                    color = randomColor,
                    animationDelay = animationDelay,
                    animationDuration = animationDuration,
                    fadeOutDelay = fadeOutDelay
                )
            )
        }
    }

    private fun startParticleAnimations() {
        particles.forEach { particle ->
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = particle.animationDuration
            animator.startDelay = particle.animationDelay
            animator.interpolator = LinearInterpolator()
            
            animator.addUpdateListener {
                invalidate() // Redraw the view
            }
            
            particleAnimators[particle.id] = animator
            animator.start()
            
            // Handle auto fade out
            if (particle.autoFadeOut) {
                postDelayed({
                    particleAnimators[particle.id]?.cancel()
                    particleAnimators.remove(particle.id)
                    particles.remove(particle)
                    invalidate()
                }, particle.animationDuration + particle.fadeOutDelay)
            }
        }
    }

    private fun clearParticles() {
        // Cancel all animators
        particleAnimators.values.forEach { it.cancel() }
        particleAnimators.clear()
        
        // Clear particles and invalidate
        particles.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        particles.forEach { particle ->
            val animator = particleAnimators[particle.id]
            val progress = animator?.animatedValue as? Float ?: 0f
            
            // Calculate current position
            val x = particle.startPosition.x + 
                    (particle.endPosition.x - particle.startPosition.x) * progress
            val y = particle.startPosition.y + 
                    (particle.endPosition.y - particle.startPosition.y) * progress
            
            // Calculate alpha for fading effect
            val alpha = if (progress > 0.8f) {
                // Start fading out at 80% of the animation
                (1f - (progress - 0.8f) / 0.2f).coerceIn(0f, 1f)
            } else {
                1f
            }
            
            // Draw the particle
            paint.color = particle.color
            paint.alpha = (alpha * 255).toInt()
            canvas.drawCircle(x, y, particle.size, paint)
        }
    }
} 