package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.Evento
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AgendaViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val diasSemana = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaResponsavelScreen(
    user: User,
    accessToken: String,
    canCreate: Boolean = false,
    agendaViewModel: AgendaViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val mesAtual by agendaViewModel.mesAtual.collectAsState()
    val diaSelecionado by agendaViewModel.diaSelecionado.collectAsState()
    val loading by agendaViewModel.loading.collectAsState()
    val diasComEventos by agendaViewModel.eventos.collectAsState()
    val turmas by agendaViewModel.turmas.collectAsState()
    val criandoEvento by agendaViewModel.criandoEvento.collectAsState()
    val erroCreate by agendaViewModel.erroCreate.collectAsState()
    val editandoEvento by agendaViewModel.editandoEvento.collectAsState()
    val erroEdit by agendaViewModel.erroEdit.collectAsState()
    val excluindoEvento by agendaViewModel.excluindoEvento.collectAsState()

    val schoolId = user.schoolId ?: ""
    var mostrarSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mostrarSheetEditar by rememberSaveable { mutableStateOf(false) }
    val sheetStateEditar = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var eventoParaEditar by remember { mutableStateOf<dev.fslab.comunicacao.escolar.model.Evento?>(null) }
    var eventoParaExcluir by remember { mutableStateOf<dev.fslab.comunicacao.escolar.model.Evento?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) {
            agendaViewModel.carregarEventos(schoolId)
            if (canCreate) agendaViewModel.carregarTurmas(schoolId)
        }
    }

    val diasMarcados = diasComEventos.mapNotNull { evento ->
        runCatching { LocalDate.parse(evento.dataInicio.take(10)) }.getOrNull()
    }.toSet()

    val eventosHoje = diaSelecionado?.let { dia ->
        diasComEventos.filter { evento ->
            runCatching { LocalDate.parse(evento.dataInicio.take(10)) == dia }.getOrDefault(false)
        }
    } ?: emptyList()

    if (mostrarSheet && canCreate) {
        ModalBottomSheet(
            onDismissRequest = { mostrarSheet = false },
            sheetState = sheetState,
            containerColor = colors.background,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.inputBorder)
                )
            }
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
            NovoEventoSheet(
                diaSelecionado = diaSelecionado ?: LocalDate.now(),
                turmas = turmas.map { it.id to it.nome },
                criando = criandoEvento,
                erro = erroCreate,
                onLimparErro = { agendaViewModel.limparErroCreate() },
                onConfirmar = { titulo, descricao, inicio, fim, turmasSelecionadas, allDay ->
                    val dia = diaSelecionado ?: LocalDate.now()
                    agendaViewModel.criarEvento(
                        titulo = titulo,
                        descricao = descricao,
                        inicio = if (allDay) agendaViewModel.formatarDataParaApi(dia, "00:00")
                                 else agendaViewModel.formatarDataParaApi(dia, inicio),
                        fim = if (allDay) null
                              else fim?.let { agendaViewModel.formatarDataParaApi(dia, it) },
                        turmasSelecionadas = turmasSelecionadas,
                        allDay = allDay,
                        onSuccess = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { mostrarSheet = false }
                        }
                    )
                }
            )
        }
    }

    if (eventoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { eventoParaExcluir = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.background,
            title = {
                Text(
                    text = "Excluir evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Deseja excluir \"${eventoParaExcluir?.titulo}\"? Esta ação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val evento = eventoParaExcluir ?: return@Button
                        agendaViewModel.excluirEvento(evento.id) {
                            eventoParaExcluir = null
                        }
                    },
                    enabled = !excluindoEvento,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    if (excluindoEvento) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            "Excluir",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { eventoParaExcluir = null }) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        )
    }

    if (mostrarSheetEditar && eventoParaEditar != null) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarSheetEditar = false
                eventoParaEditar = null
            },
            sheetState = sheetStateEditar,
            containerColor = colors.background,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.inputBorder)
                )
            }
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
            val evento = eventoParaEditar!!
            val diaEvento = runCatching { java.time.LocalDate.parse(evento.dataInicio.take(10)) }.getOrElse { java.time.LocalDate.now() }
            NovoEventoSheet(
                diaSelecionado = diaEvento,
                turmas = turmas.map { it.id to it.nome },
                criando = editandoEvento,
                eventoExistente = evento,
                erro = erroEdit,
                onLimparErro = { agendaViewModel.limparErroEdit() },
                onConfirmar = { titulo, descricao, inicio, fim, turmasSelecionadas, allDay ->
                    agendaViewModel.editarEvento(
                        id = evento.id,
                        titulo = titulo,
                        descricao = descricao,
                        inicio = if (allDay) agendaViewModel.formatarDataParaApi(diaEvento, "00:00")
                                 else agendaViewModel.formatarDataParaApi(diaEvento, inicio),
                        fim = if (allDay) null
                              else fim?.let { agendaViewModel.formatarDataParaApi(diaEvento, it) },
                        turmasSelecionadas = turmasSelecionadas,
                        allDay = allDay,
                        onSuccess = {
                            scope.launch { sheetStateEditar.hide() }.invokeOnCompletion {
                                mostrarSheetEditar = false
                                eventoParaEditar = null
                            }
                        }
                    )
                }
            )
        }
    }

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { AppHeader("Agenda") },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(
                    onClick = { mostrarSheet = true },
                    containerColor = colors.textPrimary,
                    contentColor = colors.background
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Novo evento")
                }
            }
        }
    ) { innerPadding ->

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Navegação do mês
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { agendaViewModel.irParaMesAnterior() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChevronLeft,
                                    contentDescription = "Mês anterior",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = mesAtual.format(
                                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
                                ).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = colors.textPrimary
                            )

                            IconButton(
                                onClick = { agendaViewModel.irParaProximoMes() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = "Próximo mês",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cabeçalho dos dias da semana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            diasSemana.forEach { dia ->
                                Text(
                                    text = dia,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textSecondary,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grade de dias
                        val primeiroDia = mesAtual.withDayOfMonth(1)
                        val deslocamento = primeiroDia.dayOfWeek.value % 7
                        val totalDias = mesAtual.lengthOfMonth()
                        val totalCelulas = deslocamento + totalDias
                        val linhas = (totalCelulas + 6) / 7
                        val hoje = LocalDate.now()

                        repeat(linhas) { semana ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                repeat(7) { coluna ->
                                    val indice = semana * 7 + coluna
                                    val dia = indice - deslocamento + 1

                                    if (dia < 1 || dia > totalDias) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    } else {
                                        val data = mesAtual.withDayOfMonth(dia)
                                        val selecionado = diaSelecionado == data
                                        val eHoje = data == hoje
                                        val temEvento = data in diasMarcados

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        selecionado -> androidx.compose.ui.graphics.Color.White
                                                        else -> colors.surface
                                                    }
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) { agendaViewModel.selecionarDia(data) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dia.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (eHoje || selecionado) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = when {
                                                        selecionado -> androidx.compose.ui.graphics.Color.Black
                                                        eHoje -> colors.textPrimary
                                                        else -> colors.textPrimary.copy(alpha = 0.45f)
                                                    }
                                                )
                                                if (temEvento) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    when {
                                                                        selecionado -> androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                                                                        eHoje -> colors.textPrimary
                                                                        else -> colors.textPrimary.copy(alpha = 0.45f)
                                                                    }
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (semana < linhas - 1) {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }

            // Seção de eventos
            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = colors.textPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else if (diaSelecionado == null) {
                item {
                    EstadoVazioAgenda(
                        mensagem = "Selecione um dia para ver os eventos"
                    )
                }
            } else if (eventosHoje.isEmpty()) {
                item {
                    EstadoVazioAgenda(
                        mensagem = "Nenhum evento neste dia"
                    )
                }
            } else {
                items(eventosHoje) { evento ->
                    EventoCard(
                        evento = evento,
                        colors = colors,
                        canEdit = canCreate,
                        onEditar = {
                            eventoParaEditar = evento
                            mostrarSheetEditar = true
                        },
                        onExcluir = { eventoParaExcluir = evento }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventoCard(
    evento: Evento,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors,
    canEdit: Boolean = false,
    onEditar: () -> Unit = {},
    onExcluir: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = buildString {
                            if (evento.allDay) {
                                append("Dia todo")
                            } else {
                                append(formatarHorario(evento.dataInicio))
                                if (!evento.dataFim.isNullOrBlank()) {
                                    append(" - ")
                                    append(formatarHorario(evento.dataFim))
                                }
                            }
                            if (evento.nomeTurmas.isNotEmpty()) {
                                append(" • ")
                                append(evento.nomeTurmas.joinToString(", "))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = colors.textSecondary
                    )
                }
            }
            if (canEdit) {
                Row(modifier = Modifier.padding(start = 4.dp)) {
                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onExcluir,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Excluir",
                            tint = androidx.compose.ui.graphics.Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (!evento.descricao.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = colors.lightGray,
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evento.descricao,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun EstadoVazioAgenda(mensagem: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.EventBusy,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatarHorario(datetime: String): String {
    return runCatching {
        val time = datetime.substring(11, 16)
        time
    }.getOrDefault(datetime.take(10))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoEventoSheet(
    diaSelecionado: LocalDate,
    turmas: List<Pair<String, String>>,
    criando: Boolean,
    eventoExistente: Evento? = null,
    erro: String? = null,
    onLimparErro: () -> Unit = {},
    onConfirmar: (titulo: String, descricao: String?, inicio: String, fim: String?, turmasSelecionadas: List<String>, allDay: Boolean) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val isEditing = eventoExistente != null

    var titulo by rememberSaveable { mutableStateOf(eventoExistente?.titulo ?: "") }
    var descricao by rememberSaveable { mutableStateOf(eventoExistente?.descricao ?: "") }
    var inicio by rememberSaveable { mutableStateOf(eventoExistente?.dataInicio?.let { if (it.length >= 16) it.substring(11, 16) else "" } ?: "") }
    var fim by rememberSaveable { mutableStateOf(eventoExistente?.dataFim?.let { if (it.length >= 16) it.substring(11, 16) else "" } ?: "") }
    var diaInteiro by rememberSaveable { mutableStateOf(eventoExistente?.allDay ?: false) }
    val turmasSelecionadas = remember { mutableStateListOf<String>().also { list -> eventoExistente?.classIds?.let { list.addAll(it) } } }
    var mostrarPickerInicio by remember { mutableStateOf(false) }
    var mostrarPickerFim by remember { mutableStateOf(false) }
    val timePickerStateInicio = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
    val timePickerStateFim = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)

    val labelData = diaSelecionado.format(
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.focusedIndicator,
        unfocusedBorderColor = colors.inputBorder,
        focusedTextColor = colors.textInput,
        unfocusedTextColor = colors.textInput,
        cursorColor = colors.focusedIndicator,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface
    )

    if (mostrarPickerInicio) {
        TimePickerDialog(
            onDismiss = { mostrarPickerInicio = false },
            onConfirm = {
                inicio = String.format("%02d:%02d", timePickerStateInicio.hour, timePickerStateInicio.minute)
                mostrarPickerInicio = false
            }
        ) { TimePicker(state = timePickerStateInicio) }
    }

    if (mostrarPickerFim) {
        TimePickerDialog(
            onDismiss = { mostrarPickerFim = false },
            onConfirm = {
                fim = String.format("%02d:%02d", timePickerStateFim.hour, timePickerStateFim.minute)
                mostrarPickerFim = false
            }
        ) { TimePicker(state = timePickerStateFim) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = if (isEditing) "Editar Evento" else "Novo Evento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = "Dia $labelData",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        // Título
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TÍTULO DO EVENTO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                placeholder = {
                    Text(
                        "Título do evento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )
        }

        // Descrição
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "DESCRIÇÃO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                placeholder = {
                    Text(
                        "Descrição (opcional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )
        }

        // Horário
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HORÁRIO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { diaInteiro = !diaInteiro }
                ) {
                    Text(
                        text = "Dia todo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diaInteiro) colors.buttonContainer else colors.textSecondary
                    )
                    Checkbox(
                        checked = diaInteiro,
                        onCheckedChange = { diaInteiro = it },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.buttonContainer,
                            uncheckedColor = colors.textSecondary,
                            checkmarkColor = colors.background
                        )
                    )
                }
            }
            if (!diaInteiro) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "INÍCIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color(0xFFE53935)
                            )
                        }
                        Box {
                            OutlinedTextField(
                                value = inicio,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = {
                                    Text("--:--", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                trailingIcon = {
                                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                }
                            )
                            Box(modifier = Modifier.matchParentSize().clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { mostrarPickerInicio = true })
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "TÉRMINO",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Box {
                            OutlinedTextField(
                                value = fim,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = {
                                    Text("--:--", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                trailingIcon = {
                                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                }
                            )
                            Box(modifier = Modifier.matchParentSize().clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { mostrarPickerFim = true })
                        }
                    }
                }
            }
        }

        // Turmas — dropdown multi-select
        if (turmas.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TURMAS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                TurmaMultiSelectDropdown(
                    turmas = turmas,
                    selecionadas = turmasSelecionadas,
                    onToggle = { id ->
                        if (id in turmasSelecionadas) turmasSelecionadas.remove(id)
                        else turmasSelecionadas.add(id)
                    },
                    onSelecionarTodas = { turmasSelecionadas.clear() }
                )
            }
        }

        if (erro != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFFFFEBEE))
                    .clickable { onLimparErro() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = erro,
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFFE53935)
                )
            }
        }

        // Botão
        Button(
            onClick = {
                if (titulo.isNotBlank() && (diaInteiro || inicio.isNotBlank())) {
                    onConfirmar(
                        titulo,
                        descricao.takeIf { it.isNotBlank() },
                        inicio,
                        fim.takeIf { it.isNotBlank() },
                        turmasSelecionadas.toList(),
                        diaInteiro
                    )
                }
            },
            enabled = titulo.isNotBlank() && (diaInteiro || inicio.isNotBlank()) && !criando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonContainer,
                contentColor = colors.buttonText,
                disabledContainerColor = colors.inputBorder,
                disabledContentColor = colors.textSecondary
            )
        ) {
            if (criando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.buttonText, strokeWidth = 2.dp)
            } else {
                Text(text = if (isEditing) "Salvar Alterações" else "Criar Evento", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        text = { content() }
    )
}

@Composable
private fun TurmaMultiSelectDropdown(
    turmas: List<Pair<String, String>>,
    selecionadas: List<String>,
    onToggle: (String) -> Unit,
    onSelecionarTodas: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val turmasFiltradas by remember(turmas, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) turmas
            else turmas.filter { it.second.contains(searchQuery, ignoreCase = true) }
        }
    }

    val displayText = when {
        selecionadas.isEmpty() -> "Todas as turmas"
        selecionadas.size == 1 -> turmas.firstOrNull { it.first == selecionadas[0] }?.second ?: "1 turma"
        selecionadas.size == 2 -> turmas.filter { it.first in selecionadas }.joinToString(", ") { it.second }
        else -> "${selecionadas.size} turmas selecionadas"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    showMenu = !showMenu
                    if (!showMenu) searchQuery = ""
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selecionadas.isEmpty()) colors.textSecondary else colors.textPrimary
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        if (showMenu) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
            ) {
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Buscar turma...", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
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
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Todas as turmas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selecionadas.isEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selecionadas.isEmpty()) colors.buttonContainer else colors.textSecondary
                                    )
                                    if (selecionadas.isEmpty()) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = colors.buttonContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = { onSelecionarTodas(); showMenu = false; searchQuery = "" }
                        )
                        HorizontalDivider(color = colors.inputBorder, thickness = 0.5.dp)
                    }

                    turmasFiltradas.forEach { (id, nome) ->
                        val selecionada = id in selecionadas
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selecionada) colors.buttonContainer else colors.textPrimary
                                    )
                                    if (selecionada) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = colors.buttonContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = { onToggle(id) }
                        )
                    }
                }
            }
        }
    }
}
