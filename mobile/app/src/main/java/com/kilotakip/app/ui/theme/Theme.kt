package com.kilotakip.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = Color(0xFFB9F6CA),
    secondary = TealSecondary,
    onSecondary = SurfaceLight,
    secondaryContainer = Color(0xFFA7FFEB),
    tertiary = OrangeAccent,
    onTertiary = SurfaceLight,
    tertiaryContainer = Color(0xFFFFE0B2),
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = BackgroundDark,
    primaryContainer = Color(0xFF004D25),
    secondary = Color(0xFF64FFDA),
    onSecondary = BackgroundDark,
    secondaryContainer = Color(0xFF00493D),
    tertiary = OrangeAccentLight,
    onTertiary = BackgroundDark,
    tertiaryContainer = Color(0xFF5C3300),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed
)

@Composable
fun KiloTakipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KiloTakipTypography,
        content = content
    )
}
