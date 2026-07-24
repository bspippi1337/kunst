package org.blckswan.art

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.blckswan.art.data.ExhibitionRepository
import org.blckswan.art.data.models.Exhibition
import org.blckswan.art.data.models.Work
import org.blckswan.art.ui.gallery.ConstellationView
import org.blckswan.art.ui.gallery.ImmersiveViewer
import org.blckswan.art.ui.gallery.SpatialGallery
import org.blckswan.art.ui.theme.BlckswanTheme
import org.blckswan.art.ui.theme.Ink
import org.blckswan.art.ui.theme.Phosphor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlckswanTheme {
                KunstApp()
            }
        }
    }
}

private enum class GalleryScreen {
    GALLERY,
    CONSTELLATION
}

@Composable
private fun KunstApp() {
    val context = LocalContext.current
    var exhibition by remember { mutableStateOf<Exhibition?>(null) }
    var selectedWork by remember { mutableStateOf<Work?>(null) }
    var screenName by rememberSaveable { mutableStateOf(GalleryScreen.GALLERY.name) }
    var exhibitionMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        exhibition = try {
            ExhibitionRepository(context).loadExhibition()
        } catch (_: Throwable) {
            Exhibition(
                title = "BLCKSWAN ART",
                subtitle = "Arkivet kunne ikke leses",
                edition = "Crash-safe fallback"
            )
        }
    }

    val archive = exhibition
    when {
        archive == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BLCKSWAN // LOADING SIGNAL",
                    color = Phosphor,
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        selectedWork != null -> {
            ImmersiveViewer(
                work = selectedWork!!,
                onBack = { selectedWork = null },
                modifier = Modifier.fillMaxSize()
            )
        }

        screenName == GalleryScreen.CONSTELLATION.name -> {
            ConstellationView(
                exhibition = archive,
                onBack = { screenName = GalleryScreen.GALLERY.name },
                onWorkSelected = { selectedWork = it },
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            SpatialGallery(
                exhibition = archive,
                onWorkSelected = { selectedWork = it },
                onOpenConstellation = { screenName = GalleryScreen.CONSTELLATION.name },
                exhibitionMode = exhibitionMode,
                onToggleExhibitionMode = { exhibitionMode = !exhibitionMode },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
