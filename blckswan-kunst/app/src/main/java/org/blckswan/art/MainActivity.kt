package org.blckswan.art

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private val Void = Color(0xFF070708)
private val Stage = Color(0xFF0B0B0C)
private val Panel = Color(0xFF111113)
private val Line = Color(0xFF2A2A2F)
private val Bone = Color(0xFFF3F1EC)
private val Fog = Color(0xFFA7A39E)
private val Muted = Color(0xFF716E74)
private val BloodMoon = Color(0xFFD52549)
private val BloodMoonSoft = Color(0xFF321017)
private val Signal = Color(0xFF62E6B5)

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
        window.statusBarColor = android.graphics.Color.rgb(7, 7, 8)
        window.navigationBarColor = android.graphics.Color.rgb(7, 7, 8)

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
        val body = buildString {
            append(work.title)
            append("\n\n")
            append(work.what)
            if (work.sourceUrl.isNotBlank()) append("\n\n${work.sourceUrl}")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, work.title)
            putExtra(Intent.EXTRA_TEXT, body)
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
                fontSize = 38.sp,
                lineHeight = 40.sp,
                letterSpacing = (-1.2).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 29.sp,
                lineHeight = 31.sp,
                letterSpacing = (-0.65).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.25).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                lineHeight = 21.sp
            ),
            labelLarge = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.55.sp
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
        label = "gallery-v5-screen"
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
                onShare = { onShare(works[index]) }
            )
        } else {
            GalleryHome(works = works, onSelect = { selectedIndex = it })
        }
    }
}

@Composable
private fun GalleryHome(works: List<Work>, onSelect: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedChapter by rememberSaveable { mutableStateOf("Alle") }
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

    Surface(color = Void, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            CompactHeader(works.size)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { IntroAndSearch(query = query, onQueryChange = { query = it }) }
                item {
                    ChapterStrip(
                        chapters = chapters,
                        selected = selectedChapter,
                        onSelect = { selectedChapter = it }
                    )
                }
                item { ResultLine(filtered.size) }

                when {
                    works.isEmpty() -> item { EmptyState("Kunstarkivet kunne ikke åpnes.") }
                    filtered.isEmpty() -> item { EmptyState("Ingen verk passer søket.") }
                    else -> items(filtered, key = { it.value.id }) { indexed ->
                        GalleryCard(
                            work = indexed.value,
                            position = indexed.index,
                            total = works.size,
                            onClick = { onSelect(indexed.index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Void)
            .statusBarsPadding()
            .height(62.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = BloodMoon,
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "\\$/",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = (-1.8).sp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "BLCKSWAN KUNST",
                color = Bone,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 0.8.sp
            )
            Text("$count VERK · OFFLINE", color = Muted, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.weight(1f))
        Text("\\$/", color = BloodMoon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun IntroAndSearch(query: String, onQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "Se verket.\nLes sporet.",
            color = Bone,
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            "Kunstflaten først. Forklaringen ligger ett trykk unna.",
            color = Fog,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Panel)
                .border(1.dp, Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = Fog, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = Bone, fontSize = 15.sp),
                cursorBrush = SolidColor(BloodMoon),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isBlank()) Text("Søk i verk, medium eller repo", color = Muted, fontSize = 15.sp)
                    inner()
                }
            )
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tøm søk", tint = Fog, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ChapterStrip(chapters: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chapters) { chapter ->
            val active = chapter == selected
            Surface(
                color = if (active) BloodMoonSoft else Panel,
                contentColor = if (active) Bone else Fog,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (active) BloodMoon else Line),
                modifier = Modifier.clickable { onSelect(chapter) }
            ) {
                Text(
                    text = if (chapter == "Alle") "ALLE" else chapter.uppercase(),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
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
            .padding(horizontal = 18.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(Signal, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("$count VERK", color = Fog, style = MaterialTheme.typography.labelLarge)
        }
        Text("TRYKK FOR Å ÅPNE", color = Muted, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun GalleryCard(
    work: Work,
    position: Int,
    total: Int,
    onClick: () -> Unit
) {
    Surface(
        color = Panel,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box {
                ArtPreview(
                    work = work,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.22f)
                )
                Surface(
                    color = Void.copy(alpha = 0.84f),
                    shape = RoundedCornerShape(bottomStart = 10.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        "${position + 1} / $total",
                        color = Bone,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        work.medium.uppercase(),
                        color = BloodMoon,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(work.year, color = Fog, style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    work.title,
                    color = Bone,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    shortSummary(work.what, 1),
                    color = Fog,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 7.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(color = Line, modifier = Modifier.padding(top = 13.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(work.chapter, color = Muted, style = MaterialTheme.typography.labelLarge)
                    Text("SE VERKET  →", color = Bone, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ArtPreview(work: Work, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Stage),
        contentAlignment = Alignment.Center
    ) {
        if (work.artType == "text" && work.art.isNotBlank()) {
            val fontSize = previewFontSize(work.art)
            Text(
                text = previewArt(work.art, 20),
                color = Bone,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.12f).sp,
                softWrap = false,
                maxLines = 20,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(14.dp)
            )
        } else {
            SourcePortrait(work)
        }
    }
}

@Composable
private fun SourcePortrait(work: Work) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(BloodMoonSoft, Stage),
                    radius = 720f
                )
            )
            .padding(22.dp)
    ) {
        Text(
            work.visualType.uppercase(),
            color = BloodMoon,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            "\\$/",
            color = Bone.copy(alpha = 0.12f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 92.sp,
            letterSpacing = (-8).sp,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                work.title,
                color = Bone,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${work.profile}/${work.repo}",
                color = Fog,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 9.dp)
            )
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
    onShare: () -> Unit
) {
    Surface(color = Void, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(work, position, total, onBack, onShare)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 22.dp)
            ) {
                item { DetailArtStage(work) }
                item { WorkExplanation(work, onOpenUrl, onShare) }
                item { DetailNavigation(onPrevious, onNext) }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    work: Work,
    position: Int,
    total: Int,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Void)
            .statusBarsPadding()
            .height(58.dp)
            .padding(horizontal = 4.dp),
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
                "${position + 1} / $total · ${work.medium.uppercase()}",
                color = Muted,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Rounded.Share, contentDescription = "Del", tint = Bone)
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
                .heightIn(min = 430.dp, max = 650.dp)
                .background(Color.Black)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ORIGINALFLATE", color = Muted, style = MaterialTheme.typography.labelLarge)
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
                    .padding(16.dp)
            ) {
                Text(
                    work.art,
                    color = Bone,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.14f).sp,
                    softWrap = false
                )
            }
        }
    } else {
        ArtPreview(
            work = work,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f)
        )
    }
}

@Composable
private fun WorkExplanation(
    work: Work,
    onOpenUrl: (String) -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Void)
            .padding(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Text(
            "${work.chapter.uppercase()} · ${work.year}",
            color = BloodMoon,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            work.title,
            color = Bone,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            work.what,
            color = Fog,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 13.dp)
        )

        SummaryPanel(shortSummary(work.meaning, 2))
        ExpandableSection("HVA BETYR DET?", work.meaning, initiallyExpanded = true)
        ExpandableSection("HVORDAN ER DET LAGET?", work.how)
        ExpandableSection("SE ETTER DETTE", work.look)
        ExpandableSection(
            "ARKIV OG OPPHAV",
            "Publisert av ${work.profile} i ${work.repo}. ${work.provenance}. Dokumentasjon: ${work.confidence}."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (work.sourceUrl.isNotBlank()) {
                ActionButton(
                    label = "ORIGINALKILDE",
                    icon = Icons.Rounded.OpenInNew,
                    onClick = { onOpenUrl(work.sourceUrl) },
                    modifier = Modifier.weight(1f),
                    primary = true
                )
            }
            ActionButton(
                label = "DEL",
                icon = Icons.Rounded.Share,
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
        }

        work.links.forEach { link ->
            Surface(
                color = Panel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp)
                    .clickable { onOpenUrl(link.url) }
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(link.label, color = Bone, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = Fog)
                }
            }
        }
    }
}

