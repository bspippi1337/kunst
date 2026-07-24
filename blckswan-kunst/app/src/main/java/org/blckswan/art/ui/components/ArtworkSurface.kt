package org.blckswan.art.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.theme.Fog
import org.blckswan.art.ui.theme.Ink
import org.blckswan.art.ui.theme.MoonRed
import org.blckswan.art.ui.theme.Phosphor
import org.blckswan.art.ui.theme.SurfaceDeep
import kotlin.math.sin

@Composable
fun ArtworkSurface(
    work: Work,
    modifier: Modifier = Modifier,
    immersive: Boolean = false
) {
    Box(modifier = modifier.background(Ink)) {
        SwanSignal(
            seedText = work.id,
            intensity = work.glitchIntensity,
            immersive = immersive,
            modifier = Modifier.fillMaxSize()
        )

        val artwork = work.imageAsset?.takeIf { it.isNotBlank() }
        AsyncImage(
            model = if (artwork != null) {
                "file:///android_asset/${artwork.removePrefix("/")}"
            } else {
                "file:///android_asset/brand/blckswan-icon.svg"
            },
            contentDescription = work.title,
            contentScale = if (artwork != null) ContentScale.Crop else ContentScale.Fit,
            modifier = if (artwork != null) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(if (immersive) 56.dp else 44.dp)
            },
            alpha = when {
                artwork != null && immersive -> 0.95f
                artwork != null -> 0.88f
                immersive -> 0.9f
                else -> 0.78f
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Ink.copy(alpha = if (immersive) 0.18f else 0.5f),
                            Ink.copy(alpha = if (immersive) 0.58f else 0.88f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun SwanSignal(
    seedText: String,
    intensity: Float,
    immersive: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "swan-signal")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (immersive) 9000 else 6500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swan-phase"
    )

    val seed = seedText.fold(23) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }
    val power = intensity.coerceIn(0.08f, 1f)

    Canvas(modifier = modifier.background(SurfaceDeep)) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    MoonRed.copy(alpha = 0.09f + power * 0.05f),
                    SurfaceDeep,
                    Ink
                ),
                center = Offset(size.width * 0.62f, size.height * 0.34f),
                radius = size.maxDimension * 0.82f
            )
        )

        val moonRadius = size.minDimension * (0.11f + phase * 0.018f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MoonRed.copy(alpha = 0.88f),
                    MoonRed.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.77f, size.height * 0.2f),
                radius = moonRadius * 2.2f
            ),
            radius = moonRadius * 2.2f,
            center = Offset(size.width * 0.77f, size.height * 0.2f),
            blendMode = BlendMode.Screen
        )

        var previous: Offset? = null
        repeat(12) { index ->
            val xUnit = ((seed / (index + 3) + index * 37) % 100) / 100f
            val yUnit = ((seed / (index + 7) + index * 53) % 100) / 100f
            val point = Offset(
                x = size.width * (0.08f + xUnit * 0.84f),
                y = size.height * (0.1f + yUnit * 0.76f)
            )
            previous?.let { from ->
                drawLine(
                    color = Phosphor.copy(alpha = 0.035f + power * 0.025f),
                    start = from,
                    end = point,
                    strokeWidth = 1.1f
                )
            }
            drawCircle(
                color = if (index % 4 == 0) MoonRed else Fog,
                radius = if (index % 4 == 0) 3.4f else 1.8f,
                center = point,
                alpha = 0.24f + power * 0.2f
            )
            previous = point
        }

        val drift = sin(phase * Math.PI * 2).toFloat() * size.width * 0.012f
        val centerX = size.width * 0.5f + drift
        val centerY = size.height * 0.5f
        val span = size.minDimension * if (immersive) 0.42f else 0.36f

        val leftWing = Path().apply {
            moveTo(centerX, centerY + span * 0.08f)
            cubicTo(
                centerX - span * 0.18f,
                centerY - span * 0.42f,
                centerX - span * 0.72f,
                centerY - span * 0.28f,
                centerX - span,
                centerY + span * 0.18f
            )
            cubicTo(
                centerX - span * 0.56f,
                centerY + span * 0.05f,
                centerX - span * 0.28f,
                centerY + span * 0.42f,
                centerX,
                centerY + span * 0.08f
            )
        }
        val rightWing = Path().apply {
            moveTo(centerX, centerY + span * 0.08f)
            cubicTo(
                centerX + span * 0.18f,
                centerY - span * 0.42f,
                centerX + span * 0.72f,
                centerY - span * 0.28f,
                centerX + span,
                centerY + span * 0.18f
            )
            cubicTo(
                centerX + span * 0.56f,
                centerY + span * 0.05f,
                centerX + span * 0.28f,
                centerY + span * 0.42f,
                centerX,
                centerY + span * 0.08f
            )
        }
        val neck = Path().apply {
            moveTo(centerX, centerY + span * 0.12f)
            cubicTo(
                centerX + span * 0.04f,
                centerY - span * 0.12f,
                centerX - span * 0.08f,
                centerY - span * 0.38f,
                centerX + span * 0.12f,
                centerY - span * 0.58f
            )
            cubicTo(
                centerX + span * 0.2f,
                centerY - span * 0.64f,
                centerX + span * 0.25f,
                centerY - span * 0.58f,
                centerX + span * 0.31f,
                centerY - span * 0.56f
            )
        }

        val wingBrush = Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.98f),
                MoonRed.copy(alpha = 0.18f + power * 0.12f),
                Color.Black.copy(alpha = 0.96f)
            ),
            start = Offset(centerX - span, centerY - span * 0.4f),
            end = Offset(centerX + span, centerY + span * 0.4f)
        )

        drawPath(leftWing, brush = wingBrush)
        drawPath(rightWing, brush = wingBrush)
        drawPath(
            neck,
            color = Fog.copy(alpha = 0.28f),
            style = Stroke(width = if (immersive) 6f else 4f)
        )
        drawCircle(
            color = MoonRed,
            radius = if (immersive) 4.8f else 3.4f,
            center = Offset(centerX + span * 0.22f, centerY - span * 0.585f)
        )
    }
}
