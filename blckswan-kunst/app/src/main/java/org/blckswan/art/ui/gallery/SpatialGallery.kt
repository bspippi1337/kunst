package org.blckswan.art.ui.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.blckswan.art.data.models.Exhibition
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.components.ArtworkSurface
import org.blckswan.art.ui.materials.workMaterial
import org.blckswan.art.ui.theme.Fog
import org.blckswan.art.ui.theme.Ink
import org.blckswan.art.ui.theme.MoonRed
import org.blckswan.art.ui.theme.Phosphor
import org.blckswan.art.ui.theme.SurfaceDeep
import kotlin.math.absoluteValue

@Composable
fun SpatialGallery(
    exhibition: Exhibition,
    onWorkSelected: (Work) -> Unit,
    onOpenConstellation: () -> Unit,
    exhibitionMode: Boolean,
    onToggleExhibitionMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val works = exhibition.works
    val pagerState = rememberPagerState(pageCount = { works.size.coerceAtLeast(1) })

    LaunchedEffect(exhibitionMode, works.size) {
        while (exhibitionMode && works.size > 1) {
            delay(4800)
            val next = (pagerState.currentPage + 1) % works.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp)
                .graphicsLayer { alpha = if (exhibitionMode) 0.42f else 1f }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BLCKSWAN // NODE-42",
                    color = Phosphor,
                    style = MaterialTheme.typography.labelMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(
                        text = "KONSTELLASJON",
                        color = Fog,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable(onClick = onOpenConstellation)
                    )
                    Text(
                        text = if (exhibitionMode) "STOPP" else "UTSTILLING",
                        color = if (exhibitionMode) MoonRed else Phosphor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable(onClick = onToggleExhibitionMode)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = exhibition.title.uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = exhibition.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Fog
            )
            Text(
                text = exhibition.edition.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Phosphor.copy(alpha = 0.72f)
            )
        }

        if (works.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ARKIVET ER TOMT\nSIGNAL VENTER",
                    color = Fog,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = if (exhibitionMode) 8.dp else 28.dp),
            pageSpacing = if (exhibitionMode) 8.dp else 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val work = works[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                .absoluteValue
                .coerceIn(0f, 1f)

            val scale by animateFloatAsState(
                targetValue = if (exhibitionMode) 1f else 1f - pageOffset * 0.11f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "room-scale"
            )
            val alpha by animateFloatAsState(
                targetValue = 1f - pageOffset * if (exhibitionMode) 0.15f else 0.45f,
                label = "room-alpha"
            )

            WorkRoomCard(
                work = work,
                scale = scale,
                alpha = alpha,
                exhibitionMode = exhibitionMode,
                onClick = { onWorkSelected(work) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (exhibitionMode) 0.62f else 0.72f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 10.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = works[pagerState.currentPage.coerceIn(0, works.lastIndex)].medium.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Fog.copy(alpha = 0.68f)
            )
            Text(
                text = "${pagerState.currentPage + 1} / ${works.size}",
                style = MaterialTheme.typography.labelMedium,
                color = Phosphor.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun WorkRoomCard(
    work: Work,
    scale: Float,
    alpha: Float,
    exhibitionMode: Boolean,
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
                rotationY = if (exhibitionMode) 0f else (1f - scale) * 8f
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(if (exhibitionMode) 0.dp else 6.dp))
            .background(SurfaceDeep)
            .workMaterial(work.visualMode, work.glitchIntensity * 0.62f)
            .clickable(onClick = onClick)
    ) {
        ArtworkSurface(
            work = work,
            immersive = exhibitionMode,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Ink.copy(alpha = 0.92f))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = work.medium.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Phosphor
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = work.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!exhibitionMode && work.description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = work.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Fog,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

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
