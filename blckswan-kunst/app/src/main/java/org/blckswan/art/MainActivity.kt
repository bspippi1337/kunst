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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private val Black = Color(0xFF050505)
private val Paper = Color(0xFFF2F0EB)
private val Grey = Color(0xFFAAA7A2)
private val Quiet = Color(0xFF6D6A70)
private val Red = Color(0xFFD52549)
private val Hairline = Color(0xFF252527)
private val Stage = Color(0xFF09090A)

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
        window.statusBarColor = android.graphics.Color.rgb(5, 5, 5)
        window.navigationBarColor = android.graphics.Color.rgb(5, 5, 5)
        val works = runCatching(::loadWorks).getOrElse { emptyList() }
        setContent {
            MaterialTheme(
                typography = MaterialTheme.typography.copy(
                    displayLarge = TextStyle(FontFamily.SansSerif, FontWeight.Black, 40.sp, 42.sp, (-1.3).sp),
                    headlineLarge = TextStyle(FontFamily.SansSerif, FontWeight.Black, 30.sp, 32.sp, (-0.8).sp),
                    headlineMedium = TextStyle(FontFamily.SansSerif, FontWeight.Bold, 22.sp, 25.sp, (-0.2).sp),
                    bodyLarge = TextStyle(FontFamily.SansSerif, FontWeight.Normal, 17.sp, 26.sp),
                    bodyMedium = TextStyle(FontFamily.SansSerif, FontWeight.Normal, 15.sp, 23.sp),
                    labelLarge = TextStyle(FontFamily.Monospace, FontWeight.Bold, 10.sp, 13.sp, 0.6.sp)
                )
            ) {
                GalleryApp(works, ::openUrl, ::shareWork)
            }
        }
    }

    private fun loadWorks(): List<Work> {
        val array = JSONObject(assets.open("gallery_v4.json").bufferedReader().use { it.readText() })
            .getJSONArray("works")
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val linksArray = item.optJSONArray("links")
                val links = buildList {
                    if (linksArray != null) for (i in 0 until linksArray.length()) {
                        val link = linksArray.getJSONObject(i)
                        val url = link.optString("url")
                        if (url.isNotBlank()) add(ArtLink(link.optString("label", "Kilde"), url))
                    }
                }
                add(
                    Work(
                        item.getString("id"), item.getString("title"), item.getString("profile"),
                        item.getString("repo"), item.getString("year"), item.getString("medium"),
                        item.getString("chapter"), item.getString("provenance"), item.getString("confidence"),
                        item.getString("what"), item.getString("meaning"), item.getString("how"),
                        item.getString("look"), item.optString("sourceUrl"), links,
                        item.getString("visualType"), item.getString("artType"), item.optString("art")
                    )
                )
            }
        }
    }

    private fun openUrl(url: String) {
        if (url.isNotBlank()) runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    private fun shareWork(work: Work) {
        val text = listOf(work.title, work.what, work.sourceUrl).filter { it.isNotBlank() }.joinToString("\n\n")
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, work.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Del verket"))
    }
}

@Composable
private fun GalleryApp(works: List<Work>, onOpenUrl: (String) -> Unit, onShare: (Work) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    BackHandler(selected >= 0) { selected = -1 }
    AnimatedContent(selected, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "gallery-v52") { index ->
        if (index in works.indices) {
            DetailScreen(
                work = works[index],
                position = index,
                total = works.size,
                onBack = { selected = -1 },
                onPrevious = { selected = (index - 1 + works.size) % works.size },
                onNext = { selected = (index + 1) % works.size },
                onOpenUrl = onOpenUrl,
                onShare = { onShare(works[index]) }
            )
        } else {
            GalleryHome(works) { selected = it }
        }
    }
}

