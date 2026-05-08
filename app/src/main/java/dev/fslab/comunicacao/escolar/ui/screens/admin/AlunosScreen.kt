package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.AlunoAdmin
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel

@Composable
fun AlunosScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val alunos by adminViewModel.alunos.collectAsState()
    val alunosLoading by adminViewModel.alunosLoading.collectAsState()
    val allTurmas by adminViewModel.turmas.collectAsState()
    val apiResponsaveis by adminViewModel.responsaveis.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) {
            adminViewModel.loadAlunos(schoolId)
            adminViewModel.loadUsuarios(schoolId)
        }
    }

    val parentMap by remember(apiResponsaveis) {
        derivedStateOf {
            buildMap<String, String> {
                apiResponsaveis.forEach { resp ->
                    resp.memberships
                        .filter { it.role == "parent" }
                        .flatMap { it.associatedStudents }
                        .forEach { s -> put(s.id, resp.fullName) }
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

            if (allTurmas.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    item {
                        AlunoFilterChip(label = "Todas", selected = selectedClassId == null, colors = colors) {
                            selectedClassId = null
                        }
                    }
                    items(allTurmas, key = { it.id }) { turma ->
                        AlunoFilterChip(label = turma.name, selected = selectedClassId == turma.id, colors = colors) {
                            selectedClassId = if (selectedClassId == turma.id) null else turma.id
                        }
                    }
                }
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
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        items(filtrados, key = { it.id }) { aluno ->
                            AlunoRow(
                                aluno = aluno,
                                turmaNome = aluno.classId?.let { turmaMap[it] },
                                parentNome = parentMap[aluno.id],
                                colors = colors
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlunoFilterChip(
    label: String,
    selected: Boolean,
    colors: ComunicacaoEscolarColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) colors.buttonContainer else colors.surface)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (selected) {
            Icon(imageVector = Icons.Outlined.Check, contentDescription = null, tint = colors.buttonText, modifier = Modifier.size(14.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.buttonText else colors.textSecondary
        )
    }
}

@Composable
private fun AlunoRow(
    aluno: AlunoAdmin,
    turmaNome: String?,
    parentNome: String?,
    colors: ComunicacaoEscolarColors
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.lightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aluno.nome.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = aluno.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
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
                        color = colors.textSecondary
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.lightGray)
        )
    }
}
