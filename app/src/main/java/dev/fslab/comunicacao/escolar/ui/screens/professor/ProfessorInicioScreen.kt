package dev.fslab.comunicacao.escolar.ui.screens.professor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors

data class ProfessorActivityItem(
    val actorName: String,
    val action: String,
    val studentName: String,
    val dateTime: String,
    val timeAgo: String
)

private val mockActivityItems = listOf(
    ProfessorActivityItem(
        actorName = "Roberto Alves",
        action = "criou uma autorização de saída",
        studentName = "Fernanda Alves",
        dateTime = "hoje às 15h",
        timeAgo = "1h atrás"
    ),
    ProfessorActivityItem(
        actorName = "Patrícia Gomes",
        action = "leu o diário de",
        studentName = "Ruan Gomes",
        dateTime = "14/04/2026",
        timeAgo = "2h atrás"
    ),
    ProfessorActivityItem(
        actorName = "Carlos Andrade",
        action = "criou uma autorização de saída",
        studentName = "Leo Andrade",
        dateTime = "hoje às 14h30",
        timeAgo = "3h atrás"
    )
)

@Composable
fun ProfessorInicioScreen(user: User) {
    val colors = LocalComunicacaoEscolarColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Text(
                text = "Início",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 24.dp)
            )
        }

        item {
            AcessoRapidoSection()
            Spacer(modifier = Modifier.height(28.dp))
        }

        item {
            Text(
                text = "ATIVIDADE RECENTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(mockActivityItems) { item ->
            AtividadeRecenteItem(item = item)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun AcessoRapidoSection() {
    val colors = LocalComunicacaoEscolarColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ACESSO RÁPIDO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAccessCard(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                title = "Autorizações de Saída",
                subtitle = "2 aguardando liberação",
                onClick = {}
            )
            QuickAccessCard(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = "Diário de Bordo",
                subtitle = "1 pendente para hoje",
                onClick = {}
            )
            QuickAccessCard(
                icon = Icons.Outlined.Group,
                title = "Minhas Turmas",
                subtitle = "Visualizar e gerenciar alunos",
                onClick = {}
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.iconGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
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
private fun AtividadeRecenteItem(item: ProfessorActivityItem) {
    val colors = LocalComunicacaoEscolarColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.lightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = colors.iconGray,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.textPrimary)) {
                        append(item.actorName)
                    }
                    append(" ")
                    withStyle(SpanStyle(color = colors.textPrimary)) {
                        append(item.action)
                    }
                },
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${item.studentName} · ${item.dateTime}",
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.timeAgo,
                fontSize = 12.sp,
                color = colors.mediumGray
            )
        }
    }
}