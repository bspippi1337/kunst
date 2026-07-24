package org.blckswan.art

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private val Void = Color(0xFF080809)
private val GalleryBlack = Color(0xFF101012)
private val Raised = Color(0xFF18181B)
private val BloodMoon = Color(0xFFD72D4B)
private val BloodMoonSoft = Color(0xFF35151C)
private val Bone = Color(0xFFF3F1EC)
private val Fog = Color(0xFFAAA8A3)
private val Hairline = Color(0xFF2A2A2E)
private val Signal = Color(0xFF75E4B3)

private val KunstColors = darkColorScheme(
    primary = BloodMoon,
    onPrimary = Color.White,
    secondary = Bone,
    onSecondary = Void,
    background = Void,
    onBackground = Bone,
    surface = GalleryBlack,
    onSurface = Bone,
    surfaceVariant = Raised,
    onSurfaceVariant = Fog,
    outline = Hairline,
    error = BloodMoon
)

data class ArtLink(val label: String, val url: String)

data class Work(
    val id: String,
    val title: String,
    val profile: String,
    val repo: String,
    val year: String,
    val medium: String,
    val chapter: String,
    val provenance: String,
    val confidence: String,
    val what: String,
    val meaning: String,
    val how: String,
    val look: String,
    val sourceUrl: String,
    val links: List<ArtLink>,
    val visualType: String,
    val artType: String,
    val art: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 8, 9)
        window.navigationBarColor = android.graphics.Color.rgb(8, 8, 9)

        val works = runCatching { loadWorks() }.getOrElse { emptyList() }
        setContent {
            BlckswanKunstTheme {
                GalleryApp(
                    works = works,
                    onOpenUrl = ::openUrl,
                    onShare = ::shareWork
                )
            }
        }
    }

    private fun loadWorks(): List<Work> {
        val text = assets.open("gallery_v4.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val array = JSONObject(text).getJSONArray("works")
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val linksJson = item.optJSONArray("links")
                val links = buildList {
                    if (linksJson != null) {
                        for (linkIndex in 0 until linksJson.length()) {
                            val link = linksJson.getJSONObject(linkIndex)
                            val url = link.optString("url")
                            if (url.isNotBlank()) {
                                add(ArtLink(link.optString("label", "Kilde"), url))
                            }
                        }
                    }
                }
                add(
                    Work(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        profile = item.getString("profile"),
                        repo = item.getString("repo"),
                        year = item.getString("year"),
                        medium = item.getString("medium"),
                        chapter = item.getString("chapter"),
                        provenance = item.getString("provenance"),
                        confidence = item.getString("confidence"),
                        what = item.getString("what"),
                        meaning = item.getString("meaning"),
                        how = item.getString("how"),
                        look = item.getString("look"),
                        sourceUrl = item.optString("sourceUrl"),
                        links = links,
                        visualType = item.getString("visualType"),
                        artType = item.getString("artType"),
                        art = item.optString("art")
                    )
                )
            }
        }
    }

    private fun openUrl(url: String) {
        if (url.isBlank()) return
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    private fun shareWork(work: Work) {
        val text = buildString {
            append(work.title)
            append("\n\n")
            append(work.meaning)
            if (work.sourceUrl.isNotBlank()) {
                append("\n\n")
                append(work.sourceUrl)
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, work.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Del verket"))
    }
}

@Composable
private fun BlckswanKunstTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KunstColors,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 48.sp,
                letterSpacing = (-1.7).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                letterSpacing = (-1.0).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.4).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 17.sp,
                lineHeight = 25.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            labelLarge = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.6.sp
            )
        ),
        content = content
    )
}

