package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.AdminStats
import dev.fslab.comunicacao.escolar.model.ComunicadoTemplate
import dev.fslab.comunicacao.escolar.model.ProfessorAdmin
import dev.fslab.comunicacao.escolar.model.ResponsavelAdmin
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.navigation.Screen
import dev.fslab.comunicacao.escolar.ui.components.BottomNavBar
import dev.fslab.comunicacao.escolar.ui.components.BottomNavItem
import dev.fslab.comunicacao.escolar.ui.screens.responsavel.PerfilScreen
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel

// Sealed class for sub-screen navigation
sealed class AdminSubScreen {
    object Turmas : AdminSubScreen()
    data class TurmaDetail(val turma: dev.fslab.comunicacao.escolar.model.Turma) : AdminSubScreen()
    object Usuarios : AdminSubScreen()
    data class ProfessorDetail(val professor: ProfessorAdmin) : AdminSubScreen()
    data class ResponsavelDetail(val responsavel: ResponsavelAdmin) : AdminSubScreen()
    object VincularUsuario : AdminSubScreen()
    object Alunos : AdminSubScreen()
    object Templates : AdminSubScreen()
    object NovoTemplate : AdminSubScreen()
    data class TemplateDetail(val template: ComunicadoTemplate) : AdminSubScreen()
    object AuditLogs : AdminSubScreen()
}

private sealed class AdminNavKey {
    data class Tab(val route: String) : AdminNavKey()
    data class SubScreen(val screen: AdminSubScreen, val depth: Int) : AdminNavKey()
}

// Bottom nav items
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
        icon = Icons.Outlined.AdminPanelSettings,
        route = Screen.Perfil.route,
        contentDescription = "Perfil"
    )
)

// Admin Dashboard (shell)
@Composable
fun AdminDashboardScreen(
    user: User,
    accessToken: String,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val adminViewModel: AdminViewModel = viewModel()
    val schoolId = user.schoolId ?: ""
    var currentRoute by rememberSaveable { mutableStateOf(Screen.AdminHome.route) }
    val subScreenStack = remember { mutableStateListOf<AdminSubScreen>() }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) {
            adminViewModel.loadDashboardStats(schoolId)
            adminViewModel.loadTurmas(schoolId)
        }
    }

    BackHandler(enabled = subScreenStack.isNotEmpty()) {
        subScreenStack.removeLast()
    }
    BackHandler(enabled = subScreenStack.isEmpty() && currentRoute != Screen.AdminHome.route) {
        currentRoute = Screen.AdminHome.route
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            BottomNavBar(
                items = adminNavItems,
                currentRoute = currentRoute,
                onItemClick = {
                    subScreenStack.clear()
                    currentRoute = it.route
                }
            )
        }
    ) { innerPadding ->
        val navKey: AdminNavKey = if (subScreenStack.isNotEmpty()) {
            AdminNavKey.SubScreen(subScreenStack.last(), subScreenStack.size)
        } else {
            AdminNavKey.Tab(currentRoute)
        }

        AnimatedContent(
            targetState = navKey,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background),
            transitionSpec = {
                val isSubToSub = initialState is AdminNavKey.SubScreen && targetState is AdminNavKey.SubScreen
                val isPush = when {
                    isSubToSub -> (targetState as AdminNavKey.SubScreen).depth >= (initialState as AdminNavKey.SubScreen).depth
                    targetState is AdminNavKey.SubScreen -> true
                    else -> false
                }
                val isPop = initialState is AdminNavKey.SubScreen && targetState is AdminNavKey.Tab
                val isTabChange = initialState is AdminNavKey.Tab && targetState is AdminNavKey.Tab

                when {
                    isTabChange -> fadeIn(tween(220)) togetherWith fadeOut(tween(220))
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
            label = "admin_nav"
        ) { key ->
            when (key) {
                is AdminNavKey.Tab -> when (key.route) {
                    Screen.AdminHome.route -> AdminHomeScreen(
                        user = user,
                        adminViewModel = adminViewModel,
                        onNavigate = { subScreenStack.add(it) }
                    )
                    Screen.Conversas.route -> AdminPlaceholderTela(
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.35f),
                                modifier = Modifier.size(72.dp)
                            )
                        },
                        nome = "Conversas"
                    )
                    Screen.Mural.route -> AdminPlaceholderTela(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.35f),
                                modifier = Modifier.size(72.dp)
                            )
                        },
                        nome = "Mural"
                    )
                    Screen.Agenda.route -> AdminPlaceholderTela(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.35f),
                                modifier = Modifier.size(72.dp)
                            )
                        },
                        nome = "Agenda"
                    )
                    Screen.Perfil.route -> PerfilScreen(
                        user = user,
                        authViewModel = authViewModel,
                        themeViewModel = themeViewModel,
                        onLogout = onLogout
                    )
                    else -> {}
                }

                is AdminNavKey.SubScreen -> when (val screen = key.screen) {
                    is AdminSubScreen.Turmas ->
                        TurmasScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onTurmaClick = { subScreenStack.add(AdminSubScreen.TurmaDetail(it)) }
                        )

                    is AdminSubScreen.TurmaDetail ->
                        TurmaDetailScreen(
                            turma = screen.turma,
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onProfessorClick = { subScreenStack.add(AdminSubScreen.ProfessorDetail(it)) },
                            onResponsavelClick = { subScreenStack.add(AdminSubScreen.ResponsavelDetail(it)) }
                        )

                    is AdminSubScreen.Usuarios ->
                        UsuariosScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onProfessorClick = { subScreenStack.add(AdminSubScreen.ProfessorDetail(it)) },
                            onResponsavelClick = { subScreenStack.add(AdminSubScreen.ResponsavelDetail(it)) },
                            onVincularClick = { subScreenStack.add(AdminSubScreen.VincularUsuario) }
                        )

                    is AdminSubScreen.ProfessorDetail ->
                        ProfessorDetailScreen(
                            professor = screen.professor,
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onTurmaClick = { subScreenStack.add(AdminSubScreen.TurmaDetail(it)) }
                        )

                    is AdminSubScreen.ResponsavelDetail ->
                        ResponsavelDetailScreen(
                            responsavel = screen.responsavel,
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onTurmaClick = { subScreenStack.add(AdminSubScreen.TurmaDetail(it)) }
                        )

                    is AdminSubScreen.VincularUsuario ->
                        VincularUsuarioScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() }
                        )

                    is AdminSubScreen.Alunos ->
                        AlunosScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onTurmaClick = { subScreenStack.add(AdminSubScreen.TurmaDetail(it)) },
                            onResponsavelClick = { subScreenStack.add(AdminSubScreen.ResponsavelDetail(it)) }
                        )

                    is AdminSubScreen.Templates ->
                        TemplatesScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() },
                            onTemplateClick = { subScreenStack.add(AdminSubScreen.TemplateDetail(it)) },
                            onNovoTemplate = { subScreenStack.add(AdminSubScreen.NovoTemplate) }
                        )

                    is AdminSubScreen.NovoTemplate ->
                        NovoTemplateScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() }
                        )

                    is AdminSubScreen.TemplateDetail ->
                        TemplateDetailScreen(
                            template = screen.template,
                            onBack = { subScreenStack.removeLast() }
                        )

                    is AdminSubScreen.AuditLogs ->
                        AuditLogsScreen(
                            schoolId = schoolId,
                            adminViewModel = adminViewModel,
                            onBack = { subScreenStack.removeLast() }
                        )
                }
            }
        }
    }
}

