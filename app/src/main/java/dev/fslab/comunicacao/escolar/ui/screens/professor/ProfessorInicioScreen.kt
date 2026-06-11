package dev.fslab.comunicacao.escolar.ui.screens.professor

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.AlunoAdmin
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.screens.admin.AlunoDetalheBottomSheet
import dev.fslab.comunicacao.escolar.ui.screens.admin.UserAvatar
import dev.fslab.comunicacao.escolar.ui.screens.responsavel.AutorizacaoSaidaScreen
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ProfessorInicioUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.ProfessorInicioViewModel

@Composable
fun ProfessorInicioScreen(
    user: User,
    onNavigateToDiario: () -> Unit = {},
    onAbrirConversa: (userId: String, nome: String, avatarUrl: String?) -> Unit = { _, _, _ -> },
    viewModel: ProfessorInicioViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    val turmasCount by viewModel.turmasCount.collectAsState()
    val alunosCount by viewModel.alunosCount.collectAsState()
    val turmas by viewModel.turmas.collectAsState()
    val atividadesCount = (uiState as? ProfessorInicioUiState.Content)?.atividades?.size

    val schoolId = user.schoolId
    LaunchedEffect(user.id) {
        if (schoolId != null) viewModel.loadStats(schoolId, user.id)
    }

    var subScreen by remember { mutableStateOf<ProfessorInicioSubScreen?>(null) }

    BackHandler(enabled = subScreen != null) { subScreen = null }

    AnimatedContent(
        targetState = subScreen,
        transitionSpec = {
            val isPush = targetState != null
            val isPop = initialState != null && targetState == null
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
        label = "professor_inicio_nav"
    ) { screen ->
        when (screen) {
            ProfessorInicioSubScreen.Atividades ->
                AtividadeRecenteScreen(uiState = uiState, onBack = { subScreen = null })
            ProfessorInicioSubScreen.MinhasTurmas ->
                MinhasTurmasScreen(
                    turmas = turmas,
                    schoolId = schoolId ?: "",
                    viewModel = viewModel,
                    onBack = { subScreen = null },
                    onAbrirConversa = onAbrirConversa
                )
            ProfessorInicioSubScreen.Autorizacoes ->
                AutorizacaoSaidaScreen(onBack = { subScreen = null }, canCreate = false, canRegisterSaida = true)
            null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                AppHeader("Início")
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(value = alunosCount?.toString() ?: "—", label = "Alunos", modifier = Modifier.weight(1f))
                            StatCard(value = turmasCount?.toString() ?: "—", label = "Turmas", modifier = Modifier.weight(1f))
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
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            QuickAccessItem(
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                title = "Diário de Bordo",
                                subtitle = "Registrar atividades da turma",
                                onClick = onNavigateToDiario
                            )
                            QuickAccessItem(
                                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                                title = "Autorizações de Saída",
                                subtitle = "Gerenciar autorizações",
                                onClick = { subScreen = ProfessorInicioSubScreen.Autorizacoes }
                            )
                            QuickAccessItem(
                                icon = Icons.Outlined.Group,
                                title = "Minhas Turmas",
                                subtitle = "Visualizar e gerenciar alunos",
                                onClick = { subScreen = ProfessorInicioSubScreen.MinhasTurmas }
                            )
                            QuickAccessItem(
                                icon = Icons.Outlined.History,
                                title = "Atividade Recente",
                                subtitle = when {
                                    atividadesCount != null && atividadesCount > 0 -> "$atividadesCount registros recentes"
                                    else -> "Ver últimas atividades"
                                },
                                onClick = { subScreen = ProfessorInicioSubScreen.Atividades },
                                showDivider = false
                            )
                        }
                    }
                }
            }
        }
    }
}


private sealed class ProfessorInicioSubScreen {
    object Atividades : ProfessorInicioSubScreen()
    object MinhasTurmas : ProfessorInicioSubScreen()
    object Autorizacoes : ProfessorInicioSubScreen()
}

private val turnoOptions = listOf("Manhã", "Tarde", "Integral")

