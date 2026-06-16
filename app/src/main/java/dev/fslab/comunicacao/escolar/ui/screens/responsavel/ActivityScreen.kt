package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.DailyLog
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ConversaViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.DailyLogsUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.DailyLogsViewModel
import dev.fslab.comunicacao.escolar.ui.screens.conversas.ConversaDetailScreen

private data class ConversaAtividade(
    val conversaId: String,
    val titulo: String,
    val avatarUrl: String?,
    val log: DailyLog
)

@Composable
fun ActivityScreen(
    user: User,
    pendingLogId: String? = null,
    onPendingLogConsumed: () -> Unit = {},
    viewModel: DailyLogsViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedLog by remember { mutableStateOf<DailyLog?>(null) }
    var showAutorizacoes by remember { mutableStateOf(false) }
    var conversaAberta by remember { mutableStateOf<ConversaAtividade?>(null) }

    LaunchedEffect(pendingLogId, uiState) {
        if (pendingLogId != null && uiState is DailyLogsUiState.Content) {
            val log = (uiState as DailyLogsUiState.Content).groups
                .flatMap { it.logs }
                .firstOrNull { it.id == pendingLogId }
            if (log != null) {
                selectedLog = log
                onPendingLogConsumed()
            }
        }
    }

    BackHandler(enabled = conversaAberta != null) { conversaAberta = null }
    BackHandler(enabled = showAutorizacoes) { showAutorizacoes = false }
    BackHandler(enabled = selectedLog != null) { selectedLog = null }

    if (conversaAberta != null) {
        val ca = conversaAberta!!
        AtividadeConversaScreen(
            conversaId = ca.conversaId,
            titulo = ca.titulo,
            avatarUrl = ca.avatarUrl,
            log = ca.log,
            user = user,
            onBack = { conversaAberta = null }
        )
        return
    }

    if (showAutorizacoes) {
        AutorizacaoSaidaScreen(onBack = { showAutorizacoes = false })
        return
    }

    if (selectedLog != null) {
        DailyLogDetailScreen(
            log = selectedLog!!,
            user = user,
            onBack = { selectedLog = null },
            onOpenConversation = { conversaId, titulo, avatarUrl ->
                conversaAberta = ConversaAtividade(conversaId, titulo, avatarUrl, selectedLog!!)
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader("Atividades")

        when (uiState) {
            is DailyLogsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = colors.textSecondary,
                        strokeWidth = 2.dp
                    )
                }
            }

            is DailyLogsUiState.Error -> {
                val message = (uiState as DailyLogsUiState.Error).message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadDailyLogs() }) {
                        Text(text = "Tentar novamente")
                    }
                }
            }

            is DailyLogsUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sem atividades por enquanto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            is DailyLogsUiState.Content -> {
                val groups = (uiState as DailyLogsUiState.Content).groups

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        QuickAccessSection(onVerAutorizacoes = { showAutorizacoes = true })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    groups.forEach { group ->
                        item {
                            Text(
                                text = group.dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(group.logs) { log ->
                            DailyLogCard(
                                log = log,
                                onClick = { selectedLog = log }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AtividadeConversaScreen(
    conversaId: String,
    titulo: String,
    avatarUrl: String?,
    log: DailyLog,
    user: User,
    onBack: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val conversaViewModel: ConversaViewModel = viewModel(key = "atividade_$conversaId")

    ConversaDetailScreen(
        conversaId = conversaId,
        titulo = titulo,
        avatarUrl = avatarUrl,
        user = user,
        conversaViewModel = conversaViewModel,
        onBack = onBack,
        onActivityRefTapped = { onBack() }
    )
}

@Composable
private fun QuickAccessSection(onVerAutorizacoes: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ACESSO RÁPIDO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        QuickAccessCard(
            title = "Autorizações de Saída",
            subtitle = "1 aguardando liberação",
            onClick = onVerAutorizacoes
        )
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isPressed) colors.lightGray else colors.background,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.lightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = null,
                    tint = colors.iconGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun DailyLogCard(log: DailyLog, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.lightGray),
            contentAlignment = Alignment.Center
        ) {
            if (log.studentAvatarUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(log.studentAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = log.childName,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.childName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            if (log.time.isNotBlank()) {
                Text(
                    text = log.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
