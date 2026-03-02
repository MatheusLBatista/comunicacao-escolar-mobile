package dev.fslab.comunicacao.escolar.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.model.DailyLog
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors

@Composable
fun DailyLogCard(log: DailyLog, isSelected: Boolean = false) {
    val colors = LocalComunicacaoEscolarColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) colors.dailyLogsCardSelected else colors.dailyLogsCardDefault,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circular
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.dailyLogsAvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar de ${log.childName}",
                    tint = colors.dailyLogsAvatarIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.childName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.dailyLogsChildName
                    )
                    Text(
                        text = log.time,
                        fontSize = 12.sp,
                        color = colors.dailyLogsTimeText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.description,
                    fontSize = 14.sp,
                    color = colors.dailyLogsDescription,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = colors.dailyLogsDivider, thickness = 0.5.dp)
    }
}

@Composable
fun DailyLogsScreen() {
    val colors = LocalComunicacaoEscolarColors.current

    val logs = mapOf(
        "24/03/2026" to listOf(
            DailyLog(1, "Leo", "2:30 PM", "Check-in realizado com sucesso. Comeu bem no almoço...", "24/03/2026"),
            DailyLog(2, "Neo", "10:15 AM", "Café da manhã: maça, salgadinho...", "24/03/2026")
        ),
        "23/03/2026" to listOf(
            DailyLog(3, "Neo", "4:00 PM", "O tempo da soneca foi mais curto do que o habitual, mas ele brincou...", "23/03/2026"),
            DailyLog(4, "Leo", "2:30 PM", "Estava bastante agitado hoje, então comeu e dormiu muito bem...", "23/03/2026")
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.dailyLogsBackground)
    ) {
        // Título da tela
        Text(
            text = "Activity Logs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.dailyLogsTitleText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 40.dp)
        )

        // Lista com scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            logs.forEach { (date, logsForDate) ->
                // Header da data
                Text(
                    text = date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.dailyLogsDateText,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                // Cards do dia
                logsForDate.forEachIndexed { index, log ->
                    DailyLogCard(
                        log = log,
                        isSelected = index == 0 && date == "24/03/2026"
                    )

                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.dailyLogsNavBarBg)
                .padding(vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "Mais opções",
            tint = colors.dailyLogsNavIconInactive,
            modifier = Modifier
                .size(24.dp)
        )
        }

        BottomNavigationBar()
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean
) {
    val colors = LocalComunicacaoEscolarColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) colors.dailyLogsNavIconActive else colors.dailyLogsNavIconInactive,
            modifier = Modifier.size(24.dp)
        )
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(
                    color = if (isActive) colors.dailyLogsNavIconActive else androidx.compose.ui.graphics.Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
fun BottomNavigationBar(activeIndex: Int = 0) {
    val colors = LocalComunicacaoEscolarColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.dailyLogsNavBarBg)
            .padding(vertical = 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(Icons.Default.MailOutline, "Mensagens", activeIndex == 0)
        NavBarItem(Icons.Default.ChatBubbleOutline, "Logs", activeIndex == 1)
        NavBarItem(Icons.Default.FavoriteBorder, "Favoritos", activeIndex == 2)
        NavBarItem(Icons.Default.CalendarMonth, "Calendário", activeIndex == 3)
        NavBarItem(Icons.Default.PersonOutline, "Perfil", activeIndex == 4)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DailyLogsScreenPreview() {
    ComunicacaoEscolarTheme {
        DailyLogsScreen()
    }
}