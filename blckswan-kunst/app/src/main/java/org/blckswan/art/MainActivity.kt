package org.blckswan.art

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private val Ink = Color(0xFF070707)
private val Panel = Color(0xFF131315)
private val Line = Color(0xFF2B2B2E)
private val Paper = Color(0xFFF2F0EA)
private val PaperSoft = Color(0xFFE2DFD7)
private val PaperText = Color(0xFF171718)
private val PaperMuted = Color(0xFF5C5954)
private val Bone = Color(0xFFF6F5F2)
private val Fog = Color(0xFFA8A6A2)
private val BloodMoon = Color(0xFFC51F3A)
private val BloodMoonDark = Color(0xFF98152B)

private data class ArtLink(val label: String, val url: String)

private data class Work(
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
        window.statusBarColor = android.graphics.Color.rgb(7, 7, 7)
        window.navigationBarColor = android.graphics.Color.rgb(7, 7, 7)

        val works = runCatching(::loadWorks).getOrElse { emptyList() }
        setContent {
            BlckswanTheme {
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
                            if (url.isNotBlank()) add(ArtLink(link.optString("label", "Kilde"), url))
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
            append(work.what)
            if (work.sourceUrl.isNotBlank()) append("\n\n${work.sourceUrl}")
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
private fun BlckswanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 47.sp,
                letterSpacing = (-1.8).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                lineHeight = 35.sp,
                letterSpacing = (-1.1).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 27.sp,
                lineHeight = 29.sp,
                letterSpacing = (-0.6).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 17.sp,
                lineHeight = 26.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
                lineHeight = 23.sp
            ),
            labelLarge = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
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
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen"
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
            GalleryScreen(works = works, onSelect = { selectedIndex = it })
        }
    }
}

