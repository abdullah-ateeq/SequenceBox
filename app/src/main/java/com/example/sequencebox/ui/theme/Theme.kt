package com.example.sequencebox.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.sequencebox.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = MathDarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = MathBlueSecondary,
    onSecondary = Color.White,
    background = MathDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MathDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = MathDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = AccentError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MathBluePrimary,
    onPrimary = Color.White,
    primaryContainer = MathBlueLight,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = MathBlueSecondary,
    onSecondary = Color.White,
    background = MathLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = MathLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MathLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = AccentError,
    onError = Color.White
)

@Composable
fun SequenceBoxTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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
