package dev.fslab.comunicacao.escolar.ui.screens.conversas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.model.UserRole
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.screens.admin.UsuarioListItem
import dev.fslab.comunicacao.escolar.ui.screens.admin.UserAvatar
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.ConversaViewModel

@Composable
fun NovaConversaScreen(
    user: User,
    conversaViewModel: ConversaViewModel,
    onBack: () -> Unit,
    onConversaCreated: (conversaId: String, titulo: String, avatarUrl: String?) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val allContacts by conversaViewModel.potentialContacts.collectAsState()
    val isLoading by conversaViewModel.contactsLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var tabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(user.schoolId) {
        user.schoolId?.let { sid ->
            conversaViewModel.loadPotentialContacts(sid, user.id)
        }
    }

    val admins = remember(allContacts, user.schoolId) {
        allContacts.filter { u ->
            u.memberships.any { m -> m.role == "admin" && m.schoolId == user.schoolId }
        }
    }
    val teachers = remember(allContacts, user.schoolId) {
        allContacts.filter { u ->
            u.memberships.any { m -> m.role == "teacher" && m.schoolId == user.schoolId }
        }
    }
    val parents = remember(allContacts, user.schoolId) {
        allContacts.filter { u ->
            u.memberships.any { m -> m.role == "parent" && m.schoolId == user.schoolId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader(title = "Nova Conversa", onBack = onBack)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Buscar usuário...",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(52.dp),
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

        // Tabs — only for ADMIN (sees both professor and responsável)
        if (user.role == UserRole.ADMIN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Professores", "Responsáveis").forEachIndexed { idx, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (tabIndex == idx) colors.buttonContainer else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { tabIndex = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (tabIndex == idx) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (tabIndex == idx) colors.buttonText else colors.textSecondary
                        )
                    }
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

            else -> {
                val contactList: List<ApiSchoolUser> = when (user.role) {
                    UserRole.ADMIN       -> if (tabIndex == 0) teachers else parents
                    UserRole.RESPONSAVEL -> teachers
                    UserRole.PROFESSOR   -> parents
                }
                val filtered = contactList.filter {
                    searchQuery.isBlank() || it.fullName.contains(searchQuery, ignoreCase = true)
                }
                val showSecretaria = user.role == UserRole.RESPONSAVEL && admins.isNotEmpty() &&
                    (searchQuery.isBlank() || "secretaria".contains(searchQuery, ignoreCase = true) ||
                     "escola".contains(searchQuery, ignoreCase = true))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 12.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showSecretaria) {
                        val adminUser = admins.first()
                        item(key = "secretaria") {
                            SecretariaItem(onClick = {
                                user.schoolId?.let { sid ->
                                    conversaViewModel.findOrCreateConversation(sid, adminUser.id) { id ->
                                        onConversaCreated(id, "Secretaria (Escola)", adminUser.avatarUrl)
                                    }
                                }
                            })
                        }
                    }

                    if (filtered.isEmpty() && !showSecretaria) {
                        item {
                            Text(
                                text = "Nenhum contato encontrado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp)
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { contact ->
                            UsuarioListItem(
                                nome = contact.fullName,
                                subtitle = contact.email ?: "",
                                onClick = {
                                    user.schoolId?.let { sid ->
                                        conversaViewModel.findOrCreateConversation(sid, contact.id) { id ->
                                            onConversaCreated(id, contact.fullName, contact.avatarUrl)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecretariaItem(onClick: () -> Unit) {
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Business,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Secretaria (Escola)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = "Administração",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}
