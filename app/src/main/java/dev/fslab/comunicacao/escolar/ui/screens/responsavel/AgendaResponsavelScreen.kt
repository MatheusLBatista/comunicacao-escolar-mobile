package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.Evento
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AgendaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val diasSemana = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")

@Composable
fun AgendaResponsavelScreen(
    user: User,
    accessToken: String,
    agendaViewModel: AgendaViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val mesAtual by agendaViewModel.mesAtual.collectAsState()
    val diaSelecionado by agendaViewModel.diaSelecionado.collectAsState()
    val loading by agendaViewModel.loading.collectAsState()
    val diasComEventos by agendaViewModel.eventos.collectAsState()

    val schoolId = user.schoolId ?: ""

    LaunchedEffect(schoolId) {
        if (schoolId.isNotBlank()) {
            agendaViewModel.carregarEventos(schoolId)
        }
    }

    val diasMarcados = remember(diasComEventos) {
        agendaViewModel.diasComEventos()
    }

    val eventosHoje = remember(diaSelecionado, diasComEventos) {
        diaSelecionado?.let { agendaViewModel.eventosNoDia(it) } ?: emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

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
                                                                    if (selecionado) androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                                                                    else colors.primary
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
                            color = colors.primary,
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
                    EventoCard(evento = evento, colors = colors)
                }
            }
        }
}

@Composable
private fun EventoCard(
    evento: Evento,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = evento.titulo,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = buildString {
                    append(formatarHorario(evento.dataInicio))
                    if (!evento.dataFim.isNullOrBlank()) {
                        append(" - ")
                        append(formatarHorario(evento.dataFim))
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
