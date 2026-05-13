package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel

private enum class PerfilAcesso(val label: String) {
    PROFESSOR("Professor"),
    RESPONSAVEL("Responsável")
}

@Composable
fun VincularUsuarioScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val apiTurmas by adminViewModel.turmas.collectAsState()
    val turmas = apiTurmas.map { it.toTurma() }
    var email by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf(PerfilAcesso.PROFESSOR) }
    var nomeAluno by remember { mutableStateOf("") }
    var selectedTurma by remember { mutableStateOf<Turma?>(null) }
    var showTurmaMenu by remember { mutableStateOf(false) }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank() && apiTurmas.isEmpty()) adminViewModel.loadTurmas(schoolId)
    }

    AdminSubScreenScaffold(title = "Vincular Usuário", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Email section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "E-MAIL DO USUÁRIO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            "usuario@email.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.MailOutline,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
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
                Text(
                    text = "O usuário precisa estar cadastrado no sistema.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            // Perfil de acesso section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PERFIL DE ACESSO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PerfilAcesso.entries.forEach { p ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (perfil == p) colors.buttonContainer else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { perfil = p }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (perfil == p) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (perfil == p) colors.buttonText else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Aluno vinculado (only for Responsável)
            AnimatedVisibility(
                visible = perfil == PerfilAcesso.RESPONSAVEL,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ALUNO VINCULADO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Ao vincular um responsável, é necessário informar os dados do aluno associado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    OutlinedTextField(
                        value = nomeAluno,
                        onValueChange = { nomeAluno = it },
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

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showTurmaMenu = true }
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
                            expanded = showTurmaMenu,
                            onDismissRequest = { showTurmaMenu = false },
                            modifier = Modifier.background(colors.background)
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
                                        showTurmaMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Confirm button
            val isEnabled = email.isNotBlank() &&
                    (perfil == PerfilAcesso.PROFESSOR || nomeAluno.isNotBlank())
            Button(
                onClick = {
                    val roleStr = if (perfil == PerfilAcesso.PROFESSOR) "teacher" else "parent"
                    adminViewModel.linkToSchool(
                        schoolId = schoolId,
                        email = email.trim(),
                        role = roleStr,
                        studentName = if (perfil == PerfilAcesso.RESPONSAVEL) nomeAluno.trim() else null,
                        classId = if (perfil == PerfilAcesso.RESPONSAVEL) selectedTurma?.id else null,
                        onSuccess = { onBack() }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.inputBorder,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    "Confirmar Vínculo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
