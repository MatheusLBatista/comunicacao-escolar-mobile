package dev.fslab.comunicacao.escolar.ui.screens.professor

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ClassDiarioStatus
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDateGroup
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoListUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoListViewModel

@Composable
fun DiarioDeBordoListScreen(
    onBack: () -> Unit,
    onOpenDiario: (classId: String, className: String, dateMs: Long) -> Unit,
    abaAtual: AbaListaDiario = AbaListaDiario.PENDENTES,
    onAbaChange: (AbaListaDiario) -> Unit = {},
    viewModel: DiarioDeBordoListViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader("Diário de Bordo", onBack = onBack)

        when (val state = uiState) {
            is DiarioDeBordoListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = colors.primary,
                        strokeWidth = 2.5.dp
                    )
                }
            }

            is DiarioDeBordoListUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.load() }) {
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

            is DiarioDeBordoListUiState.Content -> {
                val totalPendentes = state.pendentes.size
                val totalConcluidas = state.concluidasPorData.sumOf { it.turmas.size }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        AbaListaDiario.PENDENTES to "Pendentes ($totalPendentes)",
                        AbaListaDiario.CONCLUIDAS to "Concluídas ($totalConcluidas)"
                    ).forEach { (aba, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (abaAtual == aba) colors.buttonContainer else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onAbaChange(aba) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (abaAtual == aba) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (abaAtual == aba) colors.buttonText else colors.textSecondary
                            )
                        }
                    }
                }

                when (abaAtual) {
                    AbaListaDiario.PENDENTES -> {
                        if (state.pendentes.isEmpty()) {
                            EmptyState(
                                icon = Icons.Outlined.CheckCircle,
                                message = "Todos os diários foram concluídos!"
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.pendentes, key = { it.classId }) { status ->
                                    TurmaCard(
                                        status = status,
                                        onClick = { onOpenDiario(status.classId, status.className, 0L) }
                                    )
                                }
                            }
                        }
                    }
                    AbaListaDiario.CONCLUIDAS -> {
                        if (state.concluidasPorData.isEmpty()) {
                            EmptyState(
                                icon = Icons.Outlined.Pending,
                                message = "Nenhum diário concluído ainda."
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                state.concluidasPorData.forEach { group ->
                                    item(key = "header_${group.dateLabel}") {
                                        Text(
                                            text = group.dateLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(
                                        group.turmas,
                                        key = { "${group.dateLabel}_${it.classId}" }
                                    ) { status ->
                                        TurmaCard(
                                            status = status,
                                            onClick = { onOpenDiario(status.classId, status.className, group.dateMs) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AbaListaDiario { PENDENTES, CONCLUIDAS }

@Composable
private fun EmptyState(icon: ImageVector, message: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TurmaCard(
    status: ClassDiarioStatus,
    onClick: () -> Unit
) {
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
                text = status.className,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${status.studentCount} aluno${if (status.studentCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary
                )
                if (status.logCount > 0) {
                    Text(text = "·", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Text(
                        text = "${status.logCount}/${status.studentCount} registrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
