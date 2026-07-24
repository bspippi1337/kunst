package org.blckswan.art.ui.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.blckswan.art.data.models.Exhibition
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.theme.Fog
import org.blckswan.art.ui.theme.Ink
import org.blckswan.art.ui.theme.MoonRed
import org.blckswan.art.ui.theme.Phosphor
import org.blckswan.art.ui.theme.SurfaceDeep
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ConstellationView(
    exhibition: Exhibition,
    onBack: () -> Unit,
    onWorkSelected: (Work) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val works = exhibition.works
    val positions = remember(viewport, works.size) {
        if (viewport.width == 0 || viewport.height == 0 || works.isEmpty()) {
            emptyList()
        } else {
            works.mapIndexed { index, _ ->
                val angle = -PI / 2 + (2 * PI * index / works.size.coerceAtLeast(1))
                val ring = if (index % 2 == 0) 0.31f else 0.4f
                Offset(
                    x = viewport.width * 0.5f + cos(angle).toFloat() * viewport.width * ring,
                    y = viewport.height * 0.53f + sin(angle).toFloat() * viewport.height * ring * 0.72f
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .onSizeChanged { viewport = it }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Phosphor.copy(alpha = 0.08f),
                        SurfaceDeep,
                        Ink
                    ),
                    center = center,
                    radius = size.maxDimension * 0.74f
                )
            )

            positions.forEachIndexed { index, point ->
                if (positions.size > 1) {
                    val next = positions[(index + 1) % positions.size]
                    drawLine(
                        color = Phosphor.copy(alpha = 0.14f),
                        start = point,
                        end = next,
                        strokeWidth = 1.2f
                    )
                }
                if (index + 3 < positions.size) {
                    drawLine(
                        color = MoonRed.copy(alpha = 0.09f),
                        start = point,
                        end = positions[index + 3],
                        strokeWidth = 0.8f
                    )
                }
                drawCircle(
                    color = if (index % 4 == 0) MoonRed else Phosphor,
                    radius = if (index % 4 == 0) 8f else 5f,
                    center = point,
                    alpha = 0.78f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Text(
                text = "← GALLERI",
                color = Phosphor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "GIT-KONSTELLASJON",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Verkene er noder. Linjene er delt signal.",
                color = Fog,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        works.forEachIndexed { index, work ->
            val point = positions.getOrNull(index) ?: return@forEachIndexed
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (point.x - 42.dp.toPx()).roundToInt(),
                            y = (point.y - 42.dp.toPx()).roundToInt()
                        )
                    }
                    .size(84.dp)
                    .background(
                        color = SurfaceDeep.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .clickable { onWorkSelected(work) }
                    .padding(8.dp)
            ) {
                Text(
                    text = work.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun androidx.compose.ui.unit.Dp.toPx(): Float = value * 3f
