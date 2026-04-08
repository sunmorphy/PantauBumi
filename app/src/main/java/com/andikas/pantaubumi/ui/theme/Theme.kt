package com.andikas.pantaubumi.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object PantauBumiColors {

    // Primary
    val Green50 = Color(0xFFE5F2EB)
    val Green100 = Color(0xFFBCDFCA)
    val Green200 = Color(0xFF85C9A8)
    val Green400 = Color(0xFF2E7D5E)
    val Green600 = Color(0xFF1A5C42)
    val Green800 = Color(0xFF0F3D2B)
    val Green900 = Color(0xFF072318)

    // Accent
    val Sage50 = Color(0xFFF0F7F3)
    val Sage100 = Color(0xFFD5EBE0)
    val Sage200 = Color(0xFFADD4BF)
    val Sage400 = Color(0xFF6AAF90)
    val Sage600 = Color(0xFF3D8A67)
    val Sage800 = Color(0xFF1F5C40)
    val Sage900 = Color(0xFF0D3325)

    // Neutral
    val Earth50 = Color(0xFFF4F8F5)
    val Earth100 = Color(0xFFE4EDE7)
    val Earth200 = Color(0xFFC8D9CE)
    val Earth400 = Color(0xFF8AA898)
    val Earth600 = Color(0xFF536B5C)
    val Earth800 = Color(0xFF2A3B31)
    val Earth900 = Color(0xFF1F1810)

    // Semantic — risk levels
    val RiskHigh = Color(0xFFCC3B2A)
    val RiskMedium = Color(0xFFAA7B14)
    val RiskLow = Color(0xFF2E7D5E)

    val RiskHighBg = Color(0xFFFCECE9)
    val RiskMediumBg = Color(0xFFFCF3DC)
    val RiskLowBg = Color(0xFFE5F2EB)

    val RiskHighText = Color(0xFF6B1A10)
    val RiskMediumText = Color(0xFF5C4000)
    val RiskLowText = Color(0xFF0F3D2B)

    val UserColor = Color(0xFF09849A)
    val EvacuationColor = Color(0xFF4EAE0A)
    val FloodColor = Color(0xFF557FF1)
    val LandslideColor = Color(0xFF8C545D)
    val EarthquakeColor = Color(0xFFED2A1D)
    val UnknownColor = Color(0xFF797B84)
    val RouteColor = Color(0xFF52DEE5)
}

private val LightColors = lightColorScheme(
    primary = PantauBumiColors.Green400,
    onPrimary = Color.White,
    primaryContainer = PantauBumiColors.Green50,
    onPrimaryContainer = PantauBumiColors.Green800,

    secondary = PantauBumiColors.Sage400,
    onSecondary = Color.White,
    secondaryContainer = PantauBumiColors.Sage50,
    onSecondaryContainer = PantauBumiColors.Sage800,

    tertiary = PantauBumiColors.Earth400,
    onTertiary = Color.White,
    tertiaryContainer = PantauBumiColors.Earth50,
    onTertiaryContainer = PantauBumiColors.Earth800,

    background = PantauBumiColors.Earth50,
    onBackground = PantauBumiColors.Earth900,

    surface = Color.White,
    onSurface = PantauBumiColors.Earth900,
    surfaceVariant = PantauBumiColors.Earth100,
    onSurfaceVariant = PantauBumiColors.Earth600,

    outline = PantauBumiColors.Earth200,
    outlineVariant = PantauBumiColors.Earth100,

    error = PantauBumiColors.RiskHigh,
    onError = Color.White,

    errorContainer = PantauBumiColors.RiskHighBg,
    onErrorContainer = PantauBumiColors.RiskHighText,
)

private val DarkColors = darkColorScheme(
    primary = PantauBumiColors.Green200,
    onPrimary = PantauBumiColors.Green900,
    primaryContainer = PantauBumiColors.Green600,
    onPrimaryContainer = PantauBumiColors.Green50,

    secondary = PantauBumiColors.Sage200,
    onSecondary = PantauBumiColors.Sage900,
    secondaryContainer = PantauBumiColors.Sage800,
    onSecondaryContainer = PantauBumiColors.Sage50,

    tertiary = PantauBumiColors.Earth200,
    onTertiary = PantauBumiColors.Earth900,
    tertiaryContainer = PantauBumiColors.Earth800,
    onTertiaryContainer = PantauBumiColors.Earth50,

    background = PantauBumiColors.Earth900,
    onBackground = PantauBumiColors.Earth100,

    surface = PantauBumiColors.Earth800,
    onSurface = PantauBumiColors.Earth100,
    surfaceVariant = PantauBumiColors.Earth600,
    onSurfaceVariant = PantauBumiColors.Earth200,

    outline = PantauBumiColors.Earth600,
    outlineVariant = PantauBumiColors.Earth800,

    error = PantauBumiColors.RiskHigh,
    onError = Color.White,

    errorContainer = PantauBumiColors.RiskHighBg,
    onErrorContainer = PantauBumiColors.RiskHighText,
)

val PantauBumiTypography = androidx.compose.material3.Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

@Composable
fun PantauBumiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetBackgroundColor = if (darkTheme) DarkColors.background else LightColors.background
    val targetOnBackgroundColor =
        if (darkTheme) DarkColors.onBackground else LightColors.onBackground
    val targetSurfaceColor = if (darkTheme) DarkColors.surface else LightColors.surface
    val targetOnSurfaceColor = if (darkTheme) DarkColors.onSurface else LightColors.onSurface

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 700),
        label = "backgroundColor"
    )
    val onBackgroundColor by animateColorAsState(
        targetValue = targetOnBackgroundColor,
        animationSpec = tween(durationMillis = 700),
        label = "onBackgroundColor"
    )
    val surfaceColor by animateColorAsState(
        targetValue = targetSurfaceColor,
        animationSpec = tween(durationMillis = 700),
        label = "surfaceColor"
    )
    val onSurfaceColor by animateColorAsState(
        targetValue = targetOnSurfaceColor,
        animationSpec = tween(durationMillis = 700),
        label = "onSurfaceColor"
    )

    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors.copy(
            background = backgroundColor,
            onBackground = onBackgroundColor,
            surface = surfaceColor,
            onSurface = onSurfaceColor
        ),
        typography = PantauBumiTypography,
        content = content
    )
}