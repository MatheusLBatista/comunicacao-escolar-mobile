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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.AlunoAdmin
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.ResponsavelAdmin
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlunosScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onTurmaClick: (Turma) -> Unit = {},
    onResponsavelClick: (ResponsavelAdmin) -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val alunos by adminViewModel.alunos.collectAsState()
    val alunosLoading by adminViewModel.alunosLoading.collectAsState()
    val allTurmas by adminViewModel.turmas.collectAsState()
    val apiResponsaveis by adminViewModel.responsaveis.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf<String?>(null) }

    var selectedAluno by remember { mutableStateOf<AlunoAdmin?>(null) }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) {
            adminViewModel.loadAlunos(schoolId)
            adminViewModel.loadUsuarios(schoolId)
        }
    }

    val parentUserMap by remember(apiResponsaveis) {
        derivedStateOf {
            buildMap<String, ApiSchoolUser> {
                apiResponsaveis.forEach { resp ->
                    resp.memberships
                        .filter { it.role == "parent" }
                        .flatMap { it.associatedStudents }
                        .forEach { s -> put(s.id, resp) }
                }
            }
        }
    }

    val turmaMap by remember(allTurmas) {
        derivedStateOf { allTurmas.associate { it.id to it.name } }
    }

    val filtrados by remember(alunos, searchQuery, selectedClassId) {
        derivedStateOf {
            alunos.filter { a ->
                (searchQuery.isBlank() || a.nome.contains(searchQuery, ignoreCase = true)) &&
                (selectedClassId == null || a.classId == selectedClassId)
            }
        }
    }

    AdminSubScreenScaffold(title = "Alunos", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Buscar aluno...", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(52.dp),
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

            if (allTurmas.isNotEmpty()) {
                TurmaDropdownField(
                    turmas = allTurmas.map { it.toTurma() },
                    selectedTurma = allTurmas.find { it.id == selectedClassId }?.toTurma(),
                    onTurmaSelected = { selectedClassId = it.id },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                    placeholder = "Todas as turmas",
                    clearOptionLabel = "Todas as turmas",
                    onClearSelected = { selectedClassId = null }
                )
            }

            when {
                alunosLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = colors.textSecondary, strokeWidth = 2.dp)
                    }
                }
                filtrados.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isBlank() && selectedClassId == null) "Nenhum aluno cadastrado"
                                   else "Nenhum aluno encontrado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtrados, key = { it.id }) { aluno ->
                            AlunoRow(
                                aluno = aluno,
                                turmaNome = aluno.classId?.let { turmaMap[it] },
                                parentNome = parentUserMap[aluno.id]?.fullName,
                                colors = colors,
                                onClick = { selectedAluno = aluno }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedAluno?.let { aluno ->
        val parentId = parentUserMap[aluno.id]?.id
        AlunoDetalheBottomSheet(
            aluno = aluno,
            turma = aluno.classId?.let { id -> allTurmas.find { it.id == id } },
            parentUser = parentUserMap[aluno.id],
            onDismiss = { selectedAluno = null },
            onTurmaClick = { turma -> selectedAluno = null; onTurmaClick(turma) },
            onResponsavelClick = { resp -> selectedAluno = null; onResponsavelClick(resp) },
            allTurmas = allTurmas,
            onMoverParaTurma = { classId ->
                if (parentId != null) {
                    adminViewModel.moveStudentToClass(schoolId, parentId, aluno.id, classId) {
                        selectedAluno = null
                        adminViewModel.loadAlunos(schoolId)
                    }
                } else {
                    adminViewModel.assignStudentToClass(schoolId, aluno.id, classId) {
                        selectedAluno = null
                        adminViewModel.loadAlunos(schoolId)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlunoDetalheBottomSheet(
    aluno: AlunoAdmin,
    turma: ApiClass?,
    parentUser: ApiSchoolUser?,
    onDismiss: () -> Unit,
    onTurmaClick: (Turma) -> Unit,
    onResponsavelClick: (ResponsavelAdmin) -> Unit,
    allTurmas: List<ApiClass> = emptyList(),
    onMoverParaTurma: ((classId: String) -> Unit)? = null
) {
    val colors = LocalComunicacaoEscolarColors.current
    val sheetState = rememberModalBottomSheetState()
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
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
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
            UserAvatar(nome = aluno.nome, size = 64)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = aluno.nome,
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
            if (onMoverParaTurma != null && allTurmas.isNotEmpty()) {
                val turmasList = remember(allTurmas) { allTurmas.map { it.toTurma() } }
                TurmaDropdownField(
                    turmas = turmasList,
                    selectedTurma = turma?.toTurma(),
                    onTurmaSelected = { selected ->
                        if (selected.id != turma?.id) onMoverParaTurma(selected.id)
                    },
                    placeholder = "Selecionar turma"
                )
            } else if (turma != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTurmaClick(turma.toTurma()) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Text(
                    text = "Nenhuma turma vinculada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RESPONSÁVEL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (parentUser != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onResponsavelClick(parentUser.toResponsavelAdmin()) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = parentUser.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Text(
                    text = "Nenhum responsável vinculado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AlunoRow(
    aluno: AlunoAdmin,
    turmaNome: String?,
    parentNome: String?,
    colors: ComunicacaoEscolarColors,
    onClick: () -> Unit
) {
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
        UserAvatar(nome = aluno.nome, size = 40)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = aluno.nome,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                if (turmaNome != null) append(turmaNome)
                if (parentNome != null) {
                    if (isNotEmpty()) append(" · ")
                    append("Resp: $parentNome")
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
