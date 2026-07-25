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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
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

private val Ink = Color(0xFF050506)
private val Stage = Color(0xFF09090A)
private val Panel = Color(0xFF111113)
private val PanelRaised = Color(0xFF18181B)
private val Line = Color(0xFF2B2B30)
private val Bone = Color(0xFFF5F3EE)
private val Fog = Color(0xFFA7A4A0)
private val Muted = Color(0xFF77747A)
private val BloodMoon = Color(0xFFD32345)
private val BloodMoonDeep = Color(0xFF7B1024)

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
        window.statusBarColor = android.graphics.Color.rgb(5, 5, 6)
        window.navigationBarColor = android.graphics.Color.rgb(5, 5, 6)

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
                fontSize = 42.sp,
                lineHeight = 43.sp,
                letterSpacing = (-1.5).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.9).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.35).sp
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
                letterSpacing = 0.7.sp
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
        label = "gallery-screen"
    ) { index ->
        if (index in works.indices) {
            ArtworkDetail(
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
            ArtworkFeed(works = works, onSelect = { selectedIndex = it })
        }
    }
}

@Composable
private fun ArtworkFeed(works: List<Work>, onSelect: (Int) -> Unit) {
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
            GalleryHeader(
                count = works.size,
                searchOpen = searchOpen,
                query = query,
                onQueryChange = { query = it },
                onToggleSearch = {
                    searchOpen = !searchOpen
                    if (!searchOpen) query = ""
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 44.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { CollectionIntro() }
                item {
                    ChapterStrip(
                        chapters = chapters,
                        selected = selectedChapter,
                        onSelect = { selectedChapter = it }
                    )
                }
                item { ResultLine(filtered.size) }

                if (works.isEmpty()) {
                    item { EmptyState("Kunstarkivet kunne ikke åpnes.") }
                } else if (filtered.isEmpty()) {
                    item { EmptyState("Ingen verk passer søket.") }
                } else {
                    items(filtered, key = { it.value.id }) { indexed ->
                        ArtworkFeedCard(
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
private fun GalleryHeader(
    count: Int,
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\\$/",
                color = BloodMoon,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
                letterSpacing = (-2.1).sp
            )
            Spacer(Modifier.width(13.dp))
            Column {
                Text(
                    text = "BLCKSWAN KUNST",
                    color = Bone,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "$count VERK · OFFLINE",
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (searchOpen) Icons.Rounded.Close else Icons.Rounded.Search,
                    contentDescription = if (searchOpen) "Lukk søk" else "Søk",
                    tint = Bone
                )
            }
        }
        AnimatedVisibility(searchOpen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Panel)
                    .border(1.dp, Line, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Bone, fontSize = 16.sp),
                    cursorBrush = SolidColor(BloodMoon),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (query.isBlank()) Text("Søk i titler, verk og tema", color = Fog)
                        inner()
                    }
                )
            }
        }
        HorizontalDivider(color = Line)
    }
}

@Composable
private fun CollectionIntro() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 28.dp, bottom = 4.dp)
    ) {
        Text(
            text = "DIGITAL KUNSTSAMLING · 2022—2026",
            color = BloodMoon,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "Se kunsten.\nForstå sporene.",
            color = Bone,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Trykk på et verk for full visning og en forklaring uten kunstspråk.",
            color = Fog,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 13.dp)
        )
    }
}

