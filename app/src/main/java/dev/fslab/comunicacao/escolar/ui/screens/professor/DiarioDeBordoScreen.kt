package dev.fslab.comunicacao.escolar.ui.screens.professor

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.ApiTemplateField
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.components.AppToast
import dev.fslab.comunicacao.escolar.ui.components.rememberAppToastState
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.StudentDailyLogState
import dev.fslab.comunicacao.escolar.ui.viewmodel.SubmitState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DiarioDeBordoScreen(
    classId: String,
    className: String,
    onBack: () -> Unit,
    viewModel: DiarioDeBordoViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    val toastState = rememberAppToastState()

    var selectedCalendar by rememberSaveable {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }

    LaunchedEffect(classId, selectedCalendar) {
        viewModel.load(classId, selectedCalendar.timeInMillis)
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(title = className, onBack = onBack)

            val contentState = uiState as? DiarioDeBordoUiState.Content
            DiarioControlsRow(
                calendar = selectedCalendar,
                onPickDate = { newMs ->
                    selectedCalendar = Calendar.getInstance().apply {
                        timeInMillis = newMs
                        set(Calendar.HOUR_OF_DAY, 12)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                },
                templates = contentState?.templates.orEmpty(),
                selectedTemplateId = contentState?.selectedTemplateId ?: "",
                onSelectTemplate = { viewModel.selectTemplate(it) }
            )

            when (val state = uiState) {
                is DiarioDeBordoUiState.Loading -> DiarioLoadingContent()
                is DiarioDeBordoUiState.Error -> DiarioErrorContent(
                    message = state.message,
                    onRetry = { viewModel.load(classId, selectedCalendar.timeInMillis) }
                )
                is DiarioDeBordoUiState.Content -> DiarioContent(
                    state = state,
                    onTogglePresence = { viewModel.togglePresence(it) },
                    onUpdateField = { studentId, key, value -> viewModel.updateField(studentId, key, value) },
                    onSubmit = { viewModel.submit() }
                )
            }
        }
        AppToast(toastState)
    }
}

