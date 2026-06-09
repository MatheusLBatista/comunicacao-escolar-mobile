package dev.fslab.comunicacao.escolar.ui.screens.professor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.PickupLogItem
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AutorizacaoProfessorItem
import dev.fslab.comunicacao.escolar.ui.viewmodel.ControleSaidaProfessorViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ProfAutorizacoesState
import dev.fslab.comunicacao.escolar.ui.viewmodel.ProfRegistradasState
import dev.fslab.comunicacao.escolar.ui.viewmodel.RegistrarSaidaSheetState

@Composable
fun ControleSaidaProfessorScreen(
    onBack: () -> Unit,
    viewModel: ControleSaidaProfessorViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val autorizacoesState by viewModel.autorizacoesState.collectAsState()
    val registradasState by viewModel.registradasState.collectAsState()
    val recusadasState by viewModel.recusadasState.collectAsState()
    val confirmando by viewModel.confirmando.collectAsState()
    val recusando by viewModel.recusando.collectAsState()
    val sheetState by viewModel.sheetState.collectAsState()

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
                    text = "Controle de Saída",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            ControleSaidaTabBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            when (selectedTab) {
                0 -> AutorizacoesTabContent(
                    state = autorizacoesState,
                    confirmando = confirmando,
                    recusando = recusando,
                    onConfirmar = { viewModel.confirmarAutorizacao(it) },
                    onRecusar = { viewModel.recusarAutorizacao(it.doc.id) },
                    onRetry = { viewModel.loadAutorizacoes() }
                )
                1 -> RegistradasTabContent(
                    state = registradasState,
                    onRetry = { viewModel.loadRegistradas() }
                )
                2 -> RecusadasTabContent(
                    state = recusadasState,
                    onRetry = { viewModel.loadRecusadas() }
                )
            }
        }

        FloatingActionButton(
            onClick = { viewModel.abrirRegistrarSheet() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = colors.textPrimary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar saída")
        }
    }

    if (sheetState.isVisible) {
        RegistrarSaidaSheet(
            sheetState = sheetState,
            viewModel = viewModel
        )
    }
}

@Composable
private fun ControleSaidaTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val tabs = listOf("Autorizações", "Registradas", "Recusadas")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                val selected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) colors.textPrimary else colors.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(colors.textPrimary)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = colors.inputBorder, thickness = 1.dp)
    }
}

@Composable
private fun AutorizacoesTabContent(
    state: ProfAutorizacoesState,
    confirmando: String?,
    recusando: String?,
    onConfirmar: (AutorizacaoProfessorItem) -> Unit,
    onRecusar: (AutorizacaoProfessorItem) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is ProfAutorizacoesState.Loading -> TabLoadingContent()
        is ProfAutorizacoesState.Error -> TabErrorContent(state.message, onRetry)
        is ProfAutorizacoesState.Empty -> TabEmptyContent("Nenhuma autorização pendente")
        is ProfAutorizacoesState.Content -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            items(state.items, key = { it.doc.id }) { item ->
                AutorizacaoProfessorCard(
                    item = item,
                    confirmando = confirmando == item.doc.id,
                    recusando = recusando == item.doc.id,
                    onConfirmar = { onConfirmar(item) },
                    onRecusar = { onRecusar(item) }
                )
            }
        }
    }
}

@Composable
private fun RegistradasTabContent(
    state: ProfRegistradasState,
    onRetry: () -> Unit
) {
    when (state) {
        is ProfRegistradasState.Loading -> TabLoadingContent()
        is ProfRegistradasState.Error -> TabErrorContent(state.message, onRetry)
        is ProfRegistradasState.Empty -> TabEmptyContent("Nenhuma saída registrada")
        is ProfRegistradasState.Content -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(state.items, key = { it.id }) { item ->
                PickupLogCard(item = item)
            }
        }
    }
}

@Composable
private fun RecusadasTabContent(
    state: ProfAutorizacoesState,
    onRetry: () -> Unit
) {
    when (state) {
        is ProfAutorizacoesState.Loading -> TabLoadingContent()
        is ProfAutorizacoesState.Error -> TabErrorContent(state.message, onRetry)
        is ProfAutorizacoesState.Empty -> TabEmptyContent("Nenhuma autorização recusada")
        is ProfAutorizacoesState.Content -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            items(state.items, key = { it.doc.id }) { item ->
                RecusadaCard(item = item)
            }
        }
    }
}

@Composable
private fun AutorizacaoProfessorCard(
    item: AutorizacaoProfessorItem,
    confirmando: Boolean,
    recusando: Boolean,
    onConfirmar: () -> Unit,
    onRecusar: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    val isActing = confirmando || recusando

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
                if (item.studentAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.studentAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.studentName,
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
                    text = item.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (item.responsavelName.isNotBlank()) {
                    Text(
                        text = "Resp: ${item.responsavelName}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.lightGray)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val pessoa = buildString {
                append(item.autorizadoPorName)
                if (item.autorizadoPorRelacao.isNotBlank()) append(" · ${item.autorizadoPorRelacao}")
            }
            InfoRow(label = "Pessoa autorizada", value = pessoa)
            InfoRow(label = "Horário previsto", value = item.horarioPrevisto)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, colors.inputBorder, RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isActing,
                        onClick = onRecusar
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (recusando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.textSecondary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Recusar",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isActing) colors.textPrimary else colors.mediumGray)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isActing,
                        onClick = onConfirmar
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (confirmando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Confirmar",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecusadaCard(item: AutorizacaoProfessorItem) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

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
                if (item.studentAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.studentAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.studentName,
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
                    text = item.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (item.responsavelName.isNotBlank()) {
                    Text(
                        text = "Resp: ${item.responsavelName}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.lightGray)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val pessoa = buildString {
                append(item.autorizadoPorName)
                if (item.autorizadoPorRelacao.isNotBlank()) append(" · ${item.autorizadoPorRelacao}")
            }
            InfoRow(label = "Pessoa autorizada", value = pessoa)
            InfoRow(label = "Horário previsto", value = item.horarioPrevisto)
        }
    }
}

@Composable
private fun PickupLogCard(item: PickupLogItem) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.lightGray),
                contentAlignment = Alignment.Center
            ) {
                if (item.studentAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.studentAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.studentName,
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

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.studentName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    if (item.time.isNotBlank()) {
                        Text(text = item.time, fontSize = 12.sp, color = colors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = buildString {
                    append(item.pickedUpName)
                    if (item.relationship.isNotBlank()) append(" · ${item.relationship}")
                }
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, fontSize = 13.sp, color = colors.textSecondary)
                }
                if (item.isManual) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Registrado manualmente pelo professor",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
        HorizontalDivider(color = colors.inputBorder, thickness = 1.dp)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, fontSize = 11.sp, letterSpacing = 0.3.sp, color = colors.textSecondary)
        Text(text = value.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
}

@Composable
private fun TabLoadingContent() {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Carregando...", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
    }
}

@Composable
private fun TabErrorContent(message: String, onRetry: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}

@Composable
private fun TabEmptyContent(message: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrarSaidaSheet(
    sheetState: RegistrarSaidaSheetState,
    viewModel: ControleSaidaProfessorViewModel
) {
    val colors = LocalComunicacaoEscolarColors.current
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(enabled = sheetState.step == 2) {
        viewModel.goBackToStep1()
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.fecharRegistrarSheet() },
        sheetState = modalSheetState,
        containerColor = colors.background,
        dragHandle = null
    ) {
        AnimatedContent(
            targetState = sheetState.step,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(tween(280)) { it } + fadeIn(tween(200)) togetherWith
                            slideOutHorizontally(tween(280)) { -it } + fadeOut(tween(200))
                } else {
                    slideInHorizontally(tween(280)) { -it } + fadeIn(tween(200)) togetherWith
                            slideOutHorizontally(tween(280)) { it } + fadeOut(tween(200))
                }
            },
            label = "sheet_step"
        ) { step ->
            if (step == 1) {
                SheetStep1(sheetState = sheetState, viewModel = viewModel)
            } else {
                SheetStep2(sheetState = sheetState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SheetStep1(
    sheetState: RegistrarSaidaSheetState,
    viewModel: ControleSaidaProfessorViewModel
) {
    val colors = LocalComunicacaoEscolarColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = "Registrar Saída",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Saída manual sem pré-autorização",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
            IconButton(onClick = { viewModel.fecharRegistrarSheet() }) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SELECIONE A TURMA",
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (sheetState.turmasLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = colors.primary)
            }
        } else if (sheetState.turmas.isEmpty()) {
            Text(
                text = "Nenhuma turma encontrada.",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        } else {
            Column {
                sheetState.turmas.forEachIndexed { index, turma ->
                    val count = sheetState.turmaStudentCounts[turma.id] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.selectTurma(turma) }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = turma.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$count alunos",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < sheetState.turmas.lastIndex) {
                        HorizontalDivider(color = colors.inputBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetStep2(
    sheetState: RegistrarSaidaSheetState,
    viewModel: ControleSaidaProfessorViewModel
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    var quemBuscou by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var relacao by remember { mutableStateOf("") }

    val classId = sheetState.selectedTurma?.id
    val studentsForClass = remember(classId, sheetState.allStudents) {
        if (classId == null) return@remember emptyList<ApiSchoolUser>()
        sheetState.allStudents
            .filter { user -> user.memberships.any { m -> m.role == "student" && m.classId == classId } }
            .sortedBy { it.fullName }
    }
    val filteredStudents = remember(studentsForClass, sheetState.searchQuery) {
        if (sheetState.searchQuery.isBlank()) studentsForClass
        else studentsForClass.filter { it.fullName.contains(sheetState.searchQuery, ignoreCase = true) }
    }

    val isValid = sheetState.selectedStudent != null && quemBuscou.isNotBlank() && documento.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.goBackToStep1() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = colors.textPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Registrar Saída",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Saída manual sem pré-autorização",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ALUNO DA ${sheetState.selectedTurma?.name?.uppercase() ?: ""}",
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = sheetState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = {
                Text("Buscar aluno...", fontSize = 13.sp, color = colors.textSecondary)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.focusedIndicator,
                unfocusedBorderColor = colors.inputBorder,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                cursorColor = colors.focusedIndicator
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredStudents.isEmpty()) {
            Text(
                text = if (sheetState.searchQuery.isBlank()) "Nenhum aluno nesta turma."
                       else "Nenhum aluno encontrado.",
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                filteredStudents.forEach { student ->
                    val isSelected = sheetState.selectedStudent?.id == student.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colors.textPrimary else colors.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else colors.inputBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.selectStudent(student) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.15f) else colors.lightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (student.avatarUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(student.avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = student.fullName,
                                    modifier = Modifier.size(32.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else colors.iconGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = student.fullName,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else colors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "QUEM BUSCOU",
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = quemBuscou,
            onValueChange = { quemBuscou = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.focusedIndicator,
                unfocusedBorderColor = colors.inputBorder,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                cursorColor = colors.focusedIndicator,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "DOCUMENTO (CPF)",
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = documento,
            onValueChange = { input -> documento = input.filter { it.isDigit() }.take(11) },
            placeholder = { Text("000.000.000-00", fontSize = 13.sp, color = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = CpfVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.focusedIndicator,
                unfocusedBorderColor = colors.inputBorder,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                cursorColor = colors.focusedIndicator,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "RELAÇÃO",
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = relacao,
            onValueChange = { relacao = it },
            placeholder = { Text("Ex: Tia, Avó...", fontSize = 13.sp, color = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.focusedIndicator,
                unfocusedBorderColor = colors.inputBorder,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                cursorColor = colors.focusedIndicator,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        if (sheetState.erro != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = sheetState.erro, fontSize = 13.sp, color = colors.errorText)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isValid && !sheetState.registrando) colors.textPrimary else colors.mediumGray)
                .clickable(
                    enabled = isValid && !sheetState.registrando,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.registrarSaidaManual(quemBuscou, documento, relacao) }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (sheetState.registrando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(
                    text = "Registrar Saída",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

private class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 3 || i == 6) append('.')
                if (i == 9) append('-')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 6 -> offset + 1
                offset <= 9 -> offset + 2
                else -> offset + 3
            }.coerceAtMost(formatted.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 7 -> offset - 1
                offset <= 11 -> offset - 2
                else -> offset - 3
            }.coerceAtMost(digits.length)
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