@Composable
private fun ChapterStrip(chapters: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chapters) { chapter ->
            val active = chapter == selected
            Surface(
                color = if (active) Bone else Panel,
                contentColor = if (active) Ink else Fog,
                shape = CircleShape,
                border = BorderStroke(1.dp, if (active) Bone else Line),
                modifier = Modifier.clickable { onSelect(chapter) }
            ) {
                Text(
                    text = if (chapter == "Alle") "ALLE" else chapter.uppercase(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$count VERK", color = Fog, style = MaterialTheme.typography.labelLarge)
        Text("BILDE FØRST · FORKLARING ETTERPÅ", color = Muted, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ArtworkFeedCard(
    work: Work,
    position: Int,
    total: Int,
    onClick: () -> Unit
) {
    Surface(
        color = Panel,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box {
                ArtPreview(
                    work = work,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.84f)
                )
                Surface(
                    color = Ink.copy(alpha = 0.78f),
                    contentColor = Bone,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "${position + 1} / $total",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Column(Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
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
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 11.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = work.what,
                    color = Fog,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(work.medium.uppercase(), color = Muted, style = MaterialTheme.typography.labelLarge)
                    Text("ÅPNE  →", color = Bone, style = MaterialTheme.typography.labelLarge)
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
                text = previewArt(work.art, 26),
                color = Bone,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.14f).sp,
                softWrap = false,
                maxLines = 26,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(12.dp)
            )
        } else {
            SourcePortrait(work)
        }
    }
}

@Composable
private fun SourcePortrait(work: Work) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Stage, Color(0xFF12080B), Stage)
                )
            )
            .padding(22.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = work.visualType.uppercase(),
                color = BloodMoon,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "\\$/",
                color = Bone.copy(alpha = 0.9f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                letterSpacing = (-2.3).sp
            )
        }

        Column {
            Text(
                text = work.title,
                color = Bone,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${work.profile}/${work.repo}",
                color = Fog,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(work.medium.uppercase(), color = Muted, style = MaterialTheme.typography.labelLarge)
            Text(work.year, color = Muted, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ArtworkDetail(
    work: Work,
    position: Int,
    total: Int,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onShare: () -> Unit
) {
    Surface(color = Ink, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(
                work = work,
                position = position,
                total = total,
                onBack = onBack,
                onShare = onShare
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { DetailArtStage(work) }
                item {
                    WorkExplanation(
                        work = work,
                        onOpenUrl = onOpenUrl,
                        onShare = onShare
                    )
                }
                item {
                    DetailNavigation(
                        onPrevious = onPrevious,
                        onNext = onNext
                    )
                }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 6.dp),
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
                .heightIn(min = 500.dp, max = 720.dp)
                .background(Color.Black)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 5.dp),
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
                    .padding(18.dp)
            ) {
                Text(
                    text = work.art,
                    color = Bone,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.15f).sp,
                    softWrap = false
                )
            }
        }
    } else {
        ArtPreview(
            work = work,
            modifier = Modifier
                .fillMaxWidth()
                .height(590.dp)
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
            .background(Ink)
            .padding(horizontal = 18.dp, vertical = 26.dp)
    ) {
        Text(
            text = "${work.chapter.uppercase()} · ${work.year}",
            color = BloodMoon,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = work.title,
            color = Bone,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 11.dp)
        )

        ExplanationLead(
            label = "HVA SER JEG?",
            body = work.what
        )
        ExplanationLead(
            label = "KORT FORTALT",
            body = shortSummary(work.meaning, 2),
            accent = true
        )

        Spacer(Modifier.height(10.dp))
        ExpandableExplanation("HVA BETYR DET?", work.meaning, initiallyExpanded = true)
        ExpandableExplanation("HVORDAN ER DET LAGET?", work.how)
        ExpandableExplanation("SE ETTER DETTE", work.look)
        ExpandableExplanation(
            "BAKGRUNN OG OPPHAV",
            "Publisert av ${work.profile} i ${work.repo}. ${work.provenance}. Dokumentasjon: ${work.confidence}."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (work.sourceUrl.isNotBlank()) {
                ActionPill(
                    label = "ORIGINALKILDE",
                    icon = Icons.Rounded.OpenInNew,
                    onClick = { onOpenUrl(work.sourceUrl) },
                    modifier = Modifier.weight(1f),
                    primary = true
                )
            }
            ActionPill(
                label = "DEL",
                icon = Icons.Rounded.Share,
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
        }

        work.links.forEach { link ->
            Surface(
                color = Panel,
                contentColor = Bone,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onOpenUrl(link.url) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        link.label,
                        color = Bone,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = Fog)
                }
            }
        }
    }
}

@Composable
private fun ExplanationLead(label: String, body: String, accent: Boolean = false) {
    Surface(
        color = if (accent) Color(0xFF1B0B0F) else Panel,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (accent) BloodMoonDeep else Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(label, color = if (accent) BloodMoon else Fog, style = MaterialTheme.typography.labelLarge)
            Text(
                text = body,
                color = Bone,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (accent) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ExpandableExplanation(
    title: String,
    body: String,
    initiallyExpanded: Boolean = false
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }

    Surface(
        color = Color.Transparent,
        contentColor = Bone,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable { expanded = !expanded }
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = BloodMoon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    color = Bone,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Fog
                )
            }
            AnimatedVisibility(expanded) {
                Column {
                    HorizontalDivider(color = Line)
                    Text(
                        text = body,
                        color = Fog,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false
) {
    Surface(
        color = if (primary) Bone else Panel,
        contentColor = if (primary) Ink else Bone,
        shape = CircleShape,
        border = BorderStroke(1.dp, if (primary) Bone else Line),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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
private fun DetailNavigation(
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NavigationPill(
            label = "FORRIGE",
            icon = Icons.Rounded.ChevronLeft,
            onClick = onPrevious,
            modifier = Modifier.weight(1f)
        )
        NavigationPill(
            label = "NESTE",
            icon = Icons.Rounded.ChevronRight,
            onClick = onNext,
            modifier = Modifier.weight(1f),
            iconAfter = true
        )
    }
}

@Composable
private fun NavigationPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconAfter: Boolean = false
) {
    Surface(
        color = Panel,
        contentColor = Bone,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Line),
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
        Text(
            message,
            color = Fog,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

private fun previewArt(art: String, maxLines: Int): String {
    val lines = art.lines().filterNot { it.isBlank() && art.lines().size > maxLines }
    if (lines.size <= maxLines) return lines.joinToString("\n")
    val start = ((lines.size - maxLines) / 2).coerceAtLeast(0)
    return lines.drop(start).take(maxLines).joinToString("\n")
}

private fun previewFontSize(art: String): Float {
    val maxLength = art.lineSequence().maxOfOrNull { it.length } ?: 1
    return when {
        maxLength > 150 -> 3.9f
        maxLength > 120 -> 4.5f
        maxLength > 96 -> 5.2f
        maxLength > 72 -> 6.2f
        maxLength > 52 -> 7.6f
        maxLength > 34 -> 9.2f
        else -> 12.5f
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
