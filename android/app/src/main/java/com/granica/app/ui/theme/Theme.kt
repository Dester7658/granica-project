package com.granica.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val BrandIndigo = Color(0xFF3B4DFF)
private val BrandSky = Color(0xFF5AD6FF)
private val BrandTeal = Color(0xFF1ED8B4)
private val BrandCyan = Color(0xFF8AE3FF)
private val BrandAmber = Color(0xFFFFC857)

val GradientStart = Color(0xFF1D4ED8)
val GradientEnd = Color(0xFF0EA5A4)

private val LightColors = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    secondary = BrandTeal,
    onSecondary = Color.White,
    tertiary = BrandAmber,
    background = Color(0xFFF5F7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAF1FF),
    onSurface = Color(0xFF13233B),
    onSurfaceVariant = Color(0xFF5B6E8B),
    outline = Color(0xFFE4EBFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF98A9FF),
    secondary = Color(0xFF59E5D4),
    tertiary = Color(0xFFFFD166),
    background = Color(0xFF0B1220),
    surface = Color(0xFF121B2A),
    surfaceVariant = Color(0xFF1A2638),
    onSurface = Color(0xFFEAF2FF),
    onSurfaceVariant = Color(0xFF9DB0CB),
    outline = Color(0xFF2D3A52)
)

private val GranicaTypography = androidx.compose.material3.Typography(
    headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
    titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
    labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun GranicaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = GranicaTypography, content = content)
}
