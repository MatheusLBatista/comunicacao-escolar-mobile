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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import dev.fslab.comunicacao.escolar.ui.theme.ErrorText
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    val actionError by adminViewModel.actionError.collectAsState()
    val actionSuccess by adminViewModel.actionSuccess.collectAsState()
    val linkFieldError by adminViewModel.linkFieldError.collectAsState()

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var perfil by remember { mutableStateOf(PerfilAcesso.PROFESSOR) }
    var nomeAluno by remember { mutableStateOf("") }
    var selectedTurmaAluno by remember { mutableStateOf<Turma?>(null) }
    var selectedTurmaProfessor by remember { mutableStateOf<Turma?>(null) }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank() && apiTurmas.isEmpty()) adminViewModel.loadTurmas(schoolId)
    }

    LaunchedEffect(actionError) {
        if (actionError != null) {
            emailError = actionError
            adminViewModel.clearActionError()
        }
    }

    LaunchedEffect(linkFieldError) {
        if (linkFieldError != null) {
            emailError = linkFieldError
            adminViewModel.clearLinkFieldError()
        }
    }

    LaunchedEffect(actionSuccess) {
        if (actionSuccess != null) {
            adminViewModel.clearActionSuccess()
            onBack()
        }
    }

    val isEnabled = email.isNotBlank() && when (perfil) {
        PerfilAcesso.PROFESSOR -> true
        PerfilAcesso.RESPONSAVEL -> {
            val bothEmpty = nomeAluno.isBlank() && selectedTurmaAluno == null
            val bothFilled = nomeAluno.isNotBlank() && selectedTurmaAluno != null
            bothEmpty || bothFilled
        }
    }

    AdminSubScreenScaffold(title = "Vincular Usuário", onBack = onBack) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "E-MAIL DO USUÁRIO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
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
                                tint = if (emailError != null) ErrorText else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        supportingText = {
                            Text(
                                text = emailError ?: "Um e-mail de convite será enviado para o usuário.",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (emailError != null) ErrorText else colors.textSecondary
                            )
                        },
                        isError = emailError != null,
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
                            unfocusedContainerColor = colors.surface,
                            errorBorderColor = ErrorText,
                            errorTextColor = colors.textInput,
                            errorContainerColor = colors.surface,
                            errorLeadingIconColor = ErrorText,
                            errorCursorColor = ErrorText,
                            errorSupportingTextColor = ErrorText
                        )
                    )
                }

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

                AnimatedVisibility(
                    visible = perfil == PerfilAcesso.PROFESSOR,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "TURMA (OPCIONAL)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                        TurmaDropdownField(
                            turmas = turmas,
                            selectedTurma = selectedTurmaProfessor,
                            onTurmaSelected = { selectedTurmaProfessor = it },
                            placeholder = "Selecione a turma..."
                        )
                    }
                }

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

                        TurmaDropdownField(
                            turmas = turmas,
                            selectedTurma = selectedTurmaAluno,
                            onTurmaSelected = { selectedTurmaAluno = it },
                            placeholder = "Selecione a turma do aluno..."
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            emailError = "Digite um e-mail válido."
                            return@Button
                        }
                        val roleStr = if (perfil == PerfilAcesso.PROFESSOR) "teacher" else "parent"
                        adminViewModel.linkToSchool(
                            schoolId = schoolId,
                            email = trimmedEmail,
                            role = roleStr,
                            studentName = if (perfil == PerfilAcesso.RESPONSAVEL) nomeAluno.trim().ifBlank { null } else null,
                            classId = when (perfil) {
                                PerfilAcesso.RESPONSAVEL -> selectedTurmaAluno?.id
                                PerfilAcesso.PROFESSOR -> selectedTurmaProfessor?.id
                            },
                            onSuccess = {}
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEnabled,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Confirmar Vínculo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

    }
}
