package dev.fslab.comunicacao.escolar.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import dev.fslab.comunicacao.escolar.model.UserRole
import dev.fslab.comunicacao.escolar.navigation.Screen
import dev.fslab.comunicacao.escolar.navigation.navigateSafely
import dev.fslab.comunicacao.escolar.ui.screens.admin.AdminDashboardScreen
import dev.fslab.comunicacao.escolar.ui.screens.responsavel.ResponsavelDashboardScreen
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel


/**
 * HomeScreen — Roteador central pós-login.
 *
 * Lê o papel (role) do usuário autenticado e exibe o dashboard correspondente.
 * Redireciona para Login se a sessão for encerrada.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val accessToken by authViewModel.accessToken.collectAsState()

    LaunchedEffect(authState, currentUser) {
        if (authState is AuthState.Idle && currentUser == null) {
            navController.navigateSafely(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    val user = currentUser ?: return
    val token = accessToken ?: ""

    when (user.role) {
        UserRole.RESPONSAVEL -> ResponsavelDashboardScreen(
            user = user,
            accessToken = token,
            authViewModel = authViewModel,
            themeViewModel = themeViewModel,
            onLogout = { authViewModel.logout() },
            navController = navController,
            onNavigateToLogin = {
                navController.navigateSafely(Screen.Login.route) {
                    popUpTo(Screen.Home.route) {inclusive = true}
                }
            }
        )
        UserRole.PROFESSOR -> {
            ResponsavelDashboardScreen(
                user = user,
                accessToken = token,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                onLogout = { authViewModel.logout() },
                navController = navController,
                onNavigateToLogin = {
                    navController.navigateSafely(Screen.Login.route) {
                        popUpTo(Screen.Home.route) {inclusive = true}
                    }
                }

            )
        }
        UserRole.ADMIN -> AdminDashboardScreen(
            user = user,
            accessToken = token,
            authViewModel = authViewModel,
            themeViewModel = themeViewModel,
            onLogout = { authViewModel.logout() },

        )
    }
}
