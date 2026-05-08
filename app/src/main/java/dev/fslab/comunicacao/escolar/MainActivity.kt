package dev.fslab.comunicacao.escolar

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dev.fslab.comunicacao.escolar.navigation.NavGraph
import dev.fslab.comunicacao.escolar.network.TokenManager
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeMode
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(applicationContext)

        lifecycleScope.launch {
            themeViewModel.themeMode.collect { mode ->
                val isDark = when (mode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                }
                val barStyle = if (isDark)
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }
        }

        setContent {
            ComunicacaoEscolarApp(themeViewModel = themeViewModel)
        }
    }
}

@Composable
fun ComunicacaoEscolarApp(themeViewModel: ThemeViewModel) {
    val themeMode by themeViewModel.themeMode.collectAsState()

    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    ComunicacaoEscolarTheme(darkTheme = isDarkTheme) {
        val colors = LocalComunicacaoEscolarColors.current
        val navController = rememberNavController()
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            NavGraph(navController = navController, themeViewModel = themeViewModel)
        }
    }
}

