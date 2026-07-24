package org.blckswan.art.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Official BLCKSWAN Brand Kit v1.0
val Ink = Color(0xFF0A0A0B)
val SurfaceDeep = Color(0xFF121214)
val SurfaceElevated = Color(0xFF1A1A1D)
val Phosphor = Color(0xFF00FF9C)
val Fog = Color(0xFFB8B8BC)
val PureWhite = Color(0xFFF2F2F2)
val MoonRed = Color(0xFFC41E3A)
val MoonDark = Color(0xFF8B0000)
val GlitchMagenta = Color(0xFFFF2A6D)

private val BlckswanColorScheme = darkColorScheme(
    primary = MoonRed,
    onPrimary = PureWhite,
    secondary = Phosphor,
    onSecondary = Ink,
    background = Ink,
    onBackground = PureWhite,
    surface = SurfaceDeep,
    onSurface = PureWhite,
    surfaceVariant = SurfaceElevated,
    error = GlitchMagenta
)

@Composable
fun BlckswanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlckswanColorScheme,
        typography = androidx.compose.material3.Typography(
            displayLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                letterSpacing = (-1.3).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = (-0.45).sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = (-0.25).sp
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
