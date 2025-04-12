package com.codepad.foodai.ui.user_property.result

import android.graphics.PointF
import java.util.UUID

/**
 * Represents a particle for the animation effect
 */
data class Particle(
    val id: UUID = UUID.randomUUID(),
    val startPosition: PointF,
    val endPosition: PointF,
    val size: Float,
    val color: Int,
    val animationDelay: Long,
    val animationDuration: Long,
    val fadeOutDelay: Long,
    val autoFadeOut: Boolean = true
) 