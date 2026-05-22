package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.ComunicadoTemplate
import dev.fslab.comunicacao.escolar.ui.components.ConfirmDialog
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel

@Composable
fun TemplatesScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onTemplateClick: (ComunicadoTemplate) -> Unit,
    onNovoTemplate: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val apiTemplates by adminViewModel.templates.collectAsState()
    var templateToDelete by remember { mutableStateOf<ComunicadoTemplate?>(null) }

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) adminViewModel.loadTemplates(schoolId)
    }

    templateToDelete?.let { t ->
        ConfirmDialog(
            title = "Excluir template",
            message = "Excluir \"${t.nome}\"? Esta ação não pode ser desfeita.",
            confirmLabel = "Excluir",
            onConfirm = {
                adminViewModel.deleteTemplate(t.id) {}
                templateToDelete = null
            },
            onDismiss = { templateToDelete = null }
        )
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
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            templateToDelete = template
                        }
                        false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val bgColor by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> colors.error
                                else -> Color.Transparent
                            },
                            label = "swipe_delete_bg"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Excluir template",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                        }
                    }
                ) {
                    TemplateCard(template = template, onClick = { onTemplateClick(template) })
                }
            }

            item {
                NovoTemplateCard(onClick = onNovoTemplate)
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
                text = template.nome,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
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
            modifier = Modifier.size(18.dp)
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
