package org.blckswan.art.ui.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blckswan.art.data.models.Exhibition
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.materials.workMaterial
import org.blckswan.art.ui.theme.*
import kotlin.math.absoluteValue

@Composable
fun SpatialGallery(
    exhibition: Exhibition,
    onWorkSelected: (Work) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { exhibition.works.size.coerceAtLeast(1) })
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Text(
                text = exhibition.title.uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = Phosphor,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = exhibition.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = exhibition.edition.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Fog
            )
        }

        if (exhibition.works.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ingen verk i arkivet.\nLegg til exhibition.json.",
                    color = Fog,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return
        }

        // Spatial pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 28.dp),
            pageSpacing = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val work = exhibition.works[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                .absoluteValue
                .coerceIn(0f, 1f)

            val scale by animateFloatAsState(
                targetValue = 1f - (pageOffset * 0.12f),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "scale"
            )
            val alpha by animateFloatAsState(
                targetValue = 1f - (pageOffset * 0.45f),
                label = "alpha"
            )

            WorkRoomCard(
                work = work,
                scale = scale,
                alpha = alpha,
                onClick = { onWorkSelected(work) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
            )
        }

        // Page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp, top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${pagerState.currentPage + 1}  /  ${exhibition.works.size}",
                style = MaterialTheme.typography.labelMedium,
                color = Fog.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun WorkRoomCard(
    work: Work,
    scale: Float,
    alpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                rotationY = (1f - scale) * 8f
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceDeep)
            .workMaterial(work.visualMode, work.glitchIntensity)
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onClick() }
                )
            }
    ) {
        // Content gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceElevated.copy(alpha = 0.4f),
                            Ink.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = work.medium.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Phosphor
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (work.description.isNotBlank()) {
                Text(
                    text = work.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Fog,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Edge accent
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(Phosphor.copy(alpha = 0.9f), Color.Transparent)
                    )
                )
        )
    }
}