@Composable
private fun SummaryPanel(body: String) {
    Surface(
        color = BloodMoonSoft,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BloodMoon.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Column(Modifier.padding(17.dp)) {
            Text("KORT FORTALT", color = BloodMoon, style = MaterialTheme.typography.labelLarge)
            Text(
                body,
                color = Bone,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    body: String,
    initiallyExpanded: Boolean = false
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 9.dp)
            .clickable { expanded = !expanded }
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Bone, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Fog
                )
            }
            AnimatedVisibility(expanded) {
                Column {
                    HorizontalDivider(color = Line)
                    Text(body, color = Fog, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(15.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false
) {
    Surface(
        color = if (primary) Bone else Panel,
        contentColor = if (primary) Void else Bone,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (primary) Bone else Line),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(7.dp))
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun DetailNavigation(onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NavigationButton("FORRIGE", Icons.Rounded.ChevronLeft, onPrevious, Modifier.weight(1f), false)
        NavigationButton("NESTE", Icons.Rounded.ChevronRight, onNext, Modifier.weight(1f), true)
    }
}

@Composable
private fun NavigationButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier,
    iconAfter: Boolean
) {
    Surface(
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Line),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 15.dp),
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
            .padding(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\\$/", color = BloodMoon, fontFamily = FontFamily.Monospace, fontSize = 46.sp)
        Text(message, color = Fog, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
    }
}

private fun previewArt(art: String, maxLines: Int): String {
    val sourceLines = art.lines()
    val lines = sourceLines.filterNot { it.isBlank() && sourceLines.size > maxLines }
    if (lines.size <= maxLines) return lines.joinToString("\n")
    val start = ((lines.size - maxLines) / 2).coerceAtLeast(0)
    return lines.drop(start).take(maxLines).joinToString("\n")
}

private fun previewFontSize(art: String): Float {
    val maxLength = art.lineSequence().maxOfOrNull { it.length } ?: 1
    return when {
        maxLength > 160 -> 3.5f
        maxLength > 130 -> 4.0f
        maxLength > 100 -> 4.7f
        maxLength > 76 -> 5.7f
        maxLength > 54 -> 7.0f
        maxLength > 36 -> 8.8f
        else -> 11.5f
    }
}

private fun initialDetailFontSize(art: String): Float {
    val maxLength = art.lineSequence().maxOfOrNull { it.length } ?: 1
    return when {
        maxLength > 150 -> 5.5f
        maxLength > 120 -> 6.2f
        maxLength > 90 -> 7.2f
        maxLength > 65 -> 8.5f
        maxLength > 44 -> 10.5f
        else -> 13.5f
    }
}

private fun shortSummary(text: String, sentences: Int = 1): String {
    val parts = text.split(Regex("(?<=[.!?])\\s+"))
    return parts.take(sentences).joinToString(" ").ifBlank { text }
}
