package dev.fslab.comunicacao.escolar.ui.screens.conversas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.model.Conversation
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.model.UserRole
import dev.fslab.comunicacao.escolar.network.SocketManager
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.screens.admin.UserAvatar
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ConversaViewModel

@Composable
fun ConversaListScreen(
    user: User,
    accessToken: String,
    conversaViewModel: ConversaViewModel,
    onOpenConversa: (conversaId: String, titulo: String, avatarUrl: String?) -> Unit,
    onNovaConversa: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val conversations by conversaViewModel.conversations.collectAsState()
    val isLoading by conversaViewModel.conversationsLoading.collectAsState()
    val unreadCounts by conversaViewModel.unreadCounts.collectAsState()
    val lastMessages by conversaViewModel.lastMessages.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(user.id, user.schoolId) {
        if (accessToken.isNotBlank()) SocketManager.connect(accessToken)
        if (user.schoolId != null) {
            conversaViewModel.loadConversations(user.schoolId, user.id)
        }
    }

    DisposableEffect(Unit) {
        conversaViewModel.startListening()
        onDispose { conversaViewModel.stopListening() }
    }

    val filtered = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { c ->
            c.otherParticipant.fullName.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppHeader("Conversas")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Buscar conversa...",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
                Box {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showFilterMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filtrar",
                            tint = colors.textTertiary
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        offset = DpOffset(0.dp, 4.dp),
                        modifier = Modifier.background(colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Todos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = { showFilterMenu = false }
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colors.textSecondary,
                            strokeWidth = 2.dp
                        )
                    }
                }

                user.schoolId == null -> {
                    EmptyConversas(
                        message = "Você ainda não está vinculado a uma escola.",
                        subtitle = "Entre em contato com o administrador."
                    )
                }

                filtered.isEmpty() -> {
                    EmptyConversas(
                        message = if (searchQuery.isBlank()) "Nenhuma conversa ainda."
                        else "Nenhum resultado para \"$searchQuery\".",
                        subtitle = if (searchQuery.isBlank() && user.role == UserRole.RESPONSAVEL)
                            "Toque em + para iniciar uma conversa." else ""
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filtered, key = { it.id }) { conversa ->
                            val unread = unreadCounts[conversa.id] ?: 0
                            val preview = lastMessages[conversa.id] ?: ""
                            ConversaItem(
                                conversa = conversa,
                                userRole = user.role,
                                unreadCount = unread,
                                previewText = preview,
                                onClick = {
                                    onOpenConversa(conversa.id, conversa.otherParticipant.fullName, conversa.avatarUrl)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (user.schoolId != null && !isLoading) {
            FloatingActionButton(
                onClick = onNovaConversa,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 16.dp),
                containerColor = colors.buttonContainer,
                contentColor = colors.buttonText,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Nova conversa")
            }
        }
    }
}

@Composable
private fun ConversaItem(
    conversa: Conversation,
    userRole: UserRole,
    unreadCount: Int,
    previewText: String,
    onClick: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val name = conversa.otherParticipant.fullName.ifBlank { "Desconhecido" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserAvatar(nome = name, size = 48)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (conversa.lastMessageAtFormatted.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = conversa.lastMessageAtFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (unreadCount > 0) (if (colors.isDark) Color.White else colors.primary) else colors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color.White else colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (colors.isDark) Color(0xFF121212) else colors.textOnPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp)
            .height(0.5.dp)
            .background(colors.lightGray)
    )
}

@Composable
private fun EmptyConversas(message: String, subtitle: String = "") {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Chat,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
