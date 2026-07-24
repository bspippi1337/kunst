package org.blckswan.art.ui.materials

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import org.blckswan.art.ui.theme.GlitchMagenta
import org.blckswan.art.ui.theme.Phosphor
import kotlin.math.sin

/**
 * CRT-style horizontal scanlines + subtle vertical drift.
 */
fun Modifier.scanlines(
    intensity: Float = 0.18f,
    lineSpacing: Float = 3.2f
): Modifier = this.drawWithContent {
    drawContent()
    val alpha = intensity.coerceIn(0f, 0.45f)
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.1f
        )
        y += lineSpacing
    }
}

/**
 * Soft phosphor bloom (radial glow) that can pulse.
 */
@Composable
fun Modifier.phosphorBloom(
    color: Color = Phosphor,
    intensity: Float = 0.35f,
    pulse: Boolean = true
): Modifier {
    val infinite = rememberInfiniteTransition(label = "phosphor")
    val pulseFactor by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val factor = if (pulse) pulseFactor else 1f

    return this.drawWithContent {
        drawContent()
        val radius = size.minDimension * 0.55f * factor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = intensity * 0.55f),
                    color.copy(alpha = intensity * 0.18f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            blendMode = BlendMode.Screen
        )
    }
}

/**
 * Horizontal RGB-ish split + occasional tear. Pure Canvas, no shaders needed.
 */
@Composable
fun Modifier.glitchOverlay(
    intensity: Float = 0.4f,
    enabled: Boolean = true
): Modifier {
    if (!enabled || intensity <= 0.01f) return this

    val infinite = rememberInfiniteTransition(label = "glitch")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    return this.drawWithContent {
        drawContent()

        val strength = intensity.coerceIn(0f, 1f)
        val tearY = size.height * (0.25f + 0.5f * sin(phase * 6.28f * 1.7f).toFloat().let { (it + 1f) / 2f })
        val offsetX = (sin(phase * 12.5f) * 8f * strength)

        // Subtle chromatic fringes
        drawRect(
            color = GlitchMagenta.copy(alpha = 0.07f * strength),
            topLeft = Offset(offsetX * 1.4f, 0f),
            size = size,
            blendMode = BlendMode.Screen
        )
        drawRect(
            color = Phosphor.copy(alpha = 0.05f * strength),
            topLeft = Offset(-offsetX * 1.1f, 0f),
            size = size,
            blendMode = BlendMode.Screen
        )

        // Occasional horizontal tear line
        if (phase > 0.72f && phase < 0.88f) {
            drawLine(
                color = Color.White.copy(alpha = 0.55f * strength),
                start = Offset(0f, tearY),
                end = Offset(size.width, tearY),
                strokeWidth = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
            )
        }
    }
}

/**
 * Combined material stack for a work room.
 */
@Composable
fun Modifier.workMaterial(
    mode: org.blckswan.art.data.models.VisualMode,
    intensity: Float = 0.35f
): Modifier {
    var mod = this
    when (mode) {
        org.blckswan.art.data.models.VisualMode.GLITCH -> {
            mod = mod
                .scanlines(intensity = 0.16f + intensity * 0.12f)
                .glitchOverlay(intensity = intensity)
        }
        org.blckswan.art.data.models.VisualMode.PHOSPHOR -> {
            mod = mod
                .scanlines(intensity = 0.12f)
                .phosphorBloom(intensity = 0.28f + intensity * 0.25f)
        }
        org.blckswan.art.data.models.VisualMode.INTERACTIVE -> {
            mod = mod
                .scanlines(intensity = 0.14f)
                .glitchOverlay(intensity = intensity * 0.7f)
                .phosphorBloom(intensity = 0.22f, pulse = true)
        }
        else -> {
            mod = mod.scanlines(intensity = 0.09f)
        }
    }
    return mod
}
