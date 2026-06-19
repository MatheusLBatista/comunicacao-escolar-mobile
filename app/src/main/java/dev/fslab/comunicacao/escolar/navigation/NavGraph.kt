package dev.fslab.comunicacao.escolar.navigation

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.fslab.comunicacao.escolar.network.TokenManager
import dev.fslab.comunicacao.escolar.ui.screens.auth.CadastroScreen
import dev.fslab.comunicacao.escolar.ui.screens.auth.LoginScreen
import dev.fslab.comunicacao.escolar.ui.screens.auth.RecuperarSenhaScreen
import dev.fslab.comunicacao.escolar.ui.screens.auth.RedefinirSenhaScreen
import dev.fslab.comunicacao.escolar.ui.screens.common.HomeScreen
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel
//import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralViewModel
import dev.fslab.comunicacao.escolar.ui.theme.screens.MuralScreen
import androidx.compose.runtime.getValue

/**
 * Todas as rotas da aplicação definidas com type-safety.
 * Uso: navController.navigateSafely(Screen.Login.route)
 */
sealed class Screen(val route: String) {
    // Auth (público)
    object Login : Screen("login")
    object Cadastro : Screen("cadastro")
    object RecuperarSenha : Screen("recuperar_senha")
    object RedefinirSenha : Screen("redefinir_senha")
    object Home : Screen("home")

    // Responsável
    object Atividades : Screen("atividades")
    object Conversas : Screen("conversas")
    object Mural : Screen("mural")
    object Agenda : Screen("agenda")
    object Perfil : Screen("perfil")

    // Professor (fase 2)
    // object ProfessorHome : Screen("professor_home")
    // object ProfessorMural : Screen("professor_mural")
    // object ProfessorDiario : Screen("professor_diario")
    // object ProfessorConversas : Screen("professor_conversas")

    // Admin
    object AdminHome : Screen("admin_home")
}

private val TRANSITION_DURATION = 320

/**
 * NavGraph — Define todas as telas e transições de navegação da aplicação.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    DisposableEffect(navController) {
        TokenManager.onSessionExpired = {
            Handler(Looper.getMainLooper()).post {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        onDispose { TokenManager.onSessionExpired = null }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        }
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                onNavigateToCadastro = {
                    navController.navigateSafely(Screen.Cadastro.route)
                },
                onNavigateToRecuperarSenha = {
                    navController.navigateSafely(Screen.RecuperarSenha.route)
                },
                onLoginSuccess = {
                    navController.navigateSafely(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Cadastro.route) {
            CadastroScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStackSafely()
                },
                onCadastroSuccess = {
                    navController.popBackStackSafely()
                }
            )
        }

        composable(Screen.RecuperarSenha.route) {
            RecuperarSenhaScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStackSafely()
                },
                onNavigateToReset = {
                    navController.navigateSafely(Screen.RedefinirSenha.route) {
                        popUpTo(Screen.RecuperarSenha.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RedefinirSenha.route) {
            RedefinirSenhaScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigateSafely(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel
            )
        }
        composable(Screen.Mural.route){
            MuralScreen(
                muralViewModel = viewModel(),
                authViewModel = authViewModel,
                navController = navController
            )
        }

    }
}
