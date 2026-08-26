package com.lugaresi.layercalc.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * LayerCalc Studio color system
 *
 * Base oscura inspirada en herramientas técnicas / paneles de telemetría.
 * Los nombres nuevos describen la función del color, no una pantalla concreta,
 * para poder reutilizarlos después desde MaterialTheme.
 */

// Brand / acciones principales
val LayerCalcOrange = Color(0xFFFF7A1A)
val LayerCalcOrangeContainer = Color(0xFF4A2208)
val LayerCalcCyan = Color(0xFF35D7F3)
val LayerCalcCyanContainer = Color(0xFF073842)

// Dark theme
val LayerCalcDarkBackground = Color(0xFF0B0D10)
val LayerCalcDarkSurface = Color(0xFF13171C)
val LayerCalcDarkSurfaceVariant = Color(0xFF1B2027)
val LayerCalcDarkOutline = Color(0xFF38414C)
val LayerCalcDarkTextPrimary = Color(0xFFF2F4F7)
val LayerCalcDarkTextSecondary = Color(0xFFAAB2BD)

// Light theme
val LayerCalcLightBackground = Color(0xFFF7F8FA)
val LayerCalcLightSurface = Color(0xFFFFFFFF)
val LayerCalcLightSurfaceVariant = Color(0xFFE9EDF2)
val LayerCalcLightOutline = Color(0xFFC4CBD4)
val LayerCalcLightTextPrimary = Color(0xFF161A1F)
val LayerCalcLightTextSecondary = Color(0xFF59616C)

// Estados semánticos para futuras validaciones y resultados
val LayerCalcSuccess = Color(0xFF4DD58A)
val LayerCalcWarning = Color(0xFFFFC857)
val LayerCalcError = Color(0xFFFF6B6B)

/*
 * Compatibilidad con el Theme.kt actual.
 * Los mantenemos hasta que actualicemos Theme.kt en el siguiente paso.
 */
val DarkBackground = LayerCalcDarkBackground
val CardSurface = LayerCalcDarkSurface
val FilamentOrange = LayerCalcOrange
val TelemetryCyan = LayerCalcCyan
val TextPrimary = LayerCalcDarkTextPrimary
val TextSecondary = LayerCalcDarkTextSecondary

// Compatibilidad con los nombres generados originalmente por la plantilla.
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)