@Composable
private fun GalleryHome(works: List<Work>, onSelect: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var chapter by rememberSaveable { mutableStateOf("Alle") }
    val chapters = remember(works) { listOf("Alle") + works.map { it.chapter }.distinct() }
    val filtered = remember(works, query, chapter) {
        works.withIndex().filter { indexed ->
            val work = indexed.value
            val matchesChapter = chapter == "Alle" || work.chapter == chapter
            val haystack = listOf(work.title, work.profile, work.repo, work.medium, work.chapter, work.what, work.meaning)
                .joinToString(" ").lowercase()
            matchesChapter && (query.isBlank() || haystack.contains(query.trim().lowercase()))
        }
    }

    Surface(color = Black, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { MinimalHeader(works.size) }
            item { IntroSearch(query, { query = it }) }
            item { ChapterStrip(chapters, chapter) { chapter = it } }
            item { CountLine(filtered.size) }
            when {
                works.isEmpty() -> item { EmptyState("Kunstarkivet kunne ikke åpnes.") }
                filtered.isEmpty() -> item { EmptyState("Ingen verk passer søket.") }
                else -> items(filtered, key = { it.value.id }) { indexed ->
                    ExhibitionItem(indexed.value, indexed.index, works.size) { onSelect(indexed.index) }
                }
            }
        }
    }
}

@Composable
private fun MinimalHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("\\$/", color = Red, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 27.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("BLCKSWAN KUNST", color = Paper, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.8.sp)
            Text("$count VERK · OFFLINE", color = Quiet, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun IntroSearch(query: String, onQueryChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp)) {
        Text("Se verket først.", color = Paper, style = MaterialTheme.typography.displayLarge)
        Text("Forklaring, metode og opphav ligger bak hvert verk.", color = Grey, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).background(Stage, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, null, tint = Quiet, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(9.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = Paper, fontSize = 15.sp),
                cursorBrush = SolidColor(Red),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isBlank()) Text("Søk i samlingen", color = Quiet, fontSize = 15.sp)
                    inner()
                }
            )
            if (query.isNotBlank()) IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Rounded.Close, "Tøm søk", tint = Quiet, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun ChapterStrip(chapters: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(chapters) { item ->
            val active = item == selected
            Text(
                text = if (item == "Alle") "ALLE" else item.uppercase(),
                color = if (active) Paper else Quiet,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable { onSelect(item) }.padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun CountLine(count: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$count VERK", color = Quiet, style = MaterialTheme.typography.labelLarge)
        Text("TRYKK FOR FORKLARING", color = Quiet, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ExhibitionItem(work: Work, position: Int, total: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(bottom = 34.dp)
    ) {
        ArtPreview(work, Modifier.fillMaxWidth().aspectRatio(cardAspect(work)))
        Column(Modifier.padding(horizontal = 18.dp, vertical = 15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(work.medium.uppercase(), color = Red, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Text(String.format("%02d / %02d", position + 1, total), color = Quiet, style = MaterialTheme.typography.labelLarge)
            }
            Text(work.title, color = Paper, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 8.dp))
            Text(shortSummary(work.what), color = Grey, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 7.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        HorizontalDivider(color = Hairline, modifier = Modifier.padding(horizontal = 18.dp))
    }
}

@Composable
private fun ArtPreview(work: Work, modifier: Modifier = Modifier) {
    Box(modifier.background(Stage), contentAlignment = Alignment.CenterStart) {
        if (work.artType == "text" && work.art.isNotBlank()) {
            BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
                val lines = remember(work.art) { work.art.lines() }
                val widest = lines.maxOfOrNull { it.length }?.coerceAtLeast(1) ?: 1
                val fitted = (maxWidth.value / (widest * 0.61f)).coerceIn(3.2f, 13f)
                Text(
                    text = work.art.trimEnd(),
                    color = Paper,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fitted.sp,
                    lineHeight = (fitted * 1.10f).sp,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        } else {
            SourceStage(work)
        }
    }
}

@Composable
private fun SourceStage(work: Work) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Text(work.visualType.uppercase(), color = Red, style = MaterialTheme.typography.labelLarge)
        Column {
            Text("\\$/", color = Paper.copy(alpha = 0.12f), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 76.sp)
            Text(work.title, color = Paper, style = MaterialTheme.typography.headlineLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("${work.profile}/${work.repo}", color = Quiet, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun DetailScreen(
    work: Work,
    position: Int,
    total: Int,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onShare: () -> Unit
) {
    Surface(color = Black, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().height(58.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Tilbake", tint = Paper) }
                Column(Modifier.weight(1f)) {
                    Text(work.title, color = Paper, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${position + 1} / $total", color = Quiet, style = MaterialTheme.typography.labelLarge)
                }
                IconButton(onClick = onShare) { Icon(Icons.Rounded.Share, "Del", tint = Paper) }
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item { DetailArt(work) }
                item { DetailCopy(work, onOpenUrl) }
                item { BottomNavigation(onPrevious, onNext) }
            }
        }
    }
}

@Composable
private fun DetailArt(work: Work) {
    if (work.artType == "text" && work.art.isNotBlank()) {
        var fontSize by rememberSaveable(work.id) { mutableFloatStateOf(detailFont(work.art)) }
        val horizontal = rememberScrollState()
        val vertical = rememberScrollState()
        Column(Modifier.fillMaxWidth().heightIn(min = 420.dp, max = 680.dp).background(Color.Black)) {
            Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ORIGINALFLATE", color = Quiet, style = MaterialTheme.typography.labelLarge)
                Row {
                    IconButton(onClick = { fontSize = (fontSize - 1f).coerceAtLeast(4f) }) { Icon(Icons.Rounded.ZoomOut, "Mindre", tint = Paper) }
                    IconButton(onClick = { fontSize = (fontSize + 1f).coerceAtMost(24f) }) { Icon(Icons.Rounded.ZoomIn, "Større", tint = Paper) }
                }
            }
            Box(Modifier.fillMaxSize().horizontalScroll(horizontal).verticalScroll(vertical).padding(16.dp), contentAlignment = Alignment.TopStart) {
                Text(work.art.trimEnd(), color = Paper, fontFamily = FontFamily.Monospace, fontSize = fontSize.sp, lineHeight = (fontSize * 1.12f).sp, softWrap = false)
            }
        }
    } else {
        ArtPreview(work, Modifier.fillMaxWidth().aspectRatio(1.05f))
    }
}

@Composable
private fun DetailCopy(work: Work, onOpenUrl: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
        Text("${work.medium.uppercase()} · ${work.year}", color = Red, style = MaterialTheme.typography.labelLarge)
        Text(work.title, color = Paper, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(top = 9.dp))
        Text(work.what, color = Grey, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 14.dp))
        Explanation("HVA BETYR DET?", work.meaning)
        Explanation("HVORDAN ER DET LAGET?", work.how)
        Explanation("SE ETTER DETTE", work.look)
        Explanation("ARKIV OG OPPHAV", "Publisert av ${work.profile} i ${work.repo}. ${work.provenance}. Dokumentasjon: ${work.confidence}.")
        if (work.sourceUrl.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp).clickable { onOpenUrl(work.sourceUrl) }.padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ÅPNE ORIGINALKILDEN", color = Paper, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.OpenInNew, null, tint = Paper)
            }
            HorizontalDivider(color = Hairline)
        }
        work.links.forEach { link ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onOpenUrl(link.url) }.padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(link.label.uppercase(), color = Grey, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.OpenInNew, null, tint = Quiet)
            }
            HorizontalDivider(color = Hairline)
        }
    }
}

@Composable
private fun Explanation(label: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(top = 28.dp)) {
        Text(label, color = Red, style = MaterialTheme.typography.labelLarge)
        Text(body, color = Grey, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun BottomNavigation(onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp).navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.clickable(onClick = onPrevious).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ChevronLeft, null, tint = Paper)
            Text("FORRIGE", color = Paper, style = MaterialTheme.typography.labelLarge)
        }
        Row(Modifier.clickable(onClick = onNext).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("NESTE", color = Paper, style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Rounded.ChevronRight, null, tint = Paper)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(Modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("\\$/", color = Red, fontFamily = FontFamily.Monospace, fontSize = 46.sp)
        Text(message, color = Grey, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
    }
}

private fun cardAspect(work: Work): Float = when {
    work.id == "git-log-graffiti" -> 2.75f
    work.artType == "text" && work.art.lines().size <= 12 -> 2.05f
    work.artType == "text" -> 1.35f
    else -> 1.32f
}

private fun detailFont(art: String): Float {
    val width = art.lineSequence().maxOfOrNull { it.length } ?: 1
    return when {
        width > 150 -> 5.0f
        width > 120 -> 5.8f
        width > 90 -> 6.8f
        width > 65 -> 8.2f
        width > 44 -> 10.0f
        else -> 13.0f
    }
}

private fun shortSummary(text: String): String = text.split(Regex("(?<=[.!?])\\s+")).firstOrNull().orEmpty().ifBlank { text }
