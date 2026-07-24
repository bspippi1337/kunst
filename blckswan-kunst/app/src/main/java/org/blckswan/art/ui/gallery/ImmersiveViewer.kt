package org.blckswan.art.ui.gallery

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.materials.workMaterial
import org.blckswan.art.ui.theme.*

@Composable
fun ImmersiveViewer(
    work: Work,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showChrome by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .workMaterial(work.visualMode, work.glitchIntensity)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showChrome = !showChrome }
                )
            }
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SurfaceElevated,
                            Ink
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = work.title.uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Phosphor
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = work.medium,
                    style = MaterialTheme.typography.labelMedium,
                    color = Fog
                )
            }
        }

        // Chrome overlay
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Ink.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = Phosphor
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (work.sourceUrl.startsWith("http")) {
                        IconButton(onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(work.sourceUrl))
                                )
                            } catch (_: Throwable) {}
                        }) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Åpne kilde",
                                tint = Phosphor
                            )
                        }
                    }
                }

                // Bottom info sheet
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Ink.copy(alpha = 0.92f))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = work.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
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
                    if (work.sourceUrl.isNotBlank()) {
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "KILDE →",
                            style = MaterialTheme.typography.labelMedium,
                            color = Fog.copy(alpha = 0.6f)
                        )
                        Text(
                            text = work.sourceUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Phosphor.copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
