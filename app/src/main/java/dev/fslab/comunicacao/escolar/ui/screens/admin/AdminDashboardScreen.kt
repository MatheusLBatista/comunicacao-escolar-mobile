package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.navigation.Screen
import dev.fslab.comunicacao.escolar.ui.components.BottomNavBar
import dev.fslab.comunicacao.escolar.ui.components.BottomNavItem
import dev.fslab.comunicacao.escolar.ui.screens.responsavel.PerfilScreen
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel

private val adminNavItems = listOf(
    BottomNavItem(
        icon = Icons.Outlined.SpaceDashboard,
        route = Screen.AdminHome.route,
        contentDescription = "Dashboard"
    ),
    BottomNavItem(
        icon = Icons.AutoMirrored.Outlined.Chat,
        route = Screen.Conversas.route,
        contentDescription = "Conversas"
    ),
    BottomNavItem(
        icon = Icons.Outlined.FavoriteBorder,
        route = Screen.Mural.route,
        contentDescription = "Mural"
    ),
    BottomNavItem(
        icon = Icons.Outlined.DateRange,
        route = Screen.Agenda.route,
        contentDescription = "Agenda"
    ),
    BottomNavItem(
        icon = Icons.Outlined.Person,
        route = Screen.Perfil.route,
        contentDescription = "Perfil"
    )
)

/**
 * Dashboard do Admin — contém a BottomNavBar com ícone diferente na primeira aba.
 */
@Composable
fun AdminDashboardScreen(
    user: User,
    accessToken: String,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var currentRoute by rememberSaveable { mutableStateOf(Screen.AdminHome.route) }

    BackHandler(enabled = currentRoute != Screen.AdminHome.route) {
        currentRoute = Screen.AdminHome.route
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            BottomNavBar(
                items = adminNavItems,
                currentRoute = currentRoute,
                onItemClick = { currentRoute = it.route }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            when (currentRoute) {
                Screen.AdminHome.route  -> AdminHomeScreen(user = user)
                Screen.Conversas.route  -> AdminConversasScreen(user = user)
                Screen.Mural.route      -> AdminMuralScreen(user = user)
                Screen.Agenda.route     -> AdminAgendaScreen(user = user)
                Screen.Perfil.route     -> PerfilScreen(
                    user = user,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

// ── Telas do Admin ────────────────────────────────────────────────────────────

@Composable
private fun AdminHomeScreen(user: User) {
    val colors = LocalComunicacaoEscolarColors.current
    AdminPlaceholderTela(
        icon = { Icon(imageVector = Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.35f), modifier = Modifier.size(72.dp)) },
        nome = "Dashboard"
    )
}

@Composable
private fun AdminConversasScreen(user: User) {
    val colors = LocalComunicacaoEscolarColors.current
    AdminPlaceholderTela(
        icon = { Icon(imageVector = Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.35f), modifier = Modifier.size(72.dp)) },
        nome = "Conversas"
    )
}

@Composable
private fun AdminMuralScreen(user: User) {
    val colors = LocalComunicacaoEscolarColors.current
    AdminPlaceholderTela(
        icon = { Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.35f), modifier = Modifier.size(72.dp)) },
        nome = "Mural"
    )
}

@Composable
private fun AdminAgendaScreen(user: User) {
    val colors = LocalComunicacaoEscolarColors.current
    AdminPlaceholderTela(
        icon = { Icon(imageVector = Icons.Outlined.DateRange, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.35f), modifier = Modifier.size(72.dp)) },
        nome = "Agenda"
    )
}

// ── Composable auxiliar ────────────────────────────────────────────────────────

@Composable
private fun AdminPlaceholderTela(
    icon: @Composable () -> Unit,
    nome: String
) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = nome,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Em breve",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}
