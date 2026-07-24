package org.blckswan.art.ui.gallery

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.components.ArtworkSurface
import org.blckswan.art.ui.materials.workMaterial
import org.blckswan.art.ui.theme.Fog
import org.blckswan.art.ui.theme.Ink
import org.blckswan.art.ui.theme.Phosphor

@Composable
fun ImmersiveViewer(
    work: Work,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showChrome by remember(work.id) { mutableStateOf(true) }
    var scale by remember(work.id) { mutableFloatStateOf(1f) }
    var translation by remember(work.id) { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        translation = if (scale <= 1.01f) Offset.Zero else translation + panChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .workMaterial(work.visualMode, work.glitchIntensity * 0.45f)
    ) {
        ArtworkSurface(
            work = work,
            immersive = true,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = translation.x
                    translationY = translation.y
                }
                .transformable(transformState)
                .pointerInput(work.id) {
                    detectTapGestures(
                        onTap = { showChrome = !showChrome },
                        onDoubleTap = {
                            if (scale > 1.1f) {
                                scale = 1f
                                translation = Offset.Zero
                            } else {
                                scale = 2.2f
                            }
                        }
                    )
                }
        )

        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Ink.copy(alpha = 0.92f), Color.Transparent)
                            )
                        )
                        .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = Phosphor
                        )
                    }
                    Text(
                        text = "${work.id.uppercase()} // IMMERSIVE",
                        color = Fog,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.weight(1f))
                    if (work.sourceUrl.startsWith("http")) {
                        IconButton(
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(work.sourceUrl))
                                    )
                                } catch (_: Throwable) {
                                    Unit
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Åpne kilde",
                                tint = Phosphor
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Ink.copy(alpha = 0.96f))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = work.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = work.medium.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Phosphor
                    )
                    if (work.description.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = work.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Fog
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "KLYP FOR ZOOM · DOBBELTRYKK FOR 2.2× · TRYKK FOR Å SKJULE UI",
                        style = MaterialTheme.typography.labelMedium,
                        color = Fog.copy(alpha = 0.56f)
                    )
                }
            }
        }
    }
}
