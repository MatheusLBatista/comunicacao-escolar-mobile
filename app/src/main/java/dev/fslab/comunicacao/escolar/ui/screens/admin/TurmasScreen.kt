package dev.fslab.comunicacao.escolar.ui.screens.admin

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.fslab.comunicacao.escolar.model.AlunoAdmin
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.ProfessorAdmin
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel
import dev.fslab.comunicacao.escolar.ui.components.AppToast
import dev.fslab.comunicacao.escolar.ui.components.rememberAppToastState
import kotlinx.coroutines.launch

private val turnoOptions = listOf("Manhã", "Tarde", "Integral")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurmasScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onTurmaClick: (Turma) -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val apiTurmas by adminViewModel.turmas.collectAsState()
    val professores by adminViewModel.professores.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterTurno by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) {
            adminViewModel.loadTurmas(schoolId)
            adminViewModel.loadUsuarios(schoolId)
        }
    }

    val activeProfIds by remember(professores) {
        derivedStateOf { professores.map { it.id }.toSet() }
    }

    val filtered = apiTurmas.map { cls ->
        val activeTeacher = if (activeProfIds.isEmpty()) cls.teachers.firstOrNull()
                            else cls.teachers.firstOrNull { t -> t.id in activeProfIds }
        cls.toTurma().copy(professor = activeTeacher?.fullName, professorId = activeTeacher?.id)
    }.filter { turma ->
        val matchSearch = searchQuery.isBlank() ||
                turma.nome.contains(searchQuery, ignoreCase = true) ||
                turma.professor?.contains(searchQuery, ignoreCase = true) == true
        val matchFilter = filterTurno == null || turma.shift == filterTurno
        matchSearch && matchFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminSubScreenScaffold(title = "Turmas", onBack = onBack) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
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
                                "Buscar turma ou professor...",
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
                                .background(
                                    if (filterTurno != null) colors.buttonContainer
                                    else colors.surface
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showFilterMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filtrar",
                                tint = if (filterTurno != null) colors.buttonText
                                       else colors.textTertiary
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
                                        color = if (filterTurno == null) colors.textPrimary
                                                else colors.textTertiary
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
                                            color = if (filterTurno == turno) colors.textPrimary
                                                    else colors.textTertiary
                                        )
                                    },
                                    onClick = { filterTurno = turno; showFilterMenu = false }
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { turma ->
                        TurmaListItem(turma = turma, onClick = { onTurmaClick(turma) })
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 16.dp),
            containerColor = colors.buttonContainer,
            contentColor = colors.buttonText,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Nova turma")
        }
    }

    if (showBottomSheet) {
        NovaTurmaBottomSheet(
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showBottomSheet = false
                }
            },
            onCreate = { nome, turno, ano ->
                adminViewModel.createTurma(
                    schoolId = schoolId,
                    name = nome,
                    shift = turno,
                    year = ano
                ) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBottomSheet = false
                    }
                }
            }
        )
    }
}

