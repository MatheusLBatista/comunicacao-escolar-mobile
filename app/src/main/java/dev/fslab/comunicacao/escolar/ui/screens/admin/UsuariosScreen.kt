package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.FilhoAdmin
import dev.fslab.comunicacao.escolar.model.ProfessorAdmin
import dev.fslab.comunicacao.escolar.model.ResponsavelAdmin
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

// Usuários Screen
@Composable
fun UsuariosScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onProfessorClick: (ProfessorAdmin) -> Unit,
    onResponsavelClick: (ResponsavelAdmin) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val apiProfessores by adminViewModel.professores.collectAsState()
    val apiResponsaveis by adminViewModel.responsaveis.collectAsState()
    val allTurmas by adminViewModel.turmas.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var tabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) adminViewModel.loadUsuarios(schoolId)
    }

    AdminSubScreenScaffold(title = "Usuários", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Buscar usuário...",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Professores", "Responsáveis").forEachIndexed { idx, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (tabIndex == idx) colors.buttonContainer else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { tabIndex = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (tabIndex == idx) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (tabIndex == idx) colors.buttonText else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tabIndex == 0) {
                val filtrados = apiProfessores
                    .map { it.toProfessorAdmin() }
                    .filter {
                        searchQuery.isBlank() || it.nome.contains(searchQuery, ignoreCase = true)
                    }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados, key = { it.id }) { prof ->
                        UsuarioListItem(
                            nome = prof.nome,
                            subtitle = prof.email,
                            onClick = { onProfessorClick(prof) }
                        )
                    }
                }
            } else {
                val filtrados = apiResponsaveis
                    .map { it.toResponsavelAdmin(schoolId) }
                    .filter {
                        searchQuery.isBlank() || it.nome.contains(searchQuery, ignoreCase = true)
                    }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados, key = { it.id }) { resp ->
                        UsuarioListItem(
                            nome = resp.nome,
                            subtitle = resp.email,
                            onClick = { onResponsavelClick(resp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun UsuarioListItem(nome: String, subtitle: String, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserAvatar(nome = nome, size = 40)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nome,
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
}

@Composable
internal fun UserAvatar(nome: String, modifier: Modifier = Modifier, size: Int = 40) {
    val colors = LocalComunicacaoEscolarColors.current
    val initials = nome.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
    val bgColor = avatarColor(nome)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = (size * 0.3).sp),
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

private fun avatarColor(nome: String): Color {
    val colors = listOf(
        Color(0xFFA78BFA), // purple
        Color(0xFF60A5FA), // blue
        Color(0xFF34D399), // green
        Color(0xFFFBBF24), // yellow
        Color(0xFFF87171), // red
        Color(0xFF38BDF8), // sky
        Color(0xFFFB923C)  // orange
    )
    return colors[Math.abs(nome.hashCode()) % colors.size]
}

// Professor Detail Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorDetailScreen(
    professor: ProfessorAdmin,
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onTurmaClick: (Turma) -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val allTurmas by adminViewModel.turmas.collectAsState()
    val turmas = remember(allTurmas) {
        mutableStateListOf<Turma>().also { list ->
            list.clear()
            list.addAll(
                allTurmas
                    .filter { cls -> cls.teachers.any { it.id == professor.id } }
                    .map { it.toTurma() }
            )
        }
    }
    var showAddSheet by remember { mutableStateOf(false) }
    var turmaParaRemover by remember { mutableStateOf<Turma?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    turmaParaRemover?.let { turma ->
        AlertDialog(
            onDismissRequest = { turmaParaRemover = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Remover turma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalComunicacaoEscolarColors.current.textPrimary
                )
            },
            text = {
                Text(
                    text = "Remover \"${turma.nome}\" das turmas de ${professor.nome}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalComunicacaoEscolarColors.current.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.removeTeacherFromClass(schoolId, turma.id, professor.id) {}
                        turmaParaRemover = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalComunicacaoEscolarColors.current.error,
                        contentColor = LocalComunicacaoEscolarColors.current.buttonText
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remover", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { turmaParaRemover = null }) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalComunicacaoEscolarColors.current.textSecondary
                    )
                }
            },
            containerColor = LocalComunicacaoEscolarColors.current.background
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminSubScreenScaffold(title = "Professor", onBack = onBack) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Avatar
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    UserAvatar(nome = professor.nome, size = 80)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = professor.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = professor.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "TURMAS VINCULADAS - ${turmas.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                turmas.forEach { turma ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTurmaClick(turma) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = turma.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = turma.shift.ifBlank { "—" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        IconButton(
                            onClick = { turmaParaRemover = turma },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remover",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(colors.lightGray)
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 16.dp),
            containerColor = colors.buttonContainer,
            contentColor = colors.buttonText,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Adicionar turma")
        }
    }

    if (showAddSheet) {
        AdicionarTurmaBottomSheet(
            sheetState = sheetState,
            turmasDisponiveis = allTurmas.map { it.toTurma() }.filter { t -> turmas.none { it.id == t.id } },
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
            },
            onAdd = { turma ->
                adminViewModel.assignTeacherToClass(schoolId, turma.id, professor.id) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdicionarTurmaBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    turmasDisponiveis: List<Turma>,
    onDismiss: () -> Unit,
    onAdd: (Turma) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var selected by remember { mutableStateOf<Turma?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val isEmpty = turmasDisponiveis.isEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.inputBorder)
            )
        }
    ) {
        val sheetView = LocalView.current
        val isDark = colors.isDark
        val bgColor = colors.background
        SideEffect {
            val window = (sheetView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            if (window != null) {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
                window.navigationBarColor = bgColor.toArgb()
                androidx.core.view.WindowCompat.getInsetsController(window, sheetView)
                    .isAppearanceLightNavigationBars = !isDark
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Adicionar Turma",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            var dropdownWidth by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { dropdownWidth = it.width }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .then(
                            if (!isEmpty) Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showMenu = true } else Modifier
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isEmpty -> "Todas as turmas já vinculadas"
                            selected != null -> selected!!.nome
                            else -> "Selecione uma turma..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected != null && !isEmpty) colors.textPrimary else colors.textSecondary
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
                if (!isEmpty) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = DpOffset(x = 0.dp, y = -6.dp),
                        modifier = Modifier
                            .width(with(density) { dropdownWidth.toDp() })
                            .background(colors.surface)
                    ) {
                        turmasDisponiveis.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        t.nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary
                                    )
                                },
                                onClick = {
                                    selected = t
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { selected?.let { onAdd(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = selected != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.inputBorder,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Text("Adicionar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Responsável Detail Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsavelDetailScreen(
    responsavel: ResponsavelAdmin,
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val allTurmas by adminViewModel.turmas.collectAsState()
    val apiResponsaveis by adminViewModel.responsaveis.collectAsState()
    val actionError by adminViewModel.actionError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filhos = remember(apiResponsaveis) {
        val atualizado = apiResponsaveis.find { it.id == responsavel.id }
        val fonte = atualizado?.toResponsavelAdmin(schoolId)?.filhos ?: responsavel.filhos
        mutableStateListOf<FilhoAdmin>().also { list ->
            list.clear()
            list.addAll(fonte)
        }
    }
    var showAddSheet by remember { mutableStateOf(false) }
    var filhoParaRemover by remember { mutableStateOf<FilhoAdmin?>(null) }
    var filhoSelecionado by remember { mutableStateOf<FilhoAdmin?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filhoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            adminViewModel.clearActionError()
        }
    }

    filhoParaRemover?.let { filho ->
        AlertDialog(
            onDismissRequest = { filhoParaRemover = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Remover filho",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalComunicacaoEscolarColors.current.textPrimary
                )
            },
            text = {
                Text(
                    text = "Remover \"${filho.nome}\" dos filhos de ${responsavel.nome}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalComunicacaoEscolarColors.current.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.removeStudentFromParent(schoolId, responsavel.id, filho.id) {}
                        filhoParaRemover = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalComunicacaoEscolarColors.current.error,
                        contentColor = LocalComunicacaoEscolarColors.current.buttonText
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remover", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { filhoParaRemover = null }) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalComunicacaoEscolarColors.current.textSecondary
                    )
                }
            },
            containerColor = LocalComunicacaoEscolarColors.current.background
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminSubScreenScaffold(title = "Responsável", onBack = onBack) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    UserAvatar(nome = responsavel.nome, size = 80)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = responsavel.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = responsavel.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FILHOS VINCULADOS - ${filhos.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                filhos.forEachIndexed { idx, filho ->
                    val turmaNome = filho.classId?.let { cid ->
                        allTurmas.find { it.id == cid }?.name
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { filhoSelecionado = filho }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = filho.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = turmaNome ?: "Sem turma",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        IconButton(onClick = { filhoParaRemover = filho }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remover",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(colors.lightGray)
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 16.dp),
            containerColor = colors.buttonContainer,
            contentColor = colors.buttonText,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Adicionar filho")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = colors.surface,
                contentColor = colors.textPrimary
            )
        }
    }

    if (showAddSheet) {
        AdicionarFilhoBottomSheet(
            sheetState = sheetState,
            turmas = allTurmas.map { it.toTurma() },
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
            },
            onAdd = { nome, turmaId ->
                adminViewModel.addStudentToParent(schoolId, responsavel.id, nome, turmaId) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
                }
            }
        )
    }

    filhoSelecionado?.let { filho ->
        val turma = filho.classId?.let { cid -> allTurmas.find { it.id == cid } }
        FilhoDetailBottomSheet(
            sheetState = filhoSheetState,
            filho = filho,
            turma = turma,
            onDismiss = {
                scope.launch { filhoSheetState.hide() }.invokeOnCompletion { filhoSelecionado = null }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdicionarFilhoBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    turmas: List<Turma>,
    onDismiss: () -> Unit,
    onAdd: (nome: String, classId: String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var nome by remember { mutableStateOf("") }
    var selectedTurma by remember { mutableStateOf<Turma?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.inputBorder)
            )
        }
    ) {
        val sheetView = LocalView.current
        val isDark = colors.isDark
        val bgColor = colors.background
        SideEffect {
            val window = (sheetView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            if (window != null) {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
                window.navigationBarColor = bgColor.toArgb()
                androidx.core.view.WindowCompat.getInsetsController(window, sheetView)
                    .isAppearanceLightNavigationBars = !isDark
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Adicionar Filho",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                placeholder = {
                    Text(
                        "Nome completo do aluno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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

            var dropdownWidth by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { dropdownWidth = it.width }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showMenu = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTurma?.nome ?: "Selecione a turma do aluno...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedTurma != null) colors.textPrimary else colors.textSecondary
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(x = 0.dp, y = -6.dp),
                    modifier = Modifier
                        .width(with(density) { dropdownWidth.toDp() })
                        .background(colors.surface)
                ) {
                    turmas.forEach { t ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    t.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = {
                                selectedTurma = t
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { if (nome.isNotBlank() && selectedTurma != null) onAdd(nome.trim(), selectedTurma!!.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = nome.isNotBlank() && selectedTurma != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.inputBorder,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Text("Adicionar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilhoDetailBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    filho: FilhoAdmin,
    turma: ApiClass?,
    onDismiss: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.inputBorder)
            )
        }
    ) {
        val sheetView = LocalView.current
        val isDark = colors.isDark
        val bgColor = colors.background
        SideEffect {
            val window = (sheetView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            if (window != null) {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
                window.navigationBarColor = bgColor.toArgb()
                androidx.core.view.WindowCompat.getInsetsController(window, sheetView)
                    .isAppearanceLightNavigationBars = !isDark
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(nome = filho.nome, size = 64)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = filho.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "TURMA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (turma != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = turma.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = buildString {
                                if (turma.shift.isNotBlank()) append(turma.shift)
                                append(" · ${turma.year}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                Text(
                    text = "Nenhuma turma vinculada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
