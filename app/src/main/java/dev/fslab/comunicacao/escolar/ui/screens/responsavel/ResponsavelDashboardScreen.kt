package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Chat
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.navigation.Screen
import dev.fslab.comunicacao.escolar.ui.components.BottomNavBar
import dev.fslab.comunicacao.escolar.ui.components.BottomNavItem
import dev.fslab.comunicacao.escolar.ui.screens.conversas.ConversaDetailScreen
import dev.fslab.comunicacao.escolar.ui.screens.conversas.ConversaListScreen
import dev.fslab.comunicacao.escolar.ui.screens.conversas.NovaConversaScreen
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ConversaViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel

private sealed class RespSubScreen {
    data class ConversaDetail(val conversaId: String, val titulo: String, val avatarUrl: String? = null) : RespSubScreen()
    object NovaConversa : RespSubScreen()
}

private sealed class RespNavKey {
    data class Tab(val route: String) : RespNavKey()
    data class SubScreen(val screen: RespSubScreen, val depth: Int) : RespNavKey()
}

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

@Composable
fun ResponsavelDashboardScreen(
    user: User,
    accessToken: String,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val conversaViewModel: ConversaViewModel = viewModel()
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Atividades.route) }
    val subScreenStack = remember { mutableStateListOf<RespSubScreen>() }

    BackHandler(enabled = subScreenStack.isNotEmpty()) {
        subScreenStack.removeLast()
    }
    BackHandler(enabled = subScreenStack.isEmpty() && currentRoute != Screen.Atividades.route) {
        currentRoute = Screen.Atividades.route
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            if (subScreenStack.lastOrNull() !is RespSubScreen.ConversaDetail) {
                BottomNavBar(
                    items = responsavelNavItems,
                    currentRoute = currentRoute,
                    onItemClick = {
                        subScreenStack.clear()
                        currentRoute = it.route
                    }
                )
            }
        }
    ) { innerPadding ->
        val navKey: RespNavKey = if (subScreenStack.isNotEmpty()) {
            RespNavKey.SubScreen(subScreenStack.last(), subScreenStack.size)
        } else {
            RespNavKey.Tab(currentRoute)
        }

        AnimatedContent(
            targetState = navKey,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .background(colors.background),
            transitionSpec = {
                val isPush = targetState is RespNavKey.SubScreen &&
                    (initialState is RespNavKey.Tab ||
                    (initialState is RespNavKey.SubScreen &&
                        (targetState as RespNavKey.SubScreen).depth >= (initialState as RespNavKey.SubScreen).depth))
                val isPop = initialState is RespNavKey.SubScreen && targetState is RespNavKey.Tab

                when {
                    isPop ->
                        slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                        fadeIn(tween(200)) togetherWith
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                        fadeOut(tween(150))
                    isPush ->
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                        fadeIn(tween(200)) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) +
                        fadeOut(tween(150))
                    else -> fadeIn(tween(220)) togetherWith fadeOut(tween(220))
                }
            },
            label = "resp_nav"
        ) { key ->
            when (key) {
                is RespNavKey.Tab -> when (key.route) {
                    Screen.Atividades.route -> AtividadesScreen(user = user, accessToken = accessToken)
                    Screen.Conversas.route  -> ConversaListScreen(
                        user = user,
                        accessToken = accessToken,
                        conversaViewModel = conversaViewModel,
                        onOpenConversa = { id, titulo, avatarUrl ->
                            subScreenStack.add(RespSubScreen.ConversaDetail(id, titulo, avatarUrl))
                        },
                        onNovaConversa = { subScreenStack.add(RespSubScreen.NovaConversa) }
                    )
                    Screen.Mural.route      -> MuralScreen(user = user, accessToken = accessToken)
                    Screen.Agenda.route     -> AgendaScreen(user = user, accessToken = accessToken)
                    Screen.Perfil.route     -> PerfilScreen(
                        user = user,
                        authViewModel = authViewModel,
                        themeViewModel = themeViewModel,
                        onLogout = onLogout
                    )
                    else -> {}
                }

                is RespNavKey.SubScreen -> when (val screen = key.screen) {
                    is RespSubScreen.ConversaDetail ->
                        ConversaDetailScreen(
                            conversaId = screen.conversaId,
                            titulo = screen.titulo,
                            avatarUrl = screen.avatarUrl,
                            user = user,
                            conversaViewModel = conversaViewModel,
                            onBack = { subScreenStack.removeLast() }
                        )

                    is RespSubScreen.NovaConversa ->
                        NovaConversaScreen(
                            user = user,
                            conversaViewModel = conversaViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onConversaCreated = { id, titulo, avatarUrl ->
                                subScreenStack.removeLast()
                                subScreenStack.add(RespSubScreen.ConversaDetail(id, titulo, avatarUrl))
                            }
                        )
                }
            }
        }
    }
}

@Composable
fun AtividadesScreen(user: User, accessToken: String) {
    if (user.schoolId == null) {
        SemEscolaVinculada(icon = Icons.AutoMirrored.Outlined.Assignment, nomeTela = "Atividades")
    } else {
        PlaceholderTela(nome = "Atividades")
    }
}

@Composable
fun MuralScreen(user: User, accessToken: String) {
    if (user.schoolId == null) {
        SemEscolaVinculada(icon = Icons.Outlined.FavoriteBorder, nomeTela = "Mural")
    } else {
        PlaceholderTela(nome = "Mural")
    }
}

@Composable
fun AgendaScreen(user: User, accessToken: String) {
    if (user.schoolId == null) {
        SemEscolaVinculada(icon = Icons.Outlined.DateRange, nomeTela = "Agenda")
    } else {
        PlaceholderTela(nome = "Agenda")
    }
}

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
private fun PlaceholderTela(nome: String) {
    val colors = LocalComunicacaoEscolarColors.current
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
