package dev.fslab.comunicacao.escolar.ui.screens.professor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiTemplateField
import dev.fslab.comunicacao.escolar.ui.components.AppToast
import dev.fslab.comunicacao.escolar.ui.components.rememberAppToastState
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.StudentDailyLogState
import dev.fslab.comunicacao.escolar.ui.viewmodel.SubmitState

@Composable
fun DiarioDeBordoScreen(
    viewModel: DiarioDeBordoViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    val toastState = rememberAppToastState()

    LaunchedEffect(uiState) {
        val state = uiState as? DiarioDeBordoUiState.Content ?: return@LaunchedEffect
        when (val submit = state.submitState) {
            is SubmitState.Success -> {
                toastState.showSuccess("Diário enviado com sucesso!")
                viewModel.resetSubmitState()
            }
            is SubmitState.Error -> {
                toastState.showError(submit.message)
                viewModel.resetSubmitState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when (val state = uiState) {
            is DiarioDeBordoUiState.Loading -> DiarioLoadingContent()
            is DiarioDeBordoUiState.Error -> DiarioErrorContent(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is DiarioDeBordoUiState.Content -> DiarioContent(
                state = state,
                onSelectClass = { viewModel.selectClass(it) },
                onTogglePresence = { viewModel.togglePresence(it) },
                onUpdateField = { studentId, key, value -> viewModel.updateField(studentId, key, value) },
                onUpdateObservation = { studentId, text -> viewModel.updateObservation(studentId, text) },
                onSubmit = { viewModel.submit() }
            )
        }
        AppToast(toastState)
    }
}

@Composable
private fun DiarioLoadingContent() {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = colors.primary,
            strokeWidth = 2.5.dp
        )
    }
}

@Composable
private fun DiarioErrorContent(message: String, onRetry: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tentar novamente", fontSize = 13.sp)
        }
    }
}

@Composable
private fun DiarioContent(
    state: DiarioDeBordoUiState.Content,
    onSelectClass: (String) -> Unit,
    onTogglePresence: (String) -> Unit,
    onUpdateField: (String, String, String) -> Unit,
    onUpdateObservation: (String, String) -> Unit,
    onSubmit: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val isSubmitting = state.submitState is SubmitState.Submitting

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(colors.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Diário de Bordo",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hoje",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                ClassTabRow(
                    classes = state.classes,
                    selectedClassId = state.selectedClassId,
                    onSelect = onSelectClass
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (state.students.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum aluno nesta turma.",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                items(state.students, key = { it.studentId }) { student ->
                    StudentCard(
                        student = student,
                        templateFields = state.templateFields,
                        onTogglePresence = { onTogglePresence(student.studentId) },
                        onUpdateField = { key, value -> onUpdateField(student.studentId, key, value) },
                        onUpdateObservation = { text -> onUpdateObservation(student.studentId, text) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.background,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.mediumGray,
                    disabledContentColor = Color.White
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Finalizar e Enviar Diário",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassTabRow(
    classes: List<ApiClass>,
    selectedClassId: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        classes.forEach { cls ->
            val isSelected = cls.id == selectedClassId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) colors.primary else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else colors.inputBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(cls.id) }
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cls.name,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: StudentDailyLogState,
    templateFields: List<ApiTemplateField>,
    onTogglePresence: () -> Unit,
    onUpdateField: (String, String) -> Unit,
    onUpdateObservation: (String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.lightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (!student.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(student.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = student.studentName,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = colors.iconGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = student.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                PresenceToggle(
                    isPresent = student.isPresent,
                    onToggle = onTogglePresence
                )
            }

            AnimatedVisibility(
                visible = student.isPresent,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    if (templateFields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FieldDropdownRow(
                            fields = templateFields,
                            fieldValues = student.fieldValues,
                            onSelect = onUpdateField
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = student.observation,
                        onValueChange = onUpdateObservation,
                        placeholder = {
                            Text(
                                text = "Adicione uma observação individual...",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.focusedIndicator,
                            unfocusedBorderColor = colors.inputBorder,
                            focusedContainerColor = colors.background,
                            unfocusedContainerColor = colors.background,
                            cursorColor = colors.focusedIndicator
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PresenceToggle(isPresent: Boolean, onToggle: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isPresent) colors.primary else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isPresent) Color.Transparent else colors.inputBorder,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = if (isPresent) "Presente" else "Ausente",
            tint = if (isPresent) Color.White else colors.inputBorder,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun FieldDropdownRow(
    fields: List<ApiTemplateField>,
    fieldValues: Map<String, String>,
    onSelect: (String, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        fields.forEach { field ->
            FieldDropdown(
                field = field,
                selectedValue = fieldValues[field.key] ?: "",
                onSelect = { value -> onSelect(field.key, value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FieldDropdown(
    field: ApiTemplateField,
    selectedValue: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalComunicacaoEscolarColors.current
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedValue.ifBlank { field.label }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = true }
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayText,
                fontSize = 12.sp,
                color = if (selectedValue.isBlank()) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            field.options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