@Composable
private fun TurmaListItem(turma: Turma, onClick: () -> Unit) {
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
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.textSecondary)) {
                        if (turma.shift.isNotBlank()) append(turma.shift)
                        append(" · ${turma.ano}")
                    }
                    if (turma.professor != null) {
                        withStyle(SpanStyle(color = colors.textSecondary)) {
                            append(" · ${turma.professor}")
                        }
                    } else {
                        withStyle(SpanStyle(color = Color(0xFFE8803A))) {
                            append(" · Sem professor")
                        }
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaTurmaBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onCreate: (nome: String, turno: String, ano: Int) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var nome by remember { mutableStateOf("") }
    var turno by remember { mutableStateOf(turnoOptions.first()) }
    var showTurnoMenu by remember { mutableStateOf(false) }
    var turnoBoxWidth by remember { mutableIntStateOf(0) }
    var turnoOpenUpward by remember { mutableStateOf(false) }
    var ano by remember { mutableStateOf("${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}") }
    val focusManager = LocalFocusManager.current

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
        val density = LocalDensity.current
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
                text = "Nova Turma",
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
                        "Nome da turma (ex: Jardim III)",
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

            OutlinedTextField(
                value = ano,
                onValueChange = { ano = it },
                placeholder = {
                    Text(
                        "Ano letivo (ex: 2026)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        turnoBoxWidth = coords.size.width
                        val rootY = coords.positionInRoot().y.toInt()
                        val anchorBottom = rootY + coords.size.height
                        val maxPopupPx = with(density) { 200.dp.toPx() }.toInt()
                        turnoOpenUpward = anchorBottom + maxPopupPx > sheetView.height
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showTurnoMenu = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = turno,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }

                if (showTurnoMenu) {
                    val gapPx = with(density) { 2.dp.toPx() }.toInt()
                    val positionProvider = remember(turnoOpenUpward) {
                        object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset {
                                return if (turnoOpenUpward) {
                                    IntOffset(anchorBounds.left, anchorBounds.top - popupContentSize.height - gapPx)
                                } else {
                                    IntOffset(anchorBounds.left, anchorBounds.bottom + gapPx)
                                }
                            }
                        }
                    }
                    Popup(
                        popupPositionProvider = positionProvider,
                        onDismissRequest = { showTurnoMenu = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        val widthDp = with(density) { turnoBoxWidth.toDp() }
                        Column(
                            modifier = Modifier
                                .width(widthDp)
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                        ) {
                            turnoOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textPrimary
                                        )
                                    },
                                    onClick = { turno = option; showTurnoMenu = false }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val anoInt = ano.toIntOrNull()
                    if (nome.isNotBlank() && anoInt != null) {
                        focusManager.clearFocus()
                        onCreate(nome.trim(), turno, anoInt)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nome.isNotBlank() && ano.toIntOrNull() != null,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.inputBorder,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Text(
                    "Criar Turma",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurmaDetailScreen(
    turma: Turma,
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onProfessorClick: (ProfessorAdmin) -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val alunos by adminViewModel.alunos.collectAsState()
    val alunosLoading by adminViewModel.alunosLoading.collectAsState()
    val allTurmas by adminViewModel.turmas.collectAsState()
    val professores by adminViewModel.professores.collectAsState()
    val responsaveis by adminViewModel.responsaveis.collectAsState()
    val actionError by adminViewModel.actionError.collectAsState()

    val toastState = rememberAppToastState()

    var selectedAluno by remember { mutableStateOf<AlunoAdmin?>(null) }
    var showAdicionarProfessor by remember { mutableStateOf(false) }
    var showAdicionarAluno by remember { mutableStateOf(false) }

    val alunosSemTurma by adminViewModel.alunosSemTurma.collectAsState()
    val alunosSemTurmaLoading by adminViewModel.alunosSemTurmaLoading.collectAsState()

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

    val apiClass by remember(allTurmas, turma.id) {
        derivedStateOf { allTurmas.find { it.id == turma.id } }
    }

    val activeProfessorIds by remember(professores) {
        derivedStateOf { professores.map { it.id }.toSet() }
    }

    LaunchedEffect(turma.id) {
        adminViewModel.loadAlunos(schoolId, turma.id)
        adminViewModel.loadUsuarios(schoolId)
    }

    LaunchedEffect(actionError) {
        if (actionError != null) {
            toastState.showError(actionError!!)
            adminViewModel.clearActionError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AdminSubScreenScaffold(title = "Turma", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = turma.nome,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (turma.shift.isNotBlank()) {
                    Text(
                        text = "${turma.shift} · ${turma.ano}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            val firstTeacher = apiClass?.teachers?.firstOrNull { t ->
                activeProfessorIds.isEmpty() || t.id in activeProfessorIds
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROFESSOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    androidx.compose.material3.IconButton(
                        onClick = { showAdicionarProfessor = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Adicionar professor",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (firstTeacher != null) {
                item {
                    val professorUser = professores.find { it.id == firstTeacher.id }
                    val professorInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .let { m ->
                                if (professorUser != null)
                                    m.clickable(
                                        interactionSource = professorInteraction,
                                        indication = null
                                    ) { onProfessorClick(professorUser.toProfessorAdmin()) }
                                else m
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(nome = firstTeacher.fullName, size = 40)
                        Text(
                            text = firstTeacher.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (professorUser != null) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALUNOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    androidx.compose.material3.IconButton(
                        onClick = {
                            adminViewModel.loadAlunosSemTurma(schoolId)
                            showAdicionarAluno = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Adicionar aluno",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (alunosLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.textSecondary, strokeWidth = 2.dp)
                    }
                }
            } else if (alunos.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum aluno nesta turma",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            } else {
                items(alunos, key = { it.id }) { aluno ->
                    AlunoListRow(aluno = aluno, colors = colors, onClick = { selectedAluno = aluno })
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
            onResponsavelClick = { selectedAluno = null }
        )
    }

    if (showAdicionarProfessor) {
        val assignedIds = apiClass?.teachers?.map { it.id }?.toSet() ?: emptySet()
        val disponiveis = professores.filter { it.id !in assignedIds }
        AdicionarProfessorBottomSheet(
            professores = disponiveis,
            onDismiss = { showAdicionarProfessor = false },
            onSelect = { prof ->
                adminViewModel.assignTeacherToClass(schoolId, turma.id, prof.id) {
                    showAdicionarProfessor = false
                }
            }
        )
    }

    if (showAdicionarAluno) {
        AdicionarAlunoTurmaBottomSheet(
            alunos = alunosSemTurma,
            loading = alunosSemTurmaLoading,
            onDismiss = { showAdicionarAluno = false },
            onSelect = { aluno ->
                adminViewModel.assignStudentToClass(schoolId, aluno.id, turma.id) {
                    showAdicionarAluno = false
                }
            }
        )
    }

        AppToast(state = toastState)
    }
}

@Composable
private fun AlunoListRow(
    aluno: AlunoAdmin,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors,
    onClick: () -> Unit
) {
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
        UserAvatar(nome = aluno.nome, size = 40)
        Text(
            text = aluno.nome,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdicionarProfessorBottomSheet(
    professores: List<ApiSchoolUser>,
    onDismiss: () -> Unit,
    onSelect: (ApiSchoolUser) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var query by remember { mutableStateOf("") }
    val filtrados = remember(professores, query) {
        if (query.isBlank()) professores
        else professores.filter { it.fullName.contains(query, ignoreCase = true) }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Adicionar Professor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar professor...", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
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
            if (filtrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (professores.isEmpty()) "Nenhum professor disponível" else "Nenhum resultado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados, key = { it.id }) { prof ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSelect(prof) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            UserAvatar(nome = prof.fullName, size = 40)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prof.fullName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (prof.email != null) {
                                    Text(
                                        text = prof.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdicionarAlunoTurmaBottomSheet(
    alunos: List<AlunoAdmin>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSelect: (AlunoAdmin) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var query by remember { mutableStateOf("") }
    val filtrados = remember(alunos, query) {
        if (query.isBlank()) alunos
        else alunos.filter { it.nome.contains(query, ignoreCase = true) }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Adicionar Aluno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar aluno...", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
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
            when {
                loading -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = colors.textSecondary, strokeWidth = 2.dp)
                }
                filtrados.isEmpty() -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (alunos.isEmpty()) "Nenhum aluno sem turma" else "Nenhum resultado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados, key = { it.id }) { aluno ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSelect(aluno) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            UserAvatar(nome = aluno.nome, size = 40)
                            Text(
                                text = aluno.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
