package com.granica.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Deep indigo/teal duo — feels like a travel/navigation app, not a generic grey admin panel.
private val BrandIndigo = Color(0xFF3949AB)
private val BrandTeal = Color(0xFF00BFA5)
private val BrandAmber = Color(0xFFFFB300)

val GradientStart = Color(0xFF283593)
val GradientEnd = Color(0xFF00897B)

private val LightColors = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    secondary = BrandTeal,
    onSecondary = Color.White,
    tertiary = BrandAmber,
    background = Color(0xFFF4F6FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EAF6)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    secondary = Color(0xFF64FFDA),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF10131C),
    surface = Color(0xFF1A1F2E)
)

private val GranicaTypography = androidx.compose.material3.Typography(
    headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
    titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
    labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun GranicaTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = GranicaTypography, content = content)
}