@Composable
private fun GalleryApp(
    works: List<Work>,
    onOpenUrl: (String) -> Unit,
    onShare: (Work) -> Unit
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    BackHandler(enabled = selectedIndex >= 0) { selectedIndex = -1 }

    AnimatedContent(
        targetState = selectedIndex,
        transitionSpec = {
            (fadeIn(tween(180)) togetherWith fadeOut(tween(120)))
                .using(SizeTransform(clip = false))
        },
        label = "gallery-screen"
    ) { index ->
        if (index in works.indices) {
            WorkDetailScreen(
                work = works[index],
                position = index,
                total = works.size,
                onBack = { selectedIndex = -1 },
                onPrevious = { selectedIndex = (index - 1 + works.size) % works.size },
                onNext = { selectedIndex = (index + 1) % works.size },
                onOpenUrl = onOpenUrl,
                onShare = onShare
            )
        } else {
            GalleryScreen(
                works = works,
                onSelect = { selectedIndex = it }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryScreen(
    works: List<Work>,
    onSelect: (Int) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var chapter by rememberSaveable { mutableStateOf("Alle") }
    val chapters = remember(works) { listOf("Alle") + works.map { it.chapter }.distinct() }
    val filtered = remember(works, query, chapter) {
        works.withIndex().filter { indexed ->
            val work = indexed.value
            val chapterMatches = chapter == "Alle" || work.chapter == chapter
            val haystack = listOf(
                work.title, work.profile, work.repo, work.medium,
                work.chapter, work.meaning, work.year
            ).joinToString(" ").lowercase()
            chapterMatches && (query.isBlank() || haystack.contains(query.trim().lowercase()))
        }
    }

    Scaffold(
        containerColor = Void,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SwanMark(44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BLCKSWAN KUNST",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "41 digitale verk",
                                color = Fog,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Void,
                    titleContentColor = Bone
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Se verket først.",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Åpne et verk for å se hele flaten og få en kort forklaring på vanlig språk.",
                    color = Fog,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Søk i verk, medium eller repo") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Tøm søk")
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GalleryBlack,
                        unfocusedContainerColor = GalleryBlack,
                        focusedBorderColor = BloodMoon,
                        unfocusedBorderColor = Hairline,
                        focusedTextColor = Bone,
                        unfocusedTextColor = Bone,
                        cursorColor = BloodMoon
                    )
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chapters) { item ->
                    FilterChip(
                        selected = chapter == item,
                        onClick = { chapter = item },
                        label = { Text(item) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GalleryBlack,
                            labelColor = Fog,
                            selectedContainerColor = BloodMoonSoft,
                            selectedLabelColor = Bone
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = chapter == item,
                            borderColor = Hairline,
                            selectedBorderColor = BloodMoon
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Signal)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${filtered.size} VERK",
                        color = Fog,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(
                    text = "TRYKK FOR Å ÅPNE",
                    color = Fog.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (works.isEmpty()) {
                EmptyArchive()
            } else if (filtered.isEmpty()) {
                EmptySearch()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(320.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp, 10.dp, 14.dp, 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filtered, key = { it.value.id }) { indexed ->
                        GalleryCard(
                            work = indexed.value,
                            onClick = { onSelect(indexed.index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryCard(work: Work, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = GalleryBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ArtPreview(
            work = work,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = work.medium.uppercase(),
                    color = BloodMoon,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = work.year,
                    color = Fog,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(
                text = work.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = shortSummary(work.meaning),
                color = Fog,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 18.dp, bottom = 13.dp),
                color = Hairline
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = work.chapter,
                    color = Fog,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "SE VERKET  →",
                    color = Bone,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ArtPreview(work: Work, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0A0B), Color(0xFF050506))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (work.artType == "text" && work.art.isNotBlank()) {
            Text(
                text = previewArt(work.art),
                color = Bone,
                fontFamily = FontFamily.Monospace,
                fontSize = previewFontSize(work.art),
                lineHeight = previewFontSize(work.art) * 1.18f,
                maxLines = 16,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            RepositoryObjectPreview(work)
        }
        Text(
            text = "\\\$/",
            color = Bone.copy(alpha = 0.13f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 52.sp,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun RepositoryObjectPreview(work: Work) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Hairline, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DIGITALT OBJEKT",
                    color = BloodMoon,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${work.profile}/${work.repo}",
                    color = Fog,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            Text(
                text = "\\\$/",
                color = Bone,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )
        }
        Column {
            Text(
                text = work.title,
                color = Bone,
                fontSize = 30.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = work.medium,
                color = Fog,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(work.year, color = Fog, style = MaterialTheme.typography.labelLarge)
            Text("REPO / SYSTEM / TEKST", color = Fog, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkDetailScreen(
    work: Work,
    position: Int,
    total: Int,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onShare: (Work) -> Unit
) {
    Scaffold(
        containerColor = Void,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "VERK ${position + 1} AV $total",
                            color = Fog,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = work.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    IconButton(onClick = { onShare(work) }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Del verket")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Void,
                    navigationIconContentColor = Bone,
                    actionIconContentColor = Bone,
                    titleContentColor = Bone
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            item {
                DetailArtStage(work)
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = work.medium.uppercase(),
                            color = BloodMoon,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = work.year,
                            color = Fog,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Text(
                        text = work.title,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(top = 15.dp)
                    )
                    Text(
                        text = "${work.profile} / ${work.repo}",
                        color = Fog,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    SummaryPanel(work)
                    ExplanationSection("DETTE SER DU", work.what)
                    ExplanationSection("DERFOR BETYR DET NOE", work.meaning)
                    ExplanationSection("SLIK BLE DET LAGET", work.how)
                    ExplanationSection("LEGG MERKE TIL", work.look)
                    ArchivePanel(work)

                    if (work.sourceUrl.isNotBlank()) {
                        Button(
                            onClick = { onOpenUrl(work.sourceUrl) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BloodMoon,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Text("ÅPNE ORIGINALKILDEN", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(9.dp))
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    work.links.forEach { link ->
                        TextButton(
                            onClick = { onOpenUrl(link.url) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(link.label.uppercase(), style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(17.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp)
                            .border(1.dp, Hairline, RoundedCornerShape(18.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onPrevious,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 18.dp)
                        ) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = null)
                            Text("FORRIGE", style = MaterialTheme.typography.labelLarge)
                        }
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(34.dp)
                                .background(Hairline)
                        )
                        TextButton(
                            onClick = onNext,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 18.dp)
                        ) {
                            Text("NESTE", style = MaterialTheme.typography.labelLarge)
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailArtStage(work: Work) {
    if (work.artType == "text" && work.art.isNotBlank()) {
        var fontSize by rememberSaveable(work.id) { mutableFloatStateOf(initialDetailFontSize(work.art)) }
        val horizontal = rememberScrollState()
        val vertical = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050506))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORIGINAL TEKST- / ASCII-FLATE",
                    color = Fog,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace
                )
                Row {
                    IconButton(onClick = { fontSize = (fontSize - 1f).coerceAtLeast(7f) }) {
                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Mindre tekst")
                    }
                    IconButton(onClick = { fontSize = (fontSize + 1f).coerceAtMost(24f) }) {
                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Større tekst")
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .horizontalScroll(horizontal)
                    .verticalScroll(vertical)
                    .padding(20.dp)
            ) {
                Text(
                    text = work.art,
                    color = Bone,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.18f).sp,
                    softWrap = false
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(Color(0xFF050506))
                .padding(18.dp)
        ) {
            RepositoryObjectPreview(work)
        }
    }
}

@Composable
private fun SummaryPanel(work: Work) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BloodMoonSoft),
        border = androidx.compose.foundation.BorderStroke(1.dp, BloodMoon.copy(alpha = 0.55f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "KORT FORKLART",
                color = BloodMoon,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = shortSummary(work.meaning, 2),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun ExplanationSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
    ) {
        Text(
            text = title,
            color = BloodMoon,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = body,
            color = Bone,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun ArchivePanel(work: Work) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GalleryBlack),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("ARKIVINFORMASJON", color = BloodMoon, style = MaterialTheme.typography.labelLarge)
            MetadataRow("Medium", work.medium)
            MetadataRow("Kapittel", work.chapter)
            MetadataRow("Opphav", work.provenance)
            MetadataRow("Dokumentasjon", work.confidence)
            MetadataRow("Objekttype", work.visualType)
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Fog,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.width(116.dp)
        )
        Text(
            text = value,
            color = Bone,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SwanMark(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(13.dp))
            .background(BloodMoon),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\\\$/",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = (-1.8).sp
        )
    }
}

@Composable
private fun EmptyArchive() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("\\\$/", color = BloodMoon, fontSize = 56.sp, fontFamily = FontFamily.Monospace)
            Text("Kunstarkivet kunne ikke leses", color = Bone, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun EmptySearch() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("INGEN TREFF", color = BloodMoon, style = MaterialTheme.typography.labelLarge)
            Text(
                "Prøv et annet ord eller velg Alle.",
                color = Fog,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

private fun previewArt(art: String): String {
    val lines = art.lines().dropWhile { it.isBlank() }
    if (lines.size <= 16) return lines.joinToString("\n")
    val start = ((lines.size - 16) / 2).coerceAtLeast(0)
    return lines.drop(start).take(16).joinToString("\n")
}

private fun previewFontSize(art: String) = when {
    (art.lines().maxOfOrNull { it.length } ?: 0) > 100 -> 7.sp
    (art.lines().maxOfOrNull { it.length } ?: 0) > 65 -> 9.sp
    else -> 12.sp
}

private fun initialDetailFontSize(art: String): Float = when {
    (art.lines().maxOfOrNull { it.length } ?: 0) > 120 -> 8f
    (art.lines().maxOfOrNull { it.length } ?: 0) > 80 -> 10f
    else -> 14f
}

private fun shortSummary(text: String, sentences: Int = 1): String {
    val parts = text.trim().split(Regex("(?<=[.!?])\\s+"))
    return parts.take(sentences).joinToString(" ").ifBlank { text.trim() }
}
