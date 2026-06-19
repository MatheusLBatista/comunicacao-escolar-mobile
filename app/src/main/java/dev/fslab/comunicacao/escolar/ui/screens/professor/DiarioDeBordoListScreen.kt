package dev.fslab.comunicacao.escolar.ui.screens.professor

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ClassDiarioStatus
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoListUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.DiarioDeBordoListViewModel

@Composable
fun DiarioDeBordoListScreen(
    onBack: () -> Unit,
    onOpenDiario: (classId: String, className: String) -> Unit,
    viewModel: DiarioDeBordoListViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    var abaAtual by rememberSaveable { mutableStateOf(AbaListaDiario.PENDENTES) }

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
                val listaAtual = if (abaAtual == AbaListaDiario.PENDENTES) state.pendentes else state.concluidas
                val totalPendentes = state.pendentes.size
                val totalConcluidas = state.concluidas.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AbaChip(
                        label = "Pendentes",
                        count = totalPendentes,
                        selecionada = abaAtual == AbaListaDiario.PENDENTES,
                        onClick = { abaAtual = AbaListaDiario.PENDENTES }
                    )
                    AbaChip(
                        label = "Concluídas",
                        count = totalConcluidas,
                        selecionada = abaAtual == AbaListaDiario.CONCLUIDAS,
                        onClick = { abaAtual = AbaListaDiario.CONCLUIDAS }
                    )
                }

                if (listaAtual.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (abaAtual == AbaListaDiario.PENDENTES)
                                    Icons.Outlined.CheckCircle else Icons.Outlined.Pending,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (abaAtual == AbaListaDiario.PENDENTES)
                                    "Todos os diários foram concluídos!" else "Nenhum diário concluído hoje.",
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(listaAtual, key = { it.classId }) { status ->
                            TurmaCard(
                                status = status,
                                onClick = { onOpenDiario(status.classId, status.className) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class AbaListaDiario { PENDENTES, CONCLUIDAS }

@Composable
private fun AbaChip(
    label: String,
    count: Int,
    selecionada: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selecionada) colors.surface else colors.background)
            .border(
                1.dp,
                if (selecionada) colors.focusedIndicator else colors.inputBorder,
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
            text = "$label ($count)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selecionada) colors.textPrimary else colors.textSecondary
        )
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
                    color = colors.textSecondary
                )
                if (status.logCount > 0) {
                    Text(text = "·", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Text(
                        text = "${status.logCount}/${status.studentCount} registrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.isDone) colors.primary else colors.textSecondary
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
