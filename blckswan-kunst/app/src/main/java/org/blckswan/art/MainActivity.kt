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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.blckswan.art.data.ExhibitionRepository
import org.blckswan.art.data.models.Exhibition
import org.blckswan.art.data.models.Work
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

@Composable
private fun KunstApp() {
    val context = LocalContext.current
    var exhibition by remember { mutableStateOf<Exhibition?>(null) }
    var selectedWork by remember { mutableStateOf<Work?>(null) }

    LaunchedEffect(Unit) {
        try {
            val repo = ExhibitionRepository(context)
            exhibition = repo.loadExhibition()
        } catch (t: Throwable) {
            exhibition = Exhibition(
                title = "BLCKSWAN ART",
                subtitle = "Arkivet kunne ikke leses",
                edition = "Error mode"
            )
        }
    }

    when {
        exhibition == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BLCKSWAN",
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
        else -> {
            SpatialGallery(
                exhibition = exhibition!!,
                onWorkSelected = { selectedWork = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
