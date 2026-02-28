package dev.fslab.comunicacao.escolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.screens.DailyLogsScreen
import dev.fslab.comunicacao.escolar.ui.theme.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComunicacaoEscolarApp()
        }
    }
}

@Composable
fun ComunicacaoEscolarApp() {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }

    ComunicacaoEscolarTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") {
                LoginScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onEsqueciSenha = { email ->
                        navController.navigate("esqueci_senha?email=$email")
                    },
                    onRegister = { navController.navigate("cadastro") },
                    onLogin = { navController.navigate("dailyLogs") }
                )
            }

            composable("dailyLogs") {
                DailyLogsScreen()
            }
//            composable(
//                route = "esqueci_senha?email={email}",
//                arguments = listOf(
//                    navArgument("email") {
//                        type = NavType.StringType
//                        defaultValue = ""
//                    }
//                )
//            ) { backStackEntry ->
//                val email = backStackEntry.arguments?.getString("email") ?: ""
//                EsqueciSenhaScreen(
//                    emailInicial = email,
//                    onBackToLogin = { navController.popBackStack() }
//                )
//            }
//            composable("cadastro") {
//                CadastroScreen(
//                    onBackToLogin = { navController.popBackStack() }
//                )
//            }
//            composable("home") {
//                HomeScreen(
//                    onLogout = {
//                        navController.popBackStack("login", inclusive = false)
//                    },
//                    onNavigate = { index ->
//                        when (index) {
//                            1 -> navController.navigate("filas")
//                            3 -> navController.navigate("dashboard")
//                        }
//                    }
//                )
//            }
//            composable("filas") {
//                FilasScreen(
//                    onBackToHome = { navController.popBackStack("home", inclusive = false) },
//                    onNavigate = { index ->
//                        when (index) {
//                            0 -> navController.popBackStack("home", inclusive = false)
//                            3 -> navController.navigate("dashboard")
//                        }
//                    }
//                )
//            }
//            composable("dashboard") {
//                CidadaoDashboardScreen(
//                    onBackToHome = { navController.popBackStack("home", inclusive = false) },
//                    onNavigate = { index ->
//                        when (index) {
//                            0 -> navController.popBackStack("home", inclusive = false)
//                            1 -> navController.navigate("filas")
//                        }
//                    }
//                )
//            }
        }
    }
}
