package dev.fslab.comunicacao.escolar.ui.screens.conversas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.Message
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.model.formatMessageDateSeparator
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ConversaViewModel


@Composable
fun ConversaDetailScreen(
    conversaId: String,
    titulo: String,
    avatarUrl: String? = null,
    user: User,
    conversaViewModel: ConversaViewModel,
    onBack: () -> Unit,
    topContent: (@Composable () -> Unit)? = null,
    onActivityRefTapped: ((logId: String) -> Unit)? = null
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    val messages by conversaViewModel.messages.collectAsState()
    val isLoading by conversaViewModel.messagesLoading.collectAsState()
    val isSending by conversaViewModel.isSending.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversaId) {
        conversaViewModel.openConversation(conversaId)
        conversaViewModel.loadMessages(conversaId, user.id)
        conversaViewModel.markRead(conversaId)
        // Remove a notificação dessa conversa da bandeja (id usado no service é o hashCode)
        NotificationManagerCompat.from(context).cancel(conversaId.hashCode())
    }

    DisposableEffect(conversaId) {
        onDispose { conversaViewModel.closeConversation() }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        ConversaHeader(titulo = titulo, avatarUrl = avatarUrl, onBack = onBack)

        topContent?.invoke()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp).align(Alignment.Center),
                        color = colors.textSecondary,
                        strokeWidth = 2.dp
                    )
                }

                messages.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhuma mensagem ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Diga olá! 👋",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(messages, key = { _, msg -> msg.id }) { index, msg ->
                            val prevLabel = if (index > 0) formatMessageDateSeparator(messages[index - 1].sentAt) else ""
                            val currLabel = formatMessageDateSeparator(msg.sentAt)
                            MessageBubble(
                                message = msg,
                                currentUserId = user.id,
                                showDateSeparator = currLabel.isNotBlank() && currLabel != prevLabel,
                                onActivityRefTapped = onActivityRefTapped
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(colors.lightGray))

        // TODO: typing event não suportado pelo backend ainda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Escreva uma mensagem...",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
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
                    .background(if (inputText.isNotBlank()) colors.buttonContainer else colors.surface),
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = if (inputText.isNotBlank()) colors.buttonText else colors.textSecondary,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isSending) {
                                conversaViewModel.sendMessage(conversaId, inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Enviar",
                            tint = if (inputText.isNotBlank()) colors.buttonText else colors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversaHeader(titulo: String, avatarUrl: String?, onBack: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
                    .size(24.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = colors.textPrimary
                )
            }

            val initials = titulo.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
            val bgColor = conversaAvatarColor(titulo)

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (avatarUrl != null) Color.Transparent else bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = titulo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .size(24.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.lightGray)
        )
    }
}

private fun conversaAvatarColor(nome: String): Color {
    val palette = listOf(
        Color(0xFFA78BFA),
        Color(0xFF60A5FA),
        Color(0xFF34D399),
        Color(0xFFFBBF24),
        Color(0xFFF87171),
        Color(0xFF38BDF8),
        Color(0xFFFB923C)
    )
    return palette[Math.abs(nome.hashCode()) % palette.size]
}

@Composable
private fun MessageBubble(
    message: Message,
    currentUserId: String,
    showDateSeparator: Boolean,
    onActivityRefTapped: ((logId: String) -> Unit)? = null
) {
    val colors = LocalComunicacaoEscolarColors.current
    val isFromMe = message.isFromMe
    val isReadByOther = message.readBy.any { it != currentUserId }

    val bubbleSentBg = if (colors.isDark) Color(0xFFE0E0E0) else colors.primary
    val bubbleSentText = if (colors.isDark) Color(0xFF1A1A1A) else colors.textOnPrimary
    val textColor = if (isFromMe) bubbleSentText else colors.textPrimary

    val actRef = remember(message.id) { extractActivityRef(message.text) }
    val displayText = actRef?.second ?: message.text

    if (showDateSeparator) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatMessageDateSeparator(message.sentAt),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (isFromMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (isFromMe) bubbleSentBg else colors.surface)
            ) {
                if (actRef != null) {
                    val refBg = if (isFromMe)
                        Color.Black.copy(alpha = 0.10f)
                    else
                        colors.lightGray
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(refBg)
                            .then(
                                if (onActivityRefTapped != null)
                                    Modifier.clickable { onActivityRefTapped(actRef.first.logId) }
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.65f),
                            modifier = Modifier.size(13.dp)
                        )
                        Column {
                            Text(
                                text = "Atividade · ${actRef.first.date}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor.copy(alpha = 0.80f)
                            )
                            if (actRef.first.childName.isNotBlank()) {
                                Text(
                                    text = actRef.first.childName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            if (isFromMe) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp, end = 2.dp)
                ) {
                    Text(
                        text = message.sentAtFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                    Icon(
                        imageVector = if (isReadByOther) Icons.Outlined.DoneAll else Icons.Outlined.Check,
                        contentDescription = if (isReadByOther) "Lido" else "Enviado",
                        tint = if (isReadByOther) colors.primary else colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private data class ActivityRefData(val logId: String, val date: String, val childName: String)

private val ACT_REF_REGEX = Regex("""^\[act_ref:([^|]+)\|([^|]+)\|([^\]]*)\]\n?""")

private fun extractActivityRef(text: String): Pair<ActivityRefData, String>? {
    val match = ACT_REF_REGEX.find(text) ?: return null
    val (logId, date, childName) = match.destructured
    val rest = text.removePrefix(match.value).trim()
    return ActivityRefData(logId, date, childName) to rest
}
