package dev.fslab.comunicacao.escolar.ui.screens.professor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.components.BottomNavBar
import dev.fslab.comunicacao.escolar.ui.components.BottomNavItem
import dev.fslab.comunicacao.escolar.ui.screens.conversas.ConversaDetailScreen
import dev.fslab.comunicacao.escolar.ui.screens.conversas.ConversaListScreen
import dev.fslab.comunicacao.escolar.ui.screens.conversas.NovaConversaScreen
import dev.fslab.comunicacao.escolar.ui.screens.responsavel.AgendaResponsavelScreen
import dev.fslab.comunicacao.escolar.ui.screens.responsavel.PerfilScreen
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.theme.screens.MuralScreen as MuralScreenReal
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ConversaViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel

private const val ROUTE_INICIO = "professor_inicio"
private const val ROUTE_DIARIO = "professor_diario"
private const val ROUTE_CONVERSAS = "professor_conversas"
private const val ROUTE_MURAL = "professor_mural"
private const val ROUTE_AGENDA = "professor_agenda"
private const val ROUTE_PERFIL = "professor_perfil"

private val professorNavItems = listOf(
    BottomNavItem(Icons.Outlined.Home, ROUTE_INICIO, "Início"),
    BottomNavItem(Icons.AutoMirrored.Outlined.Chat, ROUTE_CONVERSAS, "Conversas"),
    BottomNavItem(Icons.Outlined.FavoriteBorder, ROUTE_MURAL, "Mural"),
    BottomNavItem(Icons.Outlined.DateRange, ROUTE_AGENDA, "Agenda"),
    BottomNavItem(Icons.Outlined.Person, ROUTE_PERFIL, "Perfil")
)

@Composable
fun ProfessorDashboardScreen(
    user: User,
    accessToken: String,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var currentRoute by rememberSaveable { mutableStateOf(ROUTE_INICIO) }

    BackHandler(enabled = currentRoute != ROUTE_INICIO) {
        currentRoute = ROUTE_INICIO
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            BottomNavBar(
                items = professorNavItems,
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
            label = "professor_tab"
        ) { route ->
            when (route) {
                ROUTE_INICIO    -> ProfessorInicioScreen(
                    user = user,
                    onNavigateToDiario = { currentRoute = ROUTE_DIARIO }
                )
                ROUTE_DIARIO    -> DiarioDeBordoScreen()
                ROUTE_CONVERSAS -> ProfessorConversasScreen(user = user, accessToken = accessToken)
                ROUTE_MURAL     -> MuralScreenReal(authViewModel = authViewModel)
                ROUTE_AGENDA    -> AgendaResponsavelScreen(
                    user = user,
                    accessToken = accessToken,
                    canCreate = true
                )
                ROUTE_PERFIL    -> PerfilScreen(
                    user = user,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun ProfessorConversasScreen(user: User, accessToken: String) {
    val conversaViewModel: ConversaViewModel = viewModel()
    var subScreen by remember { mutableStateOf<ProfessorConversasSubScreen?>(null) }

    BackHandler(enabled = subScreen != null) {
        subScreen = null
    }

    when (val screen = subScreen) {
        null -> ConversaListScreen(
            user = user,
            accessToken = accessToken,
            conversaViewModel = conversaViewModel,
            onOpenConversa = { id, titulo, avatarUrl ->
                subScreen = ProfessorConversasSubScreen.Detail(id, titulo, avatarUrl)
            },
            onNovaConversa = { subScreen = ProfessorConversasSubScreen.NovaConversa }
        )
        is ProfessorConversasSubScreen.Detail -> ConversaDetailScreen(
            conversaId = screen.conversaId,
            titulo = screen.titulo,
            avatarUrl = screen.avatarUrl,
            user = user,
            conversaViewModel = conversaViewModel,
            onBack = { subScreen = null }
        )
        is ProfessorConversasSubScreen.NovaConversa -> NovaConversaScreen(
            user = user,
            conversaViewModel = conversaViewModel,
            onBack = { subScreen = null },
            onConversaCreated = { id, titulo, avatarUrl ->
                subScreen = ProfessorConversasSubScreen.Detail(id, titulo, avatarUrl)
            }
        )
    }
}

private sealed class ProfessorConversasSubScreen {
    data class Detail(
        val conversaId: String,
        val titulo: String,
        val avatarUrl: String?
    ) : ProfessorConversasSubScreen()
    object NovaConversa : ProfessorConversasSubScreen()
}
