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

/**
 * Cores customizadas para a aplicação que mudam conforme o tema
 */
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
    // Cores para cards de features (variam entre light/dark)
    val featureBlue: Color,
    val featureGreen: Color,
    val featureOrange: Color,
    val featureCyan: Color,
    val featurePink: Color,
    val featureRed: Color,
    // Cores da LoginScreen
    val loginBackground: Color,
    val loginDarkBlue: Color,
    val loginGrayText: Color,
    val loginBorder: Color,
    val loginFooterGray: Color,
    val loginGoogleRed: Color,
    val isDark: Boolean = false  // Flag para identificar o tema
)

/**
 * Cores para tema claro — Paleta Purple/Violet premium
 */
val LightComunicacaoEscolarColors = ComunicacaoEscolarColors(
    background = PrimaryLightBlue,
    backgroundGradientStart = PrimaryLightBlue,
    backgroundGradientEnd = SurfaceLight,
    surface = SurfaceWhite,
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
    // Cores para cards de features (Light — roxo como destaque)
    featureBlue = Color(0xFF7C6AF6),
    featureGreen = Color(0xFF10B981),
    featureOrange = Color(0xFFF59E0B),
    featureCyan = Color(0xFF06B6D4),
    featurePink = Color(0xFFEC4899),
    featureRed = Color(0xFFEF4444),
    // Cores da LoginScreen
    loginBackground = LoginBackground,
    loginDarkBlue = DarkBlue,
    loginGrayText = LoginGrayText,
    loginBorder = LoginBorder,
    loginFooterGray = FooterGray,
    loginGoogleRed = GoogleRed,
    isDark = false
)

/**
 * Cores para tema escuro — Paleta Purple/Navy premium
 */
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
    primary = Color(0xFF7C6AF6),
    primaryDark = Color(0xFF5B4CCF),
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
    // Cores para cards de features (Dark — mais vibrantes sobre fundo escuro)
    featureBlue = Color(0xFF6B8AFF),
    featureGreen = Color(0xFF4ADE80),
    featureOrange = Color(0xFFFFBB5C),
    featureCyan = Color(0xFF22D3EE),
    featurePink = Color(0xFFF472B6),
    featureRed = Color(0xFFFF6B81),
    // Cores da LoginScreen (adaptadas para dark)
    loginBackground = Color(0xFF121212),
    loginDarkBlue = Color(0xFF5BA4CF),
    loginGrayText = Color(0xFFB0B0B0),
    loginBorder = Color(0xFF3A3A3A),
    loginFooterGray = Color(0xFF6B6B6B),
    loginGoogleRed = Color(0xFFFF6B6B),
    isDark = true
)

/**
 * CompositionLocal para acessar as cores customizadas
 * compositionLocalOf permite mudanças dinâmicas e propaga recomposição
 * ao invés de usar um objeto singleton, usamos CompositionLocal
 * para que as cores possam ser reativas e mudem conforme o tema sem precisar reiniciar a aplicação
 */
val LocalComunicacaoEscolarColors = compositionLocalOf { LightComunicacaoEscolarColors }

/**
 * DarkColorScheme - Paleta Material 3 para tema escuro (Purple/Navy)
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C6AF6),
    secondary = Color(0xFF9B97B8),
    tertiary = Color(0xFF5B4CCF),
    background = Color(0xFF0D0B1E),
    surface = Color(0xFF1E1A38),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFEEEDF5),
    onSurface = Color(0xFFEEEDF5)
)

/**
 * LightColorScheme - Paleta Material 3 para tema claro (Purple/Violet)
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGray,
    tertiary = PrimaryBlueDark,
    background = PrimaryLightBlue,
    surface = SurfaceWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

/**
 * ComunicacaoEscolarTheme - Função composable que aplica o tema à aplicação
 */
@Composable
fun ComunicacaoEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    // 1️⃣ Material Color Scheme
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

    // 2️⃣ Suas cores customizadas
    val filaColors =
        if (darkTheme) DarkComunicacaoEscolarColors
        else LightComunicacaoEscolarColors

    // 3️⃣ Controle da System UI
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = filaColors.background.toArgb()

            // Cor da Status Bar
            window.statusBarColor = filaColors.background.toArgb()

            // Cor da Navigation Bar
            window.navigationBarColor = filaColors.background.toArgb()

            val insetsController =
                WindowCompat.getInsetsController(window, view)

            // Define cor dos ícones:
            // true = ícones escuros
            // false = ícones claros
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // 4️⃣ Aplica tema
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