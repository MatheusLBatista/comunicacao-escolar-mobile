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
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.runtime.rememberCoroutineScope
import dev.fslab.comunicacao.escolar.ui.components.ConfirmDialog
import dev.fslab.comunicacao.escolar.model.ComunicadoTemplate
import dev.fslab.comunicacao.escolar.model.CreateTemplateFieldRequest
import dev.fslab.comunicacao.escolar.model.TipoCampo
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AdminViewModel
import java.util.UUID
import kotlinx.coroutines.launch

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
    var submitted by mutableStateOf(false)
    val opcoes = mutableStateListOf<String>().also { it.addAll(opcoes) }
    var novaOpcao by mutableStateOf("")
    var showAddOpcao by mutableStateOf(false)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateDetailScreen(
    template: ComunicadoTemplate,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val colors = LocalComunicacaoEscolarColors.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val campos = remember(template.id) {
        mutableStateListOf<CampoUiState>().also { list ->
            template.campos.forEach { campo ->
                list.add(CampoUiState(campo.id, campo.nome, campo.tipo, campo.opcoes))
            }
        }
    }
    val hasChanges by remember {
        derivedStateOf {
            if (campos.size != template.campos.size) return@derivedStateOf true
            campos.zip(template.campos).any { (cur, orig) ->
                cur.nome != orig.nome ||
                cur.tipo != orig.tipo ||
                cur.opcoes.toList() != orig.opcoes
            }
        }
    }

    fun buildFields() = campos.map { c ->
        CreateTemplateFieldRequest(
            key = c.nome.trim().lowercase().replace(" ", "_").ifBlank { c.id },
            label = c.nome.trim().ifBlank { "Campo" },
            type = when (c.tipo) {
                TipoCampo.SELECAO -> "select"
                TipoCampo.SIM_NAO -> "boolean"
                TipoCampo.TEXTO_LIVRE -> "text"
            },
            options = if (c.tipo == TipoCampo.SELECAO) c.opcoes.toList() else emptyList()
        )
    }

    fun tryBack() {
        if (hasChanges) showDiscardDialog = true else onBack()
    }

    BackHandler(enabled = hasChanges) { tryBack() }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Excluir template",
            message = "Excluir \"${template.nome}\"? Esta ação não pode ser desfeita.",
            confirmLabel = "Excluir",
            onConfirm = {
                showDeleteDialog = false
                adminViewModel.deleteTemplate(template.id) { onBack() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showDiscardDialog) {
        ConfirmDialog(
            title = "Descartar alterações?",
            message = "As alterações feitas neste template serão perdidas.",
            confirmLabel = "Descartar",
            onConfirm = { onBack() },
            onDismiss = { showDiscardDialog = false }
        )
    }

    AdminSubScreenScaffold(
        title = template.nome,
        onBack = { tryBack() },
        action = {
            if (hasChanges) {
                TextButton(onClick = {
                    submitted = true
                    campos.forEach { it.submitted = true }
                    val valid = campos.all { it.nome.isNotBlank() } &&
                        campos.all { it.tipo != TipoCampo.SELECAO || it.opcoes.isNotEmpty() } &&
                        campos.all { it.tipo != TipoCampo.SELECAO || it.opcoes.all { o -> o.isNotBlank() } }
                    if (!valid) return@TextButton
                    focusManager.clearFocus()
                    adminViewModel.updateTemplate(template.id, buildFields()) { onBack() }
                }) {
                    Text(
                        "Salvar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            } else {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir template",
                        tint = colors.textSecondary
                    )
                }
            }
        }
    ) {
        val lazyListState = rememberLazyListState()
        val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
            campos.apply { add(to.index, removeAt(from.index)) }
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(campos, key = { _, c -> c.id }) { _, campo ->
                ReorderableItem(reorderState, key = campo.id) {
                    CampoCard(
                        campo = campo,
                        onDelete = { campos.remove(campo) },
                        dragHandleModifier = Modifier.draggableHandle()
                    )
                }
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun NovoTemplateScreen(
    schoolId: String,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var nome by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var nomeFieldFocused by remember { mutableStateOf(false) }
    val campos = remember { mutableStateListOf<CampoUiState>() }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val imeVisible = WindowInsets.isImeVisible
    val cursorBrush = androidx.compose.ui.graphics.SolidColor(
        if (imeVisible) colors.textPrimary else Color.Transparent
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = colors.textPrimary
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = nome,
                onValueChange = { nome = it },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { nomeFieldFocused = it.isFocused },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                cursorBrush = cursorBrush,
                singleLine = true,
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center) {
                        if (nome.isEmpty() && !(nomeFieldFocused && imeVisible)) {
                            Text(
                                if (submitted) "Nome obrigatório" else "Nome do template",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (submitted) colors.error else colors.textSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        inner()
                    }
                }
            )
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
                TextButton(
                    onClick = {
                        submitted = true
                        campos.forEach { it.submitted = true }
                        val valid = nome.isNotBlank() &&
                            campos.isNotEmpty() &&
                            campos.all { it.nome.isNotBlank() } &&
                            campos.all { it.tipo != TipoCampo.SELECAO || it.opcoes.isNotEmpty() } &&
                            campos.all { it.tipo != TipoCampo.SELECAO || it.opcoes.all { o -> o.isNotBlank() } }
                        if (!valid) return@TextButton
                        focusManager.clearFocus()
                        scope.launch {
                            val fields = campos.map { c ->
                                CreateTemplateFieldRequest(
                                    key = c.nome.trim().lowercase().replace(" ", "_")
                                        .ifBlank { UUID.randomUUID().toString() },
                                    label = c.nome.trim().ifBlank { "Campo" },
                                    type = when (c.tipo) {
                                        TipoCampo.SELECAO -> "select"
                                        TipoCampo.SIM_NAO -> "boolean"
                                        TipoCampo.TEXTO_LIVRE -> "text"
                                    },
                                    options = if (c.tipo == TipoCampo.SELECAO) c.opcoes.toList() else emptyList()
                                )
                            }
                            adminViewModel.createTemplate(schoolId, nome.trim(), fields) {
                                onBack()
                            }
                        }
                    }
                ) {
                    Text(
                        "Salvar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(
            if (submitted && nome.isBlank()) colors.error else colors.lightGray
        ))

        val lazyListStateNovo = rememberLazyListState()
        val reorderStateNovo = rememberReorderableLazyListState(lazyListStateNovo) { from, to ->
            campos.apply { add(to.index, removeAt(from.index)) }
        }
        LazyColumn(
            state = lazyListStateNovo,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(campos, key = { _, c -> c.id }) { _, campo ->
                ReorderableItem(reorderStateNovo, key = campo.id) {
                    CampoCard(
                        campo = campo,
                        onDelete = { campos.remove(campo) },
                        dragHandleModifier = Modifier.draggableHandle()
                    )
                }
            }

            item {
                if (submitted && campos.isEmpty()) {
                    Text(
                        "Adicione ao menos 1 campo",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CampoCard(
    campo: CampoUiState,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier = Modifier
) {
    val colors = LocalComunicacaoEscolarColors.current
    val focusManager = LocalFocusManager.current
    val imeVisible = WindowInsets.isImeVisible
    val showNameError = campo.submitted && campo.nome.isBlank()
    val showOptionsError = campo.submitted && campo.tipo == TipoCampo.SELECAO && campo.opcoes.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(14.dp)
    ) {
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
                    .then(dragHandleModifier)
            )
            Spacer(modifier = Modifier.width(4.dp))
            var nomeFocused by remember { mutableStateOf(false) }
            androidx.compose.foundation.text.BasicTextField(
                value = campo.nome,
                onValueChange = { campo.nome = it },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { nomeFocused = it.isFocused },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.textPrimary),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (campo.nome.isEmpty() && !(nomeFocused && imeVisible)) {
                            Text(
                                "Nome do campo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                        }
                        inner()
                    }
                }
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
        if (showNameError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Preencha o nome do campo",
                style = MaterialTheme.typography.labelSmall,
                color = colors.error
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoCampo.entries.forEach { tipo ->
                TipoChip(
                    label = tipo.label,
                    selected = campo.tipo == tipo,
                    onClick = { campo.tipo = tipo }
                )
            }
        }

        if (campo.tipo == TipoCampo.SELECAO) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                campo.opcoes.forEachIndexed { idx, opcao ->
                    OpcaoRow(
                        value = opcao,
                        onValueChange = { campo.opcoes[idx] = it },
                        onRemove = { campo.opcoes.removeAt(idx) },
                        showError = campo.submitted && opcao.isBlank()
                    )
                }
            }
            if (showOptionsError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Adicione pelo menos 1 opção",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.error
                )
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
    onRemove: () -> Unit,
    showError: Boolean = false
) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .then(
                if (showError) Modifier.border(1.dp, colors.error, RoundedCornerShape(8.dp))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var opcaoFocused by remember { mutableStateOf(false) }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { opcaoFocused = it.isFocused },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.textPrimary),
            singleLine = true,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty() && !opcaoFocused) {
                        Text(
                            "Nova opção",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    inner()
                }
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