@Composable
private fun MinhasTurmasScreen(
    turmas: List<Turma>,
    schoolId: String,
    viewModel: ProfessorInicioViewModel,
    onBack: () -> Unit,
    onAbrirConversa: (userId: String, nome: String, avatarUrl: String?) -> Unit = { _, _, _ -> }
) {
    var selectedTurma by remember { mutableStateOf<Turma?>(null) }

    BackHandler(enabled = selectedTurma != null) { selectedTurma = null }

    AnimatedContent(
        targetState = selectedTurma,
        transitionSpec = {
            val isPush = targetState != null
            val isPop = initialState != null && targetState == null
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
        label = "turma_detail_nav"
    ) { turma ->
        if (turma != null) {
            ProfessorTurmaDetailScreen(
                turma = turma,
                schoolId = schoolId,
                viewModel = viewModel,
                onBack = { selectedTurma = null },
                onAbrirConversa = onAbrirConversa
            )
        } else {
            TurmaListContent(
                turmas = turmas,
                onBack = onBack,
                onTurmaClick = { selectedTurma = it }
            )
        }
    }
}

@Composable
private fun TurmaListContent(
    turmas: List<Turma>,
    onBack: () -> Unit,
    onTurmaClick: (Turma) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var searchQuery by remember { mutableStateOf("") }
    var filterTurno by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filtered = remember(turmas, searchQuery, filterTurno) {
        turmas.filter { turma ->
            val matchSearch = searchQuery.isBlank() || turma.nome.contains(searchQuery, ignoreCase = true)
            val matchTurno = filterTurno == null || turma.shift == filterTurno
            matchSearch && matchTurno
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader("Minhas Turmas", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Buscar turma...",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.focusedIndicator,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedTextColor = colors.textInput,
                    unfocusedTextColor = colors.textInput,
                    cursorColor = colors.focusedIndicator,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                )
            )
            Box {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (filterTurno != null) colors.buttonContainer else colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showFilterMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filtrar",
                        tint = if (filterTurno != null) colors.buttonText else colors.textTertiary
                    )
                }
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    offset = DpOffset(0.dp, 4.dp),
                    modifier = Modifier.background(colors.surface)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Todos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (filterTurno == null) colors.textPrimary else colors.textTertiary
                            )
                        },
                        onClick = { filterTurno = null; showFilterMenu = false }
                    )
                    turnoOptions.forEach { turno ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    turno,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (filterTurno == turno) colors.textPrimary else colors.textTertiary
                                )
                            },
                            onClick = { filterTurno = turno; showFilterMenu = false }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.35f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma turma encontrada",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (turmas.isEmpty()) "Você não está vinculado a nenhuma turma."
                               else "Nenhuma turma corresponde à busca.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { turma ->
                    TurmaCard(turma = turma, onClick = { onTurmaClick(turma) })
                }
            }
        }
    }
}

@Composable
private fun ProfessorTurmaDetailScreen(
    turma: Turma,
    schoolId: String,
    viewModel: ProfessorInicioViewModel,
    onBack: () -> Unit,
    onAbrirConversa: (userId: String, nome: String, avatarUrl: String?) -> Unit = { _, _, _ -> }
) {
    val colors = LocalComunicacaoEscolarColors.current
    val students by viewModel.selectedClassStudents.collectAsState()
    val loading by viewModel.loadingClassStudents.collectAsState()
    val responsaveis by viewModel.responsaveis.collectAsState()

    var selectedAluno by remember { mutableStateOf<AlunoAdmin?>(null) }

    val parentUserMap by remember(responsaveis) {
        derivedStateOf {
            buildMap<String, ApiSchoolUser> {
                responsaveis.forEach { resp ->
                    resp.memberships
                        .filter { it.role == "parent" }
                        .flatMap { it.associatedStudents }
                        .forEach { s -> put(s.id, resp) }
                }
            }
        }
    }

    val apiClass = remember(turma) {
        ApiClass(id = turma.id, name = turma.nome, shift = turma.shift, year = turma.ano)
    }

    LaunchedEffect(turma.id) {
        viewModel.loadStudentsForClass(schoolId, turma.id)
        viewModel.loadResponsaveis(schoolId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader(turma.nome, onBack = onBack)

        when {
            loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = colors.primary,
                    strokeWidth = 2.5.dp
                )
            }
            students.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.35f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum aluno nesta turma",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    StudentRow(student, onClick = { selectedAluno = student })
                }
            }
        }
    }

    selectedAluno?.let { aluno ->
        AlunoDetalheBottomSheet(
            aluno = aluno,
            turma = apiClass,
            parentUser = parentUserMap[aluno.id],
            onDismiss = { selectedAluno = null },
            onTurmaClick = { selectedAluno = null },
            onResponsavelClick = { resp -> selectedAluno = null; onAbrirConversa(resp.id, resp.nome, resp.avatar) },
            allTurmas = emptyList(),
            onMoverParaTurma = null
        )
    }
}

@Composable
private fun StudentRow(student: AlunoAdmin, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserAvatar(nome = student.nome, size = 44)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (!student.email.isNullOrBlank()) {
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 76.dp)
                .height(0.5.dp)
                .background(colors.lightGray)
        )
    }
}

@Composable
private fun TurmaCard(turma: Turma, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = turma.nome,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            val detail = buildString {
                if (turma.shift.isNotBlank()) append(turma.shift)
                append(" · ${turma.ano}")
            }
            Text(text = detail, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
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
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(top = 14.dp, bottom = if (showDivider) 14.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.lightGray))
        }
    }
}
