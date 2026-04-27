package com.kynzai.petmates.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PetMatesPrimary,
    background = PetMatesBackgroundDark,
    surface = PetMatesSurfaceDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = PetMatesTextSecondary,
)

private val LightColorScheme = lightColorScheme(
    primary = PetMatesPrimary,
    background = PetMatesBackground,
    surface = PetMatesSurface,
    onPrimary = Color.White,
    onBackground = PetMatesTextPrimary,
    onSurface = PetMatesTextPrimary,
    onSurfaceVariant = PetMatesTextSecondary,

    /* Остальные стандартные цвета можно переопределить позже
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PetMatesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color доступен на Android 12+, но для PetMates оставляем фиксированную бренд-палитру.
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
