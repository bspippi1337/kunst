package org.blckswan.art.data.models

data class Exhibition(
    val title: String = "BLCKSWAN ART",
    val subtitle: String = "GitHub er lerretet.",
    val edition: String = "Spatial Exhibition v2",
    val works: List<Work> = emptyList()
)

data class Work(
    val id: String,
    val title: String,
    val medium: String = "Digital",
    val description: String = "",
    val sourceUrl: String = "",
    val imageAsset: String? = null,          // assets/works/...
    val visualMode: VisualMode = VisualMode.STATIC,
    val glitchIntensity: Float = 0.35f,
    val audioAsset: String? = null
)

enum class VisualMode {
    STATIC,
    GLITCH,
    PHOSPHOR,
    INTERACTIVE
}
