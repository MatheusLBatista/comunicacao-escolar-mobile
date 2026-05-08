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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.ComunicadoTemplate
import dev.fslab.comunicacao.escolar.model.TipoCampo
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import java.util.UUID

@Stable
private class CampoUiState(
    id: String,
    nome: String,
    tipo: TipoCampo,
    opcoes: List<String>
) {
    val id: String = id
    var nome by mutableStateOf(nome)
    var tipo by mutableStateOf(tipo)
    val opcoes = mutableStateListOf<String>().also { it.addAll(opcoes) }
    var novaOpcao by mutableStateOf("")
    var showAddOpcao by mutableStateOf(false)
}

@Composable
fun TemplateDetailScreen(
    template: ComunicadoTemplate,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val campos = remember(template.id) {
        mutableStateListOf<CampoUiState>().also { list ->
            template.campos.forEach { campo ->
                list.add(CampoUiState(campo.id, campo.nome, campo.tipo, campo.opcoes))
            }
        }
    }

    AdminSubScreenScaffold(
        title = template.nome,
        onBack = onBack,
        action = {
            TextButton(onClick = { focusManager.clearFocus() }) {
                Text(
                    "Salvar",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalComunicacaoEscolarColors.current.textPrimary
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(campos, key = { _, c -> c.id }) { index, campo ->
                CampoCard(
                    campo = campo,
                    onDelete = { campos.removeAt(index) }
                )
            }

            item {
                AddCampoCard(onClick = {
                    campos.add(
                        CampoUiState(
                            id = UUID.randomUUID().toString(),
                            nome = "",
                            tipo = TipoCampo.SELECAO,
                            opcoes = emptyList()
                        )
                    )
                })
            }
        }
    }
}

@Composable
private fun CampoCard(
    campo: CampoUiState,
    onDelete: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        // Header row: drag handle + name + delete
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = "Reordenar",
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = campo.nome.ifEmpty { "Novo campo" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (campo.nome.isEmpty()) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Excluir campo",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Type selector chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoCampo.entries.forEach { tipo ->
                TipoChip(
                    label = tipo.label,
                    selected = campo.tipo == tipo,
                    onClick = { campo.tipo = tipo }
                )
            }
        }

        // Options list (only for Seleção)
        if (campo.tipo == TipoCampo.SELECAO) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                campo.opcoes.forEachIndexed { idx, opcao ->
                    OpcaoRow(
                        value = opcao,
                        onValueChange = { campo.opcoes[idx] = it },
                        onRemove = { campo.opcoes.removeAt(idx) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { campo.opcoes.add("") },
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Adicionar opção",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun TipoChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) colors.buttonContainer else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else colors.inputBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.buttonText else colors.textSecondary
        )
    }
}

@Composable
private fun OpcaoRow(
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        "Nova opção",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                inner()
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remover",
                tint = colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddCampoCard(onClick: () -> Unit) {
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
            text = "Adicionar campo",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}
