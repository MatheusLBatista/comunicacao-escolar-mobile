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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ProfessorInicioUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.ProfessorInicioViewModel

@Composable
fun ProfessorInicioScreen(
    user: User,
    onNavigateToDiario: () -> Unit = {},
    viewModel: ProfessorInicioViewModel = viewModel()
) {
    val colors = LocalComunicacaoEscolarColors.current
    val uiState by viewModel.uiState.collectAsState()
    val atividadesCount = (uiState as? ProfessorInicioUiState.Content)?.atividades?.size


    var showAtividades by remember { mutableStateOf(false) }

    BackHandler(enabled = showAtividades) { showAtividades = false }

    if (showAtividades) {
        AtividadeRecenteScreen(uiState = uiState, onBack = { showAtividades = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader("Início")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "ACESSO RÁPIDO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    QuickAccessItem(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = "Diário de Bordo",
                        subtitle = "Registrar atividades da turma",
                        onClick = onNavigateToDiario
                    )
                    QuickAccessItem(
                        icon = Icons.AutoMirrored.Outlined.ExitToApp,
                        title = "Autorizações de Saída",
                        subtitle = "Gerenciar autorizações",
                        onClick = {}
                    )
                    QuickAccessItem(
                        icon = Icons.Outlined.Group,
                        title = "Minhas Turmas",
                        subtitle = "Visualizar e gerenciar alunos",
                        onClick = {}
                    )
                    QuickAccessItem(
                        icon = Icons.Outlined.History,
                        title = "Atividade Recente",
                        subtitle = when {
                            atividadesCount != null && atividadesCount > 0 -> "$atividadesCount registros recentes"
                            else -> "Ver últimas atividades"
                        },
                        onClick = { showAtividades = true },
                        showDivider = false
                    )
                }
            }
        }
    }
}


@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    val colors = LocalComunicacaoEscolarColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(top = 14.dp, bottom = if (showDivider) 14.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.lightGray))
        }
    }
}