// Visão Geral (Admin Home)
@Composable
private fun AdminHomeScreen(
    user: User,
    adminViewModel: AdminViewModel,
    onNavigate: (AdminSubScreen) -> Unit
) {
    val stats by adminViewModel.stats.collectAsState()
    val colors = LocalComunicacaoEscolarColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Visão Geral",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(value = stats?.totalProfessores?.toString() ?: "—", label = "Professores", modifier = Modifier.weight(1f))
                StatCard(value = stats?.totalResponsaveis?.toString() ?: "—", label = "Responsáveis", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(value = stats?.totalAlunos?.toString() ?: "—", label = "Alunos", modifier = Modifier.weight(1f))
                StatCard(value = stats?.totalTurmas?.toString() ?: "—", label = "Turmas", modifier = Modifier.weight(1f))
            }
        }

        item {
            Text(
                text = "ACESSO RÁPIDO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                QuickAccessItem(icon = Icons.Outlined.People, title = "Usuários", subtitle = "Professores e responsáveis", onClick = { onNavigate(AdminSubScreen.Usuarios) })
                QuickAccessItem(icon = Icons.Outlined.Face, title = "Alunos", subtitle = "Ver todos os alunos da escola", onClick = { onNavigate(AdminSubScreen.Alunos) })
                QuickAccessItem(icon = Icons.Outlined.School, title = "Turmas", subtitle = "Criar e editar turmas", onClick = { onNavigate(AdminSubScreen.Turmas) })
                QuickAccessItem(icon = Icons.Outlined.Description, title = "Templates de Comunicado", subtitle = "Gerenciar modelos de diário", onClick = { onNavigate(AdminSubScreen.Templates) })
                QuickAccessItem(icon = Icons.Outlined.History, title = "Logs de Auditoria", subtitle = "Rastrear ações do sistema", onClick = { onNavigate(AdminSubScreen.AuditLogs) }, showDivider = false)
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp), fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    val colors = LocalComunicacaoEscolarColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.lightGray))
        }
    }
}

// Shared scaffold for admin sub-screens
@Composable
fun AdminSubScreenScaffold(
    title: String,
    onBack: () -> Unit,
    action: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = colors.textPrimary)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
                action()
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.lightGray))
        content()
    }
}

// Placeholder for other tabs
@Composable
private fun AdminPlaceholderTela(icon: @Composable () -> Unit, nome: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier.fillMaxSize().background(colors.background).padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = nome, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Em breve", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, textAlign = TextAlign.Center)
    }
}
