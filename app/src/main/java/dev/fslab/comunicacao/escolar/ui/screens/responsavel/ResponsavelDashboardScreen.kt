package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.navigation.Screen
import dev.fslab.comunicacao.escolar.ui.components.BottomNavBar
import dev.fslab.comunicacao.escolar.ui.components.BottomNavItem
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel
import dev.fslab.comunicacao.escolar.ui.theme.screens.MuralScreen as MuralScreenReal
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val responsavelNavItems = listOf(
    BottomNavItem(
        icon = Icons.AutoMirrored.Outlined.Assignment,
        route = Screen.Atividades.route,
        contentDescription = "Atividades"
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
 * Dashboard do Responsável — contém a BottomNavBar e gerencia qual tela está ativa.
 */
@Composable
fun ResponsavelDashboardScreen(
    user: User,
    accessToken: String,
    authViewModel: dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit,
    navController: NavController,
    onNavigateToLogin: () -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Atividades.route) }

    // Impede o back sair do app ao estar na aba principal
    BackHandler(enabled = currentRoute != Screen.Atividades.route) {
        currentRoute = Screen.Atividades.route
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            BottomNavBar(
                items = responsavelNavItems,
                currentRoute = currentRoute,
                onItemClick = { currentRoute = it.route }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background),
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
            label = "responsavel_tab"
        ) { route ->
            when (route) {
                Screen.Atividades.route -> AtividadesScreen(user = user, accessToken = accessToken)
                Screen.Conversas.route  -> ConversasScreen(user = user, accessToken = accessToken)
                Screen.Mural.route      -> MuralScreenReal(
                    muralViewModel = viewModel(),
                    authViewModel = authViewModel,
                    navController = navController,

                )
                Screen.Agenda.route     -> AgendaScreen(user = user, accessToken = accessToken)
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

// Telas do Responsável
@Composable
fun AtividadesScreen(user: User, accessToken: String) {
    val colors = LocalComunicacaoEscolarColors.current
    if (user.schoolId == null) {
        SemEscolaVinculada(
            icon = Icons.AutoMirrored.Outlined.Assignment,
            nomeTela = "Atividades"
        )
    } else {
        PlaceholderTela(nome = "Atividades", colors = colors)
    }
}

@Composable
fun ConversasScreen(user: User, accessToken: String) {
    val colors = LocalComunicacaoEscolarColors.current
    if (user.schoolId == null) {
        SemEscolaVinculada(
            icon = Icons.AutoMirrored.Outlined.Chat,
            nomeTela = "Conversas"
        )
    } else {
        PlaceholderTela(nome = "Conversas", colors = colors)
    }
}


@Composable
fun AgendaScreen(user: User, accessToken: String) {
    val colors = LocalComunicacaoEscolarColors.current
    if (user.schoolId == null) {
        SemEscolaVinculada(
            icon = Icons.Outlined.DateRange,
            nomeTela = "Agenda"
        )
    } else {
        PlaceholderTela(nome = "Agenda", colors = colors)
    }
}

// Composables auxiliares
/**
 * Estado vazio exibido quando o usuário não tem escola vinculada.
 */
@Composable
private fun SemEscolaVinculada(icon: ImageVector, nomeTela: String) {
    val colors = LocalComunicacaoEscolarColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = nomeTela,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Você ainda não está vinculado a uma escola. Entre em contato com o administrador para ser adicionado.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlaceholderTela(
    nome: String,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nome,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
    }
}
