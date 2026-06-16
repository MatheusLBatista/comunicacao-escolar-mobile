package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudent
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaida
import dev.fslab.comunicacao.escolar.model.PickupLogUi
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AutorizacaoFiltro
import dev.fslab.comunicacao.escolar.ui.viewmodel.AutorizacaoSaidaUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AutorizacaoSaidaViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.PickupLogsUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AutorizacaoSaidaScreen(
    onBack: () -> Unit,
    canCreate: Boolean = true,
    canRegisterSaida: Boolean = false,
    viewModel: AutorizacaoSaidaViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    val cancelando by viewModel.cancelando.collectAsState()
    val registrando by viewModel.registrando.collectAsState()
    val showSheet by viewModel.showNovaAutorizacaoSheet.collectAsState()
    val criando by viewModel.criando.collectAsState()
    val criarErro by viewModel.criarErro.collectAsState()
    val alunos by viewModel.alunos.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val pickupLogsState by viewModel.pickupLogsState.collectAsState()
    val revertendo by viewModel.revertendo.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(title = "Autorizações de Saída", onBack = onBack)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroChip(
                    label = "Ativas",
                    selecionado = filtro is AutorizacaoFiltro.Ativas,
                    onClick = { if (filtro !is AutorizacaoFiltro.Ativas) viewModel.setFiltro(AutorizacaoFiltro.Ativas) },
                    colors = colors
                )
                FiltroChip(
                    label = "Canceladas",
                    selecionado = filtro is AutorizacaoFiltro.Canceladas,
                    onClick = { if (filtro !is AutorizacaoFiltro.Canceladas) viewModel.setFiltro(AutorizacaoFiltro.Canceladas) },
                    colors = colors
                )
                FiltroChip(
                    label = "Saídas",
                    selecionado = filtro is AutorizacaoFiltro.Saidas,
                    onClick = { if (filtro !is AutorizacaoFiltro.Saidas) viewModel.setFiltro(AutorizacaoFiltro.Saidas) },
                    colors = colors
                )
            }

            if (filtro is AutorizacaoFiltro.Saidas) {
                when (val state = pickupLogsState) {
                    is PickupLogsUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Carregando saídas...", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                    is PickupLogsUiState.Empty -> Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma saída registrada.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                    is PickupLogsUiState.Error -> Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                    is PickupLogsUiState.Content -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.logs, key = { it.id }) { log ->
                            PickupLogCard(
                                log = log,
                                canReverter = canRegisterSaida,
                                revertendo = revertendo == log.id,
                                onReverter = { viewModel.reverterSaida(log.id, log.authorizationId) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            } else when (val state = uiState) {
                is AutorizacaoSaidaUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Carregando autorizações...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                is AutorizacaoSaidaUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadAutorizacoes() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }

                is AutorizacaoSaidaUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma autorização de saída.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                is AutorizacaoSaidaUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.autorizacoes, key = { it.id }) { autorizacao ->
                            AutorizacaoCard(
                                autorizacao = autorizacao,
                                cancelando = cancelando == autorizacao.id,
                                onCancelar = { viewModel.cancelarAutorizacao(autorizacao.id) },
                                canRegisterSaida = canRegisterSaida,
                                registrando = registrando == autorizacao.id,
                                onRegistrarSaida = { viewModel.registrarSaida(autorizacao) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        if (canCreate && filtro !is AutorizacaoFiltro.Saidas) {
            FloatingActionButton(
                onClick = { viewModel.abrirNovaAutorizacao() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp),
                containerColor = colors.buttonContainer,
                contentColor = colors.buttonText,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Nova autorização")
            }
        }
    }

    if (canCreate && showSheet) {
        NovaAutorizacaoSheet(
            alunos = alunos,
            criando = criando,
            erro = criarErro,
            onDismiss = { viewModel.fecharNovaAutorizacao() },
            onCriar = { nome, documento, relacao, fromMs, untilMs, studentId ->
                viewModel.criarAutorizacao(nome, documento, relacao, fromMs, untilMs, studentId)
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaAutorizacaoSheet(
    alunos: List<ApiAssociatedStudent>,
    criando: Boolean,
    erro: String?,
    onDismiss: () -> Unit,
    onCriar: (nome: String, documento: String, relacao: String, fromMs: Long, untilMs: Long, studentId: String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")) }

    var alunoSelecionado by remember { mutableStateOf(alunos.firstOrNull()) }
    LaunchedEffect(alunos) {
        if (alunoSelecionado == null) alunoSelecionado = alunos.firstOrNull()
    }
    var nome by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var relacao by remember { mutableStateOf("") }
    var validFromMs by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var validUntilMs by remember {
        mutableStateOf(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.timeInMillis
        )
    }

    val validFromText = remember(validFromMs) { dateFormatter.format(Date(validFromMs)) }
    val validUntilText = remember(validUntilMs) { dateFormatter.format(Date(validUntilMs)) }

    fun showDatePicker(initialMs: Long, onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMs }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onSelected(selected.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null
    ) {
        val sheetView = LocalView.current
        val isDark = colors.isDark
        val bgColor = colors.background
        SideEffect {
            val window = (sheetView.parent as? DialogWindowProvider)?.window
            if (window != null) {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
                window.navigationBarColor = bgColor.toArgb()
                WindowCompat.getInsetsController(window, sheetView)
                    .isAppearanceLightNavigationBars = !isDark
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nova Autorização",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Autorizar alguém a buscar seu(s) filho(s)",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
                IconButton(onClick = onDismiss, enabled = !criando) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (alunos.size > 1) {
                Text(
                    text = "ALUNO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (alunos.size > 5) {
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredAlunos = remember(alunos, searchQuery) {
                        if (searchQuery.isBlank()) alunos
                        else alunos.filter { it.fullName.contains(searchQuery, ignoreCase = true) }
                    }
                    Box {
                        OutlinedTextField(
                            value = alunoSelecionado?.fullName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Selecionar aluno",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors(colors)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { dropdownExpanded = true; searchQuery = "" }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(colors.surface)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Buscar aluno...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = fieldColors(colors)
                            )
                            filteredAlunos.forEach { aluno ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            aluno.fullName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textPrimary
                                        )
                                    },
                                    onClick = { alunoSelecionado = aluno; dropdownExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        alunos.forEach { aluno ->
                            val selecionado = alunoSelecionado?.id == aluno.id
                            val firstName = aluno.fullName.trim().split(" ").firstOrNull() ?: aluno.fullName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (selecionado) colors.focusedIndicator else colors.inputBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .background(if (selecionado) colors.surface else colors.background)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { alunoSelecionado = aluno }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = firstName,
                                    fontSize = 14.sp,
                                    fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selecionado) colors.textPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "QUEM VAI BUSCAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    placeholder = {
                        Text(
                            "Quem vai buscar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(colors)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DOCUMENTO (CPF)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = documento,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(11)
                        documento = buildString {
                            digits.forEachIndexed { i, c ->
                                append(c)
                                if (i == 2 || i == 5) append('.')
                                if (i == 8) append('-')
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            "Documento (CPF)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors(colors)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RELAÇÃO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = relacao,
                    onValueChange = { relacao = it },
                    placeholder = {
                        Text(
                            "Relação",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(colors)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Datas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PERÍODO DE VIGÊNCIA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DE",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Box {
                            OutlinedTextField(
                                value = validFromText,
                                onValueChange = {},
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(colors)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showDatePicker(validFromMs) { validFromMs = it } }
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ATÉ",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Box {
                            OutlinedTextField(
                                value = validUntilText,
                                onValueChange = {},
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors(colors)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showDatePicker(validUntilMs) { validUntilMs = it } }
                            )
                        }
                    }
                }
            }

            if (erro != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = erro, fontSize = 13.sp, color = colors.errorText)
            }

            Spacer(modifier = Modifier.height(20.dp))

            val isValid = alunoSelecionado != null && nome.isNotBlank() &&
                    documento.filter { it.isDigit() }.length == 11 && relacao.isNotBlank() && validUntilMs > validFromMs
            Button(
                onClick = { alunoSelecionado?.id?.let { onCriar(nome, documento, relacao, validFromMs, validUntilMs, it) } },
                enabled = isValid && !criando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.lightGray,
                    disabledContentColor = colors.textSecondary
                )
            ) {
                if (criando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.buttonText
                    )
                } else {
                    Text(
                        text = "Criar Autorização",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun fieldColors(colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.focusedIndicator,
        unfocusedBorderColor = colors.inputBorder,
        focusedTextColor = colors.textInput,
        unfocusedTextColor = colors.textInput,
        cursorColor = colors.focusedIndicator,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface
    )

@Composable
private fun AutorizacaoCard(
    autorizacao: AutorizacaoSaida,
    cancelando: Boolean,
    onCancelar: () -> Unit,
    canRegisterSaida: Boolean = false,
    registrando: Boolean = false,
    onRegistrarSaida: () -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    var showConfirm by remember { mutableStateOf(false) }
    var showConfirmSaida by remember { mutableStateOf(false) }

    if (showConfirmSaida) {
        AlertDialog(
            onDismissRequest = { showConfirmSaida = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.background,
            title = {
                Text(
                    text = "Registrar saída",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Confirmar saída de ${autorizacao.studentName} com ${autorizacao.autorizadoPor}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmSaida = false; onRegistrarSaida() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonContainer,
                        contentColor = colors.buttonText
                    )
                ) {
                    Text(
                        "Confirmar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSaida = false }) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.background,
            title = {
                Text(
                    text = "Cancelar autorização",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Tem certeza que deseja cancelar esta autorização? Ela não poderá ser usada após o cancelamento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm = false; onCancelar() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Confirmar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(
                        "Manter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.lightGray),
                contentAlignment = Alignment.Center
            ) {
                if (autorizacao.studentAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(autorizacao.studentAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = autorizacao.studentName,
                        modifier = Modifier.size(44.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.iconGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = autorizacao.studentName,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (autorizacao.className.isNotBlank()) {
                    Text(
                        text = autorizacao.className,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = autorizacao.status,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.lightGray)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow(
                label = "Pessoa autorizada",
                value = if (autorizacao.relacao.isNotBlank())
                    "${autorizacao.autorizadoPor} · ${autorizacao.relacao}"
                else
                    autorizacao.autorizadoPor
            )
            if (autorizacao.autorizadoDocumento.isNotBlank()) {
                InfoRow(label = "CPF", value = formatCpf(autorizacao.autorizadoDocumento))
            }
            InfoRow(label = "Válido até", value = autorizacao.validAte)
        }

        if (autorizacao.status == "Aguardando saída") {
            Spacer(modifier = Modifier.height(12.dp))

            if (canRegisterSaida) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.buttonContainer)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !registrando,
                            onClick = { showConfirmSaida = true }
                        )
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (registrando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colors.buttonText
                        )
                    } else {
                        Text(
                            text = "Registrar saída",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.buttonText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !cancelando,
                        onClick = { showConfirm = true }
                    )
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cancelando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.textSecondary
                    )
                } else {
                    Text(
                        text = "Cancelar autorização",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

private fun formatCpf(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.length != 11) return raw
    return "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9)}"
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, fontSize = 11.sp, letterSpacing = 0.3.sp, color = colors.textSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
    }
}

@Composable
private fun FiltroChip(
    label: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selecionado) colors.surface else colors.background)
            .border(
                1.dp,
                if (selecionado) colors.focusedIndicator else colors.inputBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selecionado) colors.textPrimary else colors.textSecondary
        )
    }
}

@Composable
private fun PickupLogCard(
    log: PickupLogUi,
    canReverter: Boolean = false,
    revertendo: Boolean = false,
    onReverter: () -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    var showConfirmReverter by remember { mutableStateOf(false) }

    if (showConfirmReverter) {
        AlertDialog(
            onDismissRequest = { showConfirmReverter = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.background,
            title = {
                Text(
                    text = "Reverter saída",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Reverter o registro de saída de ${log.studentName}? A autorização voltará a ficar ativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmReverter = false; onReverter() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Reverter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReverter = false }) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.lightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = colors.iconGray,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = log.studentName,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (log.className.isNotBlank()) {
                    Text(
                        text = log.className,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = log.departureTime,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.lightGray)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (log.pickedUpByName.isNotBlank()) {
                val pickedUpByLabel = if (log.pickedUpByRelationship.isNotBlank())
                    "${log.pickedUpByName} · ${log.pickedUpByRelationship}"
                else
                    log.pickedUpByName
                InfoRow(label = "Buscado por", value = pickedUpByLabel)
            }
            if (log.pickedUpByDocument.isNotBlank()) {
                InfoRow(label = "CPF", value = formatCpf(log.pickedUpByDocument))
            }
            if (log.verifiedByName.isNotBlank()) {
                InfoRow(label = "Verificado por", value = log.verifiedByName)
            }
        }
        if (canReverter) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !revertendo,
                        onClick = { showConfirmReverter = true }
                    )
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                if (revertendo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.textSecondary
                    )
                } else {
                    Text(
                        text = "Reverter saída",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}