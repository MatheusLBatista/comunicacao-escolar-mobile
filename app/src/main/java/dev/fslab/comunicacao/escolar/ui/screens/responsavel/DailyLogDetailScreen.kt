package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.CreateConversationRequest
import dev.fslab.comunicacao.escolar.model.DailyLog
import dev.fslab.comunicacao.escolar.model.DailyLogDetailEntry
import dev.fslab.comunicacao.escolar.model.SendMessageRequest
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import kotlinx.coroutines.launch

@Composable
fun DailyLogDetailScreen(
    log: DailyLog,
    user: User,
    onBack: () -> Unit,
    onOpenConversation: (conversaId: String, titulo: String, avatarUrl: String?) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current

    val observation = log.observation.trim().takeIf { it.isNotBlank() }
        ?: if (log.entries.isEmpty()) log.description else null
    val dateTimeLabel = buildDateTimeLabel(log.date, log.time)
    val teacherName = log.teacherName.ifBlank { "Professor(a)" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader(title = "Atividades", onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                DailyLogMessageCard(
                    teacherName = teacherName,
                    dateTimeLabel = dateTimeLabel,
                    observation = observation,
                    teacherAvatarUrl = log.teacherAvatarUrl,
                    className = log.className
                )
            }

            if (log.entries.isNotEmpty()) {
                item {
                    DailyLogEntriesCard(entries = log.entries)
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        DailyLogMessageComposer(
            log = log,
            schoolId = user.schoolId.orEmpty(),
            onOpenConversation = onOpenConversation
        )
    }
}

@Composable
private fun DailyLogMessageCard(
    teacherName: String,
    dateTimeLabel: String,
    observation: String?,
    teacherAvatarUrl: String? = null,
    className: String = ""
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.background,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.lightGray),
                contentAlignment = Alignment.Center
            ) {
                if (teacherAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(teacherAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = teacherName,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.iconGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prof. $teacherName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                val metaLabel = listOfNotNull(
                    dateTimeLabel.takeIf { it.isNotBlank() },
                    className.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                if (metaLabel.isNotBlank()) {
                    Text(
                        text = metaLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
                if (observation != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = observation,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyLogEntriesCard(entries: List<DailyLogDetailEntry>) {
    val colors = LocalComunicacaoEscolarColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
            }
            if (index != entries.lastIndex) {
                HorizontalDivider(color = colors.inputBorder, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun DailyLogMessageComposer(
    log: DailyLog,
    schoolId: String,
    onOpenConversation: (conversaId: String, titulo: String, avatarUrl: String?) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val canSend = !isSending && message.isNotBlank() &&
        (log.conversationId.isNotBlank() || log.teacherId.isNotBlank()) &&
        schoolId.isNotBlank()

    fun sendMessage() {
        if (!canSend) return
        scope.launch {
            isSending = true
            try {
                val token = TokenManager.getAccessToken() ?: return@launch
                val auth = "Bearer $token"

                val conversaId = if (log.conversationId.isNotBlank()) {
                    log.conversationId
                } else {
                    val resp = RetrofitClient.conversaApi.findOrCreate(
                        schoolId,
                        CreateConversationRequest(participantId = log.teacherId)
                    )
                    resp.data?.id ?: return@launch
                }

                val encodedText = "[act_ref:${log.id}|${log.date}|${log.childName}]\n${message.trim()}"
                RetrofitClient.conversaApi.send(
                    conversaId,
                    SendMessageRequest(text = encodedText)
                )

                message = ""
                val titulo = "Prof. ${log.teacherName.ifBlank { "Professor(a)" }}"
                onOpenConversation(conversaId, titulo, log.teacherAvatarUrl)
            } finally {
                isSending = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.lightGray)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = {
                    Text(
                        "Escreva uma mensagem...",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                shape = RoundedCornerShape(12.dp),
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

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (canSend) colors.buttonContainer else colors.surface),
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.buttonText,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = ::sendMessage,
                        modifier = Modifier.size(52.dp),
                        enabled = canSend
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Enviar mensagem",
                            tint = if (canSend) colors.buttonText else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun buildDateTimeLabel(date: String, time: String): String {
    return when {
        date.isBlank() -> time
        time.isBlank() -> date
        else -> "$date • $time"
    }
}
