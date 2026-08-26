package com.lugaresi.layercalc.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LayerCalcOrange,
    onPrimary = Color.Black,
    primaryContainer = LayerCalcOrangeContainer,
    onPrimaryContainer = LayerCalcDarkTextPrimary,

    secondary = LayerCalcCyan,
    onSecondary = Color.Black,
    secondaryContainer = LayerCalcCyanContainer,
    onSecondaryContainer = LayerCalcDarkTextPrimary,

    tertiary = LayerCalcSuccess,
    onTertiary = Color.Black,

    background = LayerCalcDarkBackground,
    onBackground = LayerCalcDarkTextPrimary,

    surface = LayerCalcDarkSurface,
    onSurface = LayerCalcDarkTextPrimary,
    surfaceVariant = LayerCalcDarkSurfaceVariant,
    onSurfaceVariant = LayerCalcDarkTextSecondary,

    outline = LayerCalcDarkOutline,
    outlineVariant = LayerCalcDarkOutline.copy(alpha = 0.65f),

    error = LayerCalcError,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = LayerCalcOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFFFE0CC),
    onPrimaryContainer = LayerCalcLightTextPrimary,

    secondary = Color(0xFF00677A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8F0FA),
    onSecondaryContainer = LayerCalcLightTextPrimary,

    tertiary = Color(0xFF147D4D),
    onTertiary = Color.White,

    background = LayerCalcLightBackground,
    onBackground = LayerCalcLightTextPrimary,

    surface = LayerCalcLightSurface,
    onSurface = LayerCalcLightTextPrimary,
    surfaceVariant = LayerCalcLightSurfaceVariant,
    onSurfaceVariant = LayerCalcLightTextSecondary,

    outline = LayerCalcLightOutline,
    outlineVariant = LayerCalcLightOutline.copy(alpha = 0.75f),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun LayerCalcStudioTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
