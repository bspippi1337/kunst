package org.blckswan.art.data

import android.content.Context
import org.blckswan.art.data.models.Exhibition
import org.blckswan.art.data.models.VisualMode
import org.blckswan.art.data.models.Work
import org.json.JSONObject

class ExhibitionRepository(private val context: Context) {

    fun loadExhibition(): Exhibition {
        return try {
            val raw = context.assets.open("exhibition.json")
                .bufferedReader()
                .use { it.readText() }
            parse(raw)
        } catch (t: Throwable) {
            Exhibition(
                title = "BLCKSWAN ART",
                subtitle = "Archive offline",
                edition = "Fallback mode",
                works = emptyList()
            )
        }
    }

    private fun parse(raw: String): Exhibition {
        val root = JSONObject(raw)
        val worksArray = root.optJSONArray("works") ?: return Exhibition()

        val works = mutableListOf<Work>()
        for (i in 0 until worksArray.length()) {
            val obj = worksArray.optJSONObject(i) ?: continue
            works += Work(
                id = obj.optString("id", "work_$i"),
                title = obj.optString("title", "Untitled"),
                medium = obj.optString("medium", "Kunst"),
                description = obj.optString("description", ""),
                sourceUrl = obj.optString("sourceUrl", ""),
                imageAsset = obj.optString("imageAsset").takeIf { it.isNotBlank() },
                visualMode = when (obj.optString("visualMode", "static").lowercase()) {
                    "glitch" -> VisualMode.GLITCH
                    "phosphor" -> VisualMode.PHOSPHOR
                    "interactive" -> VisualMode.INTERACTIVE
                    else -> VisualMode.STATIC
                },
                glitchIntensity = obj.optDouble("glitchIntensity", 0.35).toFloat().coerceIn(0f, 1f),
                audioAsset = obj.optString("audioAsset").takeIf { it.isNotBlank() }
            )
        }

        return Exhibition(
            title = root.optString("title", "BLCKSWAN ART"),
            subtitle = root.optString("subtitle", "GitHub er lerretet."),
            edition = root.optString("edition", "Spatial Exhibition"),
            works = works
        )
    }
}
