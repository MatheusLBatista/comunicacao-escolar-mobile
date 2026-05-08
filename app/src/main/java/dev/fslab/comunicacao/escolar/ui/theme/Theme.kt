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
    val focusedIndicator: Color,
    val buttonContainer: Color,
    val buttonText: Color,
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
    focusedIndicator = PrimaryBlueDark,
    buttonContainer = PrimaryBlueDark,
    buttonText = Color.White,
    isDark = false
)

val DarkComunicacaoEscolarColors = ComunicacaoEscolarColors(
    background = Color(0xFF121212),
    backgroundGradientStart = Color(0xFF1E1E1E),
    backgroundGradientEnd = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFF9E9E9E),
    textTertiary = Color(0xFF757575),
    textOnPrimary = Color.White,
    textInput = Color(0xFFE0E0E0),
    primary = Color(0xFF455A64),
    primaryDark = Color(0xFF37474F),
    iconGray = Color(0xFF9E9E9E),
    inputBorder = Color(0xFF424242),
    mediumGray = Color(0xFF616161),
    errorBackground = Color(0xFF2C1515),
    errorText = Color(0xFFEF9A9A),
    errorButton = Color(0xFFE57373),
    error = Color(0xFFEF5350),
    successBackground = Color(0xFF1B2E22),
    successText = Color(0xFF80CBC4),
    success = Color(0xFF4CAF50),
    lightGray = Color(0xFF2C2C2C),
    featureBlue = Color(0xFF6B8AFF),
    featureGreen = Color(0xFF4ADE80),
    featureOrange = Color(0xFFFFBB5C),
    featureCyan = Color(0xFF22D3EE),
    featurePink = Color(0xFFF472B6),
    featureRed = Color(0xFFFF6B81),
    focusedIndicator = Color(0xFFE0E0E0),
    buttonContainer = Color(0xFFE0E0E0),
    buttonText = Color(0xFF121212),
    isDark = true
)

val LocalComunicacaoEscolarColors = compositionLocalOf { LightComunicacaoEscolarColors }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF455A64),
    secondary = Color(0xFF9E9E9E),
    tertiary = Color(0xFF37474F),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
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
            val insetsController = WindowCompat.getInsetsController(window, view)
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