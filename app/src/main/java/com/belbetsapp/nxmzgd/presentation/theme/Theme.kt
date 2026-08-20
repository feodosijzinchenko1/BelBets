package com.belbetsapp.nxmzgd.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BelBetsColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimarySoft,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = AccentGold,
    onSecondary = Color(0xFF1E1B4B),
    tertiary = LiveGreen,
    onTertiary = Color.White,
    background = SurfaceSoft,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BrandPrimarySoft,
    onSurfaceVariant = TextSecondary,
    outline = DividerGray,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun BelBetsTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BrandPrimaryDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = BelBetsColorScheme,
        typography = AppTypography,
        content = content
    )
}
