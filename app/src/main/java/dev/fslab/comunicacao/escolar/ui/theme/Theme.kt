package dev.fslab.comunicacao.escolar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class ComunicacaoEscolarColors(
    val background: Color,
    val backgroundGradientStart: Color,
    val backgroundGradientEnd: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnPrimary: Color,
    val textInput: Color,
    val primary: Color,
    val primaryDark: Color,
    val iconGray: Color,
    val inputBorder: Color,
    val mediumGray: Color,
    val errorBackground: Color,
    val errorText: Color,
    val errorButton: Color,
    val error: Color,
    val successBackground: Color,
    val successText: Color,
    val success: Color,
    val lightGray: Color,
    val featureBlue: Color,
    val featureGreen: Color,
    val featureOrange: Color,
    val featureCyan: Color,
    val featurePink: Color,
    val featureRed: Color,
    val isDark: Boolean = false
)

val LightComunicacaoEscolarColors = ComunicacaoEscolarColors(
    background = SurfaceWhite,
    backgroundGradientStart = PrimaryLightBlue,
    backgroundGradientEnd = SurfaceLight,
    surface = LightGray,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    textOnPrimary = TextOnPrimary,
    textInput = TextBlack,
    primary = PrimaryBlue,
    primaryDark = PrimaryBlueDark,
    iconGray = IconGray,
    inputBorder = InputBorderGray,
    mediumGray = MediumGray,
    errorBackground = ErrorBackground,
    errorText = ErrorText,
    errorButton = ErrorButton,
    error = Color(0xFFDC2626),
    successBackground = SuccessBackground,
    successText = SuccessText,
    success = Color(0xFF10B981),
    lightGray = LightGray,
    featureBlue = Color(0xFF7C6AF6),
    featureGreen = Color(0xFF10B981),
    featureOrange = Color(0xFFF59E0B),
    featureCyan = Color(0xFF06B6D4),
    featurePink = Color(0xFFEC4899),
    featureRed = Color(0xFFEF4444),
    isDark = false
)

val DarkComunicacaoEscolarColors = ComunicacaoEscolarColors(
    background = Color(0xFF0D0B1E),
    backgroundGradientStart = Color(0xFF1A1533),
    backgroundGradientEnd = Color(0xFF0D0B1E),
    surface = Color(0xFF1E1A38),
    textPrimary = Color(0xFFEEEDF5),
    textSecondary = Color(0xFF9B97B8),
    textTertiary = Color(0xFF7B779A),
    textOnPrimary = Color.White,
    textInput = Color(0xFFEEEDF5),
    primary = Color(0xFF455A64),
    primaryDark = Color(0xFF263238),
    iconGray = Color(0xFF9B97B8),
    inputBorder = Color(0xFF2E2A4A),
    mediumGray = Color(0xFF5C587A),
    errorBackground = Color(0xFF3D1A2E),
    errorText = Color(0xFFFF8A9B),
    errorButton = Color(0xFFE05577),
    error = Color(0xFFFF6B81),
    successBackground = Color(0xFF132D24),
    successText = Color(0xFF6EEDB0),
    success = Color(0xFF4ADE80),
    lightGray = Color(0xFF252142),
    featureBlue = Color(0xFF6B8AFF),
    featureGreen = Color(0xFF4ADE80),
    featureOrange = Color(0xFFFFBB5C),
    featureCyan = Color(0xFF22D3EE),
    featurePink = Color(0xFFF472B6),
    featureRed = Color(0xFFFF6B81),
    isDark = true
)

val LocalComunicacaoEscolarColors = compositionLocalOf { LightComunicacaoEscolarColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF455A64),
    secondary = Color(0xFF9B97B8),
    tertiary = Color(0xFF263238),
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFEEEDF5),
    onSurface = Color(0xFFEEEDF5)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGray,
    tertiary = PrimaryBlueDark,
    background = SurfaceWhite,
    surface = LightGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun ComunicacaoEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val filaColors =
        if (darkTheme) DarkComunicacaoEscolarColors
        else LightComunicacaoEscolarColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = filaColors.background.toArgb()
            window.navigationBarColor = filaColors.background.toArgb()

            val insetsController =
                WindowCompat.getInsetsController(window, view)

            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalComunicacaoEscolarColors provides filaColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}