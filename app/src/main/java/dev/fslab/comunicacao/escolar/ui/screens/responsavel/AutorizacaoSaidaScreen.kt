package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import android.app.DatePickerDialog
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudent
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaida
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AutorizacaoSaidaUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AutorizacaoSaidaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AutorizacaoSaidaScreen(
    onBack: () -> Unit,
    viewModel: AutorizacaoSaidaViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    val cancelando by viewModel.cancelando.collectAsState()
    val showSheet by viewModel.showNovaAutorizacaoSheet.collectAsState()
    val criando by viewModel.criando.collectAsState()
    val criarErro by viewModel.criarErro.collectAsState()
    val qrCodeId by viewModel.qrCodeId.collectAsState()
    val alunos by viewModel.alunos.collectAsState()
    val alunoFiltro by viewModel.alunoFiltro.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colors.textPrimary
                    )
                }
                Text(
                    text = "Autorizações de Saída",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            if (alunos.size > 1) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FiltroChip(
                            label = "Todos",
                            selecionado = alunoFiltro == null,
                            onClick = { viewModel.filtrarPorAluno(null) },
                            colors = colors
                        )
                    }
                    items(alunos) { aluno ->
                        FiltroChip(
                            label = aluno.fullName.trim().split(" ").firstOrNull() ?: aluno.fullName,
                            selecionado = alunoFiltro == aluno.id,
                            onClick = { viewModel.filtrarPorAluno(aluno.id) },
                            colors = colors
                        )
                    }
                }
            }

            when (val state = uiState) {
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
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(state.autorizacoes, key = { it.id }) { autorizacao ->
                            AutorizacaoCard(
                                autorizacao = autorizacao,
                                cancelando = cancelando == autorizacao.id,
                                onCancelar = { viewModel.cancelarAutorizacao(autorizacao.id) },
                                onVerQrCode = { viewModel.mostrarQrCode(autorizacao.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.abrirNovaAutorizacao() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = colors.textPrimary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nova autorização")
        }
    }

    if (showSheet) {
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

    if (qrCodeId != null) {
        QrCodeDialog(
            authorizationId = qrCodeId!!,
            onDismiss = { viewModel.dispensarQrCode() }
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
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Nome
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("QUEM VAI BUSCAR", fontSize = 11.sp, letterSpacing = 0.8.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(colors)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Documento (CPF)
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
                label = { Text("DOCUMENTO (CPF)", fontSize = 11.sp, letterSpacing = 0.8.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors(colors)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Relação
            OutlinedTextField(
                value = relacao,
                onValueChange = { relacao = it },
                label = { Text("RELAÇÃO", fontSize = 11.sp, letterSpacing = 0.8.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(colors)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Datas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = validFromText,
                        onValueChange = {},
                        label = { Text("DE", fontSize = 11.sp, letterSpacing = 0.8.sp) },
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

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = validUntilText,
                        onValueChange = {},
                        label = { Text("ATÉ", fontSize = 11.sp, letterSpacing = 0.8.sp) },
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

            if (erro != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = erro, fontSize = 13.sp, color = colors.errorText)
            }

            Spacer(modifier = Modifier.height(20.dp))

            val isValid = alunoSelecionado != null && nome.isNotBlank() &&
                    documento.isNotBlank() && relacao.isNotBlank() && validUntilMs > validFromMs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isValid && !criando) colors.buttonContainer else colors.lightGray)
                    .clickable(
                        enabled = isValid && !criando,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { alunoSelecionado?.id?.let { onCriar(nome, documento, relacao, validFromMs, validUntilMs, it) } }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isValid) colors.buttonText else colors.textSecondary
                    )
                }
            }
        }
    }
}

private fun generateQrBitmap(content: String, sizePx: Int = 600): Bitmap {
    val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
private fun QrCodeDialog(
    authorizationId: String,
    onDismiss: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val qrBitmap = remember(authorizationId) { generateQrBitmap(authorizationId) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QR Code de Autorização",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Apresente este QR Code na portaria para autorizar a saída",
                fontSize = 13.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.buttonContainer)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Entendido",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.buttonText
                )
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
        unfocusedContainerColor = colors.surface,
        focusedLabelColor = colors.textSecondary,
        unfocusedLabelColor = colors.textSecondary
    )

@Composable
private fun AutorizacaoCard(
    autorizacao: AutorizacaoSaida,
    cancelando: Boolean,
    onCancelar: () -> Unit,
    onVerQrCode: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Cancelar autorização", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Tem certeza que deseja cancelar esta autorização? Ela não poderá ser usada após o cancelamento.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onCancelar() }) {
                    Text("Confirmar", color = colors.errorText, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Manter", color = colors.textSecondary)
                }
            }
        )
    }

    val isActive = autorizacao.status == "Aguardando saída"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
            .background(colors.background)
            .then(
                if (isActive) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onVerQrCode
                ) else Modifier
            )
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

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = autorizacao.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = autorizacao.status,
                    fontSize = 13.sp,
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
            InfoRow(label = "Válido até", value = autorizacao.validAte)
        }

        Spacer(modifier = Modifier.height(12.dp))

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
            .background(if (selecionado) colors.textPrimary else colors.background)
            .border(1.dp, if (selecionado) colors.textPrimary else colors.inputBorder, RoundedCornerShape(20.dp))
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
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selecionado) Color.White else colors.textSecondary
        )
    }
}