@Composable
private fun GalleryScreen(works: List<Work>, onSelect: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedChapter by rememberSaveable { mutableStateOf("Alle") }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    val chapters = remember(works) { listOf("Alle") + works.map { it.chapter }.distinct() }
    val filtered = remember(works, query, selectedChapter) {
        works.withIndex().filter { indexed ->
            val work = indexed.value
            val chapterOk = selectedChapter == "Alle" || work.chapter == selectedChapter
            val haystack = listOf(
                work.title, work.profile, work.repo, work.medium,
                work.chapter, work.what, work.meaning, work.year
            ).joinToString(" ").lowercase()
            chapterOk && (query.isBlank() || haystack.contains(query.trim().lowercase()))
        }
    }

    Surface(color = Ink, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            GalleryTopBar(
                searchOpen = searchOpen,
                query = query,
                onQueryChange = { query = it },
                onToggleSearch = {
                    searchOpen = !searchOpen
                    if (!searchOpen) query = ""
                }
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(320.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 42.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GalleryIntro()
                }

                if (works.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FeaturedWork(
                            work = works.first(),
                            onClick = { onSelect(0) }
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ChapterStrip(
                        chapters = chapters,
                        selected = selectedChapter,
                        onSelect = { selectedChapter = it }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ResultLine(count = filtered.size)
                }

                if (works.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("Kunstarkivet kunne ikke åpnes.") }
                } else if (filtered.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("Ingen verk passer søket.") }
                } else {
                    items(filtered, key = { it.value.id }) { indexed ->
                        GalleryCard(work = indexed.value, onClick = { onSelect(indexed.index) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryTopBar(
    searchOpen: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .statusBarsPadding()
            .border(width = 0.dp, color = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\\$/",
                color = BloodMoon,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
                letterSpacing = (-2.2).sp
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "BLCKSWAN KUNST",
                color = Bone,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (searchOpen) Icons.Rounded.Close else Icons.Rounded.Search,
                    contentDescription = if (searchOpen) "Lukk søk" else "Søk",
                    tint = Bone
                )
            }
        }
        HorizontalDivider(color = Line)
        if (searchOpen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Panel)
                    .border(1.dp, Line, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Bone, fontSize = 16.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(BloodMoon),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (query.isBlank()) Text("Søk i 41 verk", color = Fog)
                        inner()
                    }
                )
            }
        }
    }
}

@Composable
private fun GalleryIntro() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 34.dp)
    ) {
        Text(
            text = "DIGITAL KUNSTSAMLING · 2022–2026",
            color = BloodMoon,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "Kunst som lever i kode, filer og spor.",
            color = Bone,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 15.dp)
        )
        Text(
            text = "Se verket først. Deretter får du en kort forklaring på vanlig språk, før du kan gå dypere inn i mening, metode og kontekst.",
            color = Fog,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
            color = Line
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Stat("41", "VERK")
            Stat("4", "KAPITLER")
            Stat("100%", "OFFLINE")
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column {
        Text(value, color = Bone, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = Fog, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun FeaturedWork(work: Work, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        ArtPreview(
            work = work,
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp),
            featured = true
        )
        Column(Modifier.padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("START HER", color = BloodMoon, style = MaterialTheme.typography.labelLarge)
                Text("01 / 41", color = BloodMoon, style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = work.title,
                color = Bone,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = work.what,
                color = Fog,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChapterStrip(chapters: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chapters) { chapter ->
            val active = chapter == selected
            Surface(
                color = if (active) Paper else Color.Transparent,
                contentColor = if (active) PaperText else Fog,
                shape = CircleShape,
                border = BorderStroke(1.dp, if (active) Paper else Line),
                modifier = Modifier.clickable { onSelect(chapter) }
            ) {
                Text(
                    text = if (chapter == "Alle") "ALLE VERK" else chapter.uppercase(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ResultLine(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$count VERK", color = Fog, style = MaterialTheme.typography.labelLarge)
        Text("TRYKK FOR Å ÅPNE", color = Fog.copy(alpha = 0.72f), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun GalleryCard(work: Work, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
    ) {
        ArtPreview(
            work = work,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = work.chapter.uppercase(),
                    color = BloodMoon,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(work.year, color = Fog, style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = work.title,
                color = Bone,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = work.what,
                color = Fog,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 9.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(modifier = Modifier.padding(top = 17.dp, bottom = 13.dp), color = Line)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(work.medium.uppercase(), color = Fog, style = MaterialTheme.typography.labelLarge)
                Text("SE VERKET  ↗", color = Bone, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ArtPreview(work: Work, modifier: Modifier = Modifier, featured: Boolean = false) {
    Box(
        modifier = modifier.background(Color(0xFF030303)),
        contentAlignment = Alignment.Center
    ) {
        if (work.artType == "text" && work.art.isNotBlank()) {
            val fontSize = previewFontSize(work.art, featured)
            Text(
                text = previewArt(work.art, if (featured) 24 else 18),
                color = Bone,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.15f).sp,
                softWrap = false,
                maxLines = if (featured) 24 else 18,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            RepositoryPlate(work)
        }
    }
}

@Composable
private fun RepositoryPlate(work: Work) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0C0C0D), Color(0xFF1A0E12), Color(0xFF0C0C0D))
                )
            )
            .padding(22.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("DIGITALT OBJEKT", color = BloodMoon, style = MaterialTheme.typography.labelLarge)
                Text(
                    "${work.profile}/${work.repo}",
                    color = Fog,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Text(
                "\\$/",
                color = Bone,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = (-2.2).sp
            )
        }
        Column {
            Text(
                work.title,
                color = Bone,
                fontWeight = FontWeight.Black,
                fontSize = 31.sp,
                lineHeight = 31.sp,
                letterSpacing = (-0.8).sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(work.medium, color = Fog, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 9.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(work.year, color = Fog, style = MaterialTheme.typography.labelLarge)
            Text("KILDEOBJEKT", color = Fog, style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
    Surface(color = Ink, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DetailTopBar(
                work = work,
                position = position,
                total = total,
                onBack = onBack,
                onShare = { onShare(work) }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 0.dp)
            ) {
                item { DetailArtStage(work) }
                item {
                    DetailPaper(
                        work = work,
                        onOpenUrl = onOpenUrl,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        onShare = { onShare(work) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    work: Work,
    position: Int,
    total: Int,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Tilbake", tint = Bone)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    work.title,
                    color = Bone,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${position + 1} / $total · ${work.chapter.uppercase()}",
                    color = Fog,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Rounded.Share, contentDescription = "Del", tint = Bone)
            }
        }
        HorizontalDivider(color = Line)
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
                .heightIn(min = 420.dp, max = 590.dp)
                .background(Color(0xFF020202))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ORIGINAL TEKST- / ASCII-FLATE", color = Fog, style = MaterialTheme.typography.labelLarge)
                Row {
                    IconButton(onClick = { fontSize = (fontSize - 1f).coerceAtLeast(5f) }) {
                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Mindre", tint = Bone)
                    }
                    IconButton(onClick = { fontSize = (fontSize + 1f).coerceAtMost(24f) }) {
                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Større", tint = Bone)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontal)
                    .verticalScroll(vertical)
                    .padding(22.dp)
            ) {
                Text(
                    text = work.art,
                    color = Bone,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.16f).sp,
                    softWrap = false
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(Color(0xFF020202))
                .padding(18.dp)
        ) {
            RepositoryPlate(work)
        }
    }
}

@Composable
private fun DetailPaper(
    work: Work,
    onOpenUrl: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Paper)
            .padding(horizontal = 20.dp, vertical = 30.dp)
            .navigationBarsPadding()
    ) {
        Text("${work.medium.uppercase()} · ${work.year}", color = BloodMoonDark, style = MaterialTheme.typography.labelLarge)
        Text(
            work.title,
            color = PaperText,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            work.what,
            color = Color(0xFF3F3D39),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        Surface(
            color = PaperSoft,
            shape = RoundedCornerShape(17.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("KORT FORKLART", color = BloodMoonDark, style = MaterialTheme.typography.labelLarge)
                Text(
                    shortSummary(work.meaning, 2),
                    color = PaperText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 9.dp)
                )
            }
        }

        PaperSection("HVA BETYR DET?", work.meaning)
        PaperSection("HVORDAN ER DET LAGET?", work.how)
        PaperSection("SE ETTER DETTE", work.look)
        PaperSection(
            "OM OBJEKTET",
            "Publisert av ${work.profile} i ${work.repo}. ${work.provenance}. Dokumentasjon: ${work.confidence}."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (work.sourceUrl.isNotBlank()) {
                Button(
                    onClick = { onOpenUrl(work.sourceUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = PaperText, contentColor = Paper),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ORIGINALKILDE", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(7.dp))
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(17.dp))
                }
            }
            Surface(
                color = Color.Transparent,
                contentColor = PaperText,
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFAAA69D)),
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onShare)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DEL VERKET", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(7.dp))
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                }
            }
        }

        work.links.forEach { link ->
            Surface(
                color = PaperSoft,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onOpenUrl(link.url) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(link.label, color = PaperText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = PaperText)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavButton("FORRIGE", Icons.Rounded.ChevronLeft, onPrevious, Modifier.weight(1f))
            NavButton("NESTE", Icons.Rounded.ChevronRight, onNext, Modifier.weight(1f), iconAfter = true)
        }
    }
}

@Composable
private fun PaperSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp)
    ) {
        HorizontalDivider(color = Color(0xFFC8C4BB))
        Text(
            title,
            color = BloodMoonDark,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            body,
            color = PaperMuted,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
}

@Composable
private fun NavButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconAfter: Boolean = false
) {
    Surface(
        color = PaperSoft,
        contentColor = PaperText,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!iconAfter) Icon(icon, contentDescription = null)
            if (!iconAfter) Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
            if (iconAfter) Spacer(Modifier.width(5.dp))
            if (iconAfter) Icon(icon, contentDescription = null)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\\$/", color = BloodMoon, fontFamily = FontFamily.Monospace, fontSize = 48.sp)
        Text(message, color = Fog, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 14.dp))
    }
}

private fun previewArt(art: String, maxLines: Int): String {
    val lines = art.lines().filterNot { it.isBlank() && art.lines().size > maxLines }
    if (lines.size <= maxLines) return lines.joinToString("\n")
    val start = ((lines.size - maxLines) / 2).coerceAtLeast(0)
    return lines.drop(start).take(maxLines).joinToString("\n")
}

private fun previewFontSize(art: String, featured: Boolean): Float {
    val maxLength = art.lineSequence().maxOfOrNull { it.length } ?: 1
    val base = when {
        maxLength > 130 -> 4.3f
        maxLength > 100 -> 5.0f
        maxLength > 76 -> 6.0f
        maxLength > 54 -> 7.2f
        maxLength > 36 -> 9.0f
        else -> 12.0f
    }
    return if (featured) base * 1.05f else base
}

private fun initialDetailFontSize(art: String): Float {
    val maxLength = art.lineSequence().maxOfOrNull { it.length } ?: 1
    return when {
        maxLength > 130 -> 6f
        maxLength > 100 -> 7f
        maxLength > 70 -> 8f
        maxLength > 45 -> 10f
        else -> 13f
    }
}

private fun shortSummary(text: String, sentences: Int = 1): String {
    val parts = text.split(Regex("(?<=[.!?])\\s+"))
    return parts.take(sentences).joinToString(" ").ifBlank { text }
}
