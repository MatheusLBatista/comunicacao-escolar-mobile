package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.ApiDailyLogTemplate
import dev.fslab.comunicacao.escolar.model.CampoTemplate
import dev.fslab.comunicacao.escolar.model.ComunicadoTemplate
import dev.fslab.comunicacao.escolar.model.TipoCampo
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight

@Composable
fun TemplatesScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onTemplateClick: (ComunicadoTemplate) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val apiTemplates by adminViewModel.templates.collectAsState()
    var showInlineCreate by remember { mutableStateOf(false) }
    var novoNome by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) adminViewModel.loadTemplates(schoolId)
    }

    AdminSubScreenScaffold(title = "Templates de Comunicado", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Gerencie os modelos de campos que os professores\npreenchem no diário de bordo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(apiTemplates.map { it.toComunicadoTemplate() }, key = { it.id }) { template ->
                TemplateCard(template = template, onClick = { onTemplateClick(template) })
            }

            item {
                AnimatedVisibility(
                    visible = showInlineCreate,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    InlineCreateTemplateCard(
                        nome = novoNome,
                        onNomeChange = { novoNome = it },
                        onCancel = {
                            showInlineCreate = false
                            novoNome = ""
                            focusManager.clearFocus()
                        },
                        onCreate = {
                            if (novoNome.isNotBlank()) {
                                adminViewModel.createTemplate(schoolId, novoNome.trim()) {
                                    scope.launch {
                                        novoNome = ""
                                        showInlineCreate = false
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                        }
                    )
                }

                if (!showInlineCreate) {
                    NovoTemplateCard(onClick = { showInlineCreate = true })
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(template: ComunicadoTemplate, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.nome,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${template.campos.size} campos",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun NovoTemplateCard(onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.inputBorder, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "Novo template",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun InlineCreateTemplateCard(
    nome: String,
    onNomeChange: (String) -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "NOVO TEMPLATE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        OutlinedTextField(
            value = nome,
            onValueChange = onNomeChange,
            placeholder = {
                Text(
                    "Nome do template",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onCreate() }),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.focusedIndicator,
                unfocusedBorderColor = colors.inputBorder,
                focusedTextColor = colors.textInput,
                unfocusedTextColor = colors.textInput,
                cursorColor = colors.focusedIndicator,
                focusedContainerColor = colors.background,
                unfocusedContainerColor = colors.background
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = colors.textSecondary
                )
            ) {
                Text("Cancelar", style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onCreate,
                modifier = Modifier.weight(1f),
                enabled = nome.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.inputBorder,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                Text("Criar", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
