package org.blckswan.art.ui.materials

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import org.blckswan.art.data.models.VisualMode
import org.blckswan.art.ui.theme.GlitchMagenta
import org.blckswan.art.ui.theme.Phosphor
import kotlin.math.sin

fun Modifier.scanlines(
    intensity: Float = 0.12f,
    lineSpacing: Float = 3.6f
): Modifier = drawWithContent {
    drawContent()
    val alpha = intensity.coerceIn(0f, 0.28f)
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += lineSpacing
    }
}

@Composable
fun Modifier.phosphorBloom(
    color: Color = Phosphor,
    intensity: Float = 0.28f,
    pulse: Boolean = true
): Modifier {
    val transition = rememberInfiniteTransition(label = "phosphor")
    val pulseFactor by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phosphor-pulse"
    )
    val factor = if (pulse) pulseFactor else 1f

    return drawWithContent {
        drawContent()
        val radius = size.minDimension * 0.54f * factor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = intensity * 0.36f),
                    color.copy(alpha = intensity * 0.1f),
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

@Composable
fun Modifier.glitchOverlay(
    intensity: Float = 0.28f,
    enabled: Boolean = true
): Modifier {
    if (!enabled || intensity <= 0.01f) return this

    val transition = rememberInfiniteTransition(label = "glitch")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glitch-phase"
    )

    return drawWithContent {
        drawContent()

        val strength = intensity.coerceIn(0f, 1f)
        val wave = sin(phase * 12.5f).toFloat()
        val offsetX = wave * 6f * strength
        val tearWave = sin(phase * 10.676f).toFloat()
        val tearY = size.height * (0.34f + ((tearWave + 1f) * 0.16f))

        if (phase > 0.78f && phase < 0.9f) {
            drawRect(
                color = GlitchMagenta.copy(alpha = 0.055f * strength),
                topLeft = Offset(offsetX, 0f),
                size = size,
                blendMode = BlendMode.Screen
            )
            drawRect(
                color = Phosphor.copy(alpha = 0.04f * strength),
                topLeft = Offset(-offsetX, 0f),
                size = size,
                blendMode = BlendMode.Screen
            )
            drawLine(
                color = Color.White.copy(alpha = 0.42f * strength),
                start = Offset(0f, tearY),
                end = Offset(size.width, tearY),
                strokeWidth = 1.4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        }
    }
}

@Composable
fun Modifier.workMaterial(
    mode: VisualMode,
    intensity: Float = 0.28f
): Modifier {
    val power = intensity.coerceIn(0f, 1f)
    return when (mode) {
        VisualMode.GLITCH -> scanlines(0.1f + power * 0.08f)
            .glitchOverlay(power)

        VisualMode.PHOSPHOR -> scanlines(0.08f)
            .phosphorBloom(intensity = 0.2f + power * 0.16f)

        VisualMode.INTERACTIVE -> scanlines(0.09f)
            .glitchOverlay(power * 0.55f)
            .phosphorBloom(intensity = 0.2f, pulse = true)

        VisualMode.STATIC -> scanlines(0.06f)
    }
}
