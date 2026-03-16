package dev.fslab.comunicacao.escolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.screens.AtividadeScreen
import dev.fslab.comunicacao.escolar.ui.theme.screens.DailyLogsScreen
import dev.fslab.comunicacao.escolar.ui.theme.screens.LoginScreen
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel

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
fun ComunicacaoEscolarApp(authViewModel: AuthViewModel = viewModel()) {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }

    val authState by authViewModel.authState.collectAsState()

    ComunicacaoEscolarTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        val isLoginSuccess = authState is AuthState.Success
        val isLoading = authState is AuthState.Loading
        val errorMessage = (authState as? AuthState.Error)?.message

        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") {
                if (isLoginSuccess) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.navigate("dailyLogs") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onEsqueciSenha = { email ->
                        authViewModel.clearError()
                        navController.navigate("esqueci_senha?email=$email")
                    },
                    onRegister = {
                        authViewModel.clearError()
                        navController.navigate("cadastro")
                    },
                    onLogin = { email, senha ->
                        authViewModel.loginUser(email, senha)
                    },
                    onLoginGoogle = { /* TODO: Implementar login com Google */ },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onErrorDismiss = { authViewModel.clearError() }
                )
            }

            composable("dailyLogs") {
                DailyLogsScreen()
            }

            composable("atividade") {
                AtividadeScreen()
            }
        }
    }
}
