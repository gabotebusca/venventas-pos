package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyDark,
    primaryContainer = NavySecondary,
    onPrimaryContainer = GoldLight,
    secondary = EmeraldSuccess,
    onSecondary = NavyDark,
    secondaryContainer = ForestGreen,
    onSecondaryContainer = SlateLight,
    tertiary = GoldLight,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = CoralError
)

private val LightColorScheme = lightColorScheme(
    primary = BentoBluePrimary,
    onPrimary = Color.White,
    primaryContainer = BentoBlueContainer,
    onPrimaryContainer = BentoBlueDark,
    secondary = BentoMintText,
    onSecondary = Color.White,
    secondaryContainer = BentoMintContainer,
    onSecondaryContainer = BentoMintText,
    tertiary = BentoAmberText,
    tertiaryContainer = BentoAmberContainer,
    onTertiaryContainer = BentoAmberText,
    background = BentoBackground,
    onBackground = BentoDarkText,
    surface = BentoCardBg,
    onSurface = BentoDarkText,
    surfaceVariant = BentoBackground,
    onSurfaceVariant = BentoSlateLabel,
    outline = BentoBorder,
    outlineVariant = BentoBorderSubtle,
    error = CoralError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