@Composable
private fun DiarioControlsRow(
    calendar: Calendar,
    onPickDate: (Long) -> Unit,
    templates: List<dev.fslab.comunicacao.escolar.model.ApiDailyLogTemplate>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")) }
    val dateLabel = when {
        isToday(calendar) -> "Hoje"
        isYesterday(calendar) -> "Ontem"
        else -> dateFormatter.format(calendar.time)
    }
    var templateExpanded by remember { mutableStateOf(false) }
    val templateLabel = templates.find { it.id == selectedTemplateId }
        ?.name?.ifBlank { "Template" } ?: "Template"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.inputBorder, RoundedCornerShape(12.dp))
                .background(colors.surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val picked = Calendar.getInstance().apply {
                                set(year, month, day, 12, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            if (!isFuture(picked)) onPickDate(picked.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textInput,
                modifier = Modifier.weight(1f)
            )
        }

        if (templates.size > 1) {
            var templateDropdownWidth by remember { mutableStateOf(0) }
            val density = LocalDensity.current
            Box(modifier = Modifier.weight(1f).onSizeChanged { templateDropdownWidth = it.width }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, colors.inputBorder, RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { templateExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = templateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textInput,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = templateExpanded,
                    onDismissRequest = { templateExpanded = false },
                    modifier = Modifier
                        .width(with(density) { templateDropdownWidth.toDp() })
                        .background(colors.surface)
                ) {
                    templates.forEach { template ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = template.name.ifBlank { "Template" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = {
                                onSelectTemplate(template.id)
                                templateExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiarioLoadingContent() {
    val colors = LocalComunicacaoEscolarColors.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
    onTogglePresence: (String) -> Unit,
    onUpdateField: (String, String, String) -> Unit,
    onSubmit: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val isSubmitting = state.submitState is SubmitState.Submitting

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(colors.background),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (!state.isEditable) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.lightGray)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Visualização — edição disponível apenas no dia de hoje.",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
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
                        isEditable = state.isEditable,
                        onTogglePresence = { onTogglePresence(student.studentId) },
                        onUpdateField = { key, value -> onUpdateField(student.studentId, key, value) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (state.isEditable) {
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
}

@Composable
private fun StudentCard(
    student: StudentDailyLogState,
    templateFields: List<ApiTemplateField>,
    isEditable: Boolean,
    onTogglePresence: () -> Unit,
    onUpdateField: (String, String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
            .background(colors.background)
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
                enabled = isEditable,
                onToggle = onTogglePresence
            )
        }

        AnimatedVisibility(
            visible = student.isPresent,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            if (templateFields.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    TemplateFieldsSection(
                        fields = templateFields,
                        fieldValues = student.fieldValues,
                        isEditable = isEditable,
                        onUpdateField = onUpdateField
                    )
                }
            }
        }
    }
}

@Composable
private fun PresenceToggle(isPresent: Boolean, enabled: Boolean, onToggle: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    isPresent -> colors.buttonContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = 1.5.dp,
                color = if (isPresent) Color.Transparent else colors.inputBorder,
                shape = CircleShape
            )
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = if (isPresent) "Presente" else "Ausente",
            tint = if (isPresent) colors.buttonText else colors.inputBorder,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun groupTemplateFields(fields: List<ApiTemplateField>): List<List<ApiTemplateField>> {
    val result = mutableListOf<List<ApiTemplateField>>()
    var i = 0
    while (i < fields.size) {
        val field = fields[i]
        if (isCompactTemplateField(field)) {
            val next = fields.getOrNull(i + 1)
            if (next != null && isCompactTemplateField(next)) {
                result.add(listOf(field, next))
                i += 2
            } else {
                result.add(listOf(field))
                i++
            }
        } else {
            result.add(listOf(field))
            i++
        }
    }
    return result
}

private fun isCompactTemplateField(field: ApiTemplateField): Boolean =
    (field.type == "select" && field.options.isNotEmpty()) || field.type == "boolean"

@Composable
private fun TemplateFieldsSection(
    fields: List<ApiTemplateField>,
    fieldValues: Map<String, String>,
    isEditable: Boolean,
    onUpdateField: (String, String) -> Unit
) {
    val groups = remember(fields) { groupTemplateFields(fields) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        groups.forEach { group ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.forEach { field ->
                    TemplateField(
                        field = field,
                        value = fieldValues[field.key] ?: "",
                        isEditable = isEditable,
                        onUpdateField = { v -> onUpdateField(field.key, v) },
                        modifier = if (group.size > 1) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateField(
    field: ApiTemplateField,
    value: String,
    isEditable: Boolean,
    onUpdateField: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        field.type == "select" && field.options.isNotEmpty() -> SelectFieldDropdown(
            field = field,
            selectedValue = value,
            isEditable = isEditable,
            onSelect = onUpdateField,
            modifier = modifier
        )
        field.type == "boolean" -> BooleanFieldToggle(
            field = field,
            selectedValue = value,
            isEditable = isEditable,
            onSelect = onUpdateField,
            modifier = modifier
        )
        else -> TextTemplateField(
            field = field,
            value = value,
            isEditable = isEditable,
            onValueChange = onUpdateField,
            modifier = modifier
        )
    }
}

@Composable
private fun SelectFieldDropdown(
    field: ApiTemplateField,
    selectedValue: String,
    isEditable: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalComunicacaoEscolarColors.current
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedValue.ifBlank { field.label }
    var dropdownWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier.onSizeChanged { dropdownWidth = it.width }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.inputBorder, RoundedCornerShape(12.dp))
                .background(colors.surface)
                .then(
                    if (isEditable) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = true }
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedValue.isBlank()) colors.textSecondary else colors.textInput,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            if (isEditable) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isEditable) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(density) { dropdownWidth.toDp() })
                    .background(colors.surface)
            ) {
                field.options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(text = option, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun BooleanFieldToggle(
    field: ApiTemplateField,
    selectedValue: String,
    isEditable: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalComunicacaoEscolarColors.current
    val options = if (field.options.size >= 2) field.options.take(2) else listOf("Sim", "Não")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedValue == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colors.buttonContainer else colors.surface)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else colors.inputBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .then(
                        if (isEditable) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(option) } else Modifier
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) colors.buttonText else colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun TextTemplateField(
    field: ApiTemplateField,
    value: String,
    isEditable: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalComunicacaoEscolarColors.current
    OutlinedTextField(
        value = value,
        onValueChange = { if (isEditable) onValueChange(it) },
        placeholder = {
            Text(
                text = field.label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = !isEditable,
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textInput),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.focusedIndicator,
            unfocusedBorderColor = colors.inputBorder,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            cursorColor = colors.focusedIndicator,
            focusedTextColor = colors.textInput,
            unfocusedTextColor = colors.textInput
        )
    )
}

private fun isToday(cal: Calendar): Boolean {
    val now = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
    return cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
}

private fun isFuture(cal: Calendar): Boolean {
    val now = Calendar.getInstance()
    return cal.get(Calendar.YEAR) > now.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) > now.get(Calendar.DAY_OF_YEAR))
}
