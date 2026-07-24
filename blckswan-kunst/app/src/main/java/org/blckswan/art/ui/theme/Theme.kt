package org.blckswan.art.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF040706)
val SurfaceDeep = Color(0xFF0A100D)
val SurfaceElevated = Color(0xFF121A16)
val Phosphor = Color(0xFF66F59A)
val Fog = Color(0xFFB4C4BA)
val MoonRed = Color(0xFFE23B5A)
val GlitchMagenta = Color(0xFFFF2D95)

private val BlckswanColorScheme = darkColorScheme(
    primary = Phosphor,
    onPrimary = Ink,
    secondary = Fog,
    onSecondary = Ink,
    background = Ink,
    onBackground = Color.White,
    surface = SurfaceDeep,
    onSurface = Color.White,
    surfaceVariant = SurfaceElevated,
    error = MoonRed
)

@Composable
fun BlckswanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlckswanColorScheme,
        typography = androidx.compose.material3.Typography(
            displayLarge = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = (-0.5).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            labelMedium = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            )
        ),
        content = content
    )
}
