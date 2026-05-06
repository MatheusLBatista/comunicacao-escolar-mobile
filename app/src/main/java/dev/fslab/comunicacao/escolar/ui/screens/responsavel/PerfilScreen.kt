package dev.fslab.comunicacao.escolar.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.blur
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.PerfilUiState
import dev.fslab.comunicacao.escolar.ui.viewmodel.PerfilViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeMode
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors


@Composable
fun PerfilScreen(
    user: User,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val uiState by perfilViewModel.uiState.collectAsState()
    val salvando by perfilViewModel.salvando.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val colors = LocalComunicacaoEscolarColors.current

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var nomeTemp by remember { mutableStateOf(user.nome) }
    var showAvatarLightbox by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            perfilViewModel.uploadAvatar(context, uri) { updatedUser ->
                authViewModel.updateCurrentUser(updatedUser)
            }
        }
    }

    LaunchedEffect(user) { perfilViewModel.inicializar(user) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PerfilUiState.Success -> {
                snackbarHostState.showSnackbar((uiState as PerfilUiState.Success).message)
                perfilViewModel.clearState()
            }
            is PerfilUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as PerfilUiState.Error).message)
                perfilViewModel.clearState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(if (showAvatarLightbox) Modifier.blur(20.dp) else Modifier)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Perfil",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            // ── Avatar + Nome ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .then(if (user.avatar != null) Modifier.clickable { showAvatarLightbox = true } else Modifier)
                    ) {
                        // Avatar: foto real se disponível, ícone Person cinza caso contrário
                        if (user.avatar != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user.avatar)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar de ${user.nome}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }

                    // Botão de editar avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E272C))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Alterar foto",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user.nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            // ── Informações Pessoais ──────────────────────────────────────────
            SectionHeader("Informações Pessoais")

            InfoCard {
                InfoRow(
                    label = "Nome",
                    value = user.nome,
                    onEdit = { showEditDialog = true }
                )
                InfoDivider()
                InfoRow(label = "E-mail", value = user.email.ifEmpty { "—" })
                InfoDivider()
                InfoRow(
                    label = "Senha",
                    value = "••••••••••",
                    onEdit = { /* TODO: tela de redefinição de senha */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Preferências ──────────────────────────────────────────────────
            SectionHeader("Preferências")

            InfoCard {
                PrefsRow(
                    label = "Tema",
                    value = when (themeMode) {
                        ThemeMode.SYSTEM -> "Seguir o sistema"
                        ThemeMode.LIGHT  -> "Claro"
                        ThemeMode.DARK   -> "Escuro"
                    },
                    onClick = { showThemeDialog = true }
                )
                InfoDivider()
                PrefsRow(
                    label = "Fuso Horário",
                    value = fusoHorarioLabel(user.fusoHorario),
                    onClick = { /* TODO: seletor de fuso horário */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Sair ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colors.errorBackground, RoundedCornerShape(16.dp))
                    .background(colors.background)
                    .clickable { onLogout() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = "Sair",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Sair do aplicativo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // ── Lightbox de avatar ────────────────────────────────────────────────
        if (showAvatarLightbox && user.avatar != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showAvatarLightbox = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar de ${user.nome}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
                IconButton(
                    onClick = { showAvatarLightbox = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // ── Dialog de edição de nome ──────────────────────────────────────────────
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar nome", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = nomeTemp,
                    onValueChange = { nomeTemp = it },
                    label = { Text("Nome completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.focusedIndicator,
                        focusedLabelColor = colors.focusedIndicator
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        perfilViewModel.salvarNome(
                            userId = user.id,
                            novoNome = nomeTemp,
                            onSuccess = { updatedUser ->
                                authViewModel.updateCurrentUser(updatedUser)
                            }
                        )
                    },
                    enabled = nomeTemp.trim().isNotEmpty() && !salvando
                ) {
                    if (salvando) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Salvar", fontWeight = FontWeight.Bold, color = colors.buttonContainer)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false; nomeTemp = user.nome }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Seletor de tema ──────────────────────────────────────────────────────
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text("Tema do aplicativo", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        ThemeMode.SYSTEM to "Seguir o sistema",
                        ThemeMode.LIGHT  to "Claro",
                        ThemeMode.DARK   to "Escuro"
                    ).forEach { (mode, label) ->
                        val selected = themeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) colors.lightGray else Color.Transparent)
                                .clickable {
                                    themeViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                color = if (selected) colors.textPrimary else colors.textSecondary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.primaryDark)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ── Composables auxiliares ─────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    val colors = LocalComunicacaoEscolarColors.current
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = colors.textSecondary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp).padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.lightGray, RoundedCornerShape(16.dp))
            .background(colors.surface)
    ) {
        content()
    }
}

@Composable
private fun InfoDivider() {
    val colors = LocalComunicacaoEscolarColors.current
    HorizontalDivider(color = colors.lightGray, thickness = 1.dp)
}

@Composable
private fun InfoRow(label: String, value: String, onEdit: (() -> Unit)? = null) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = colors.textPrimary)
        }
        if (onEdit != null) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Editar $label",
                tint = colors.iconGray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onEdit() }
            )
        }
    }
}


@Composable
private fun PrefsRow(label: String, value: String, onClick: () -> Unit) {
    val colors = LocalComunicacaoEscolarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = colors.textPrimary)
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.iconGray,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun fusoHorarioLabel(fusoHorario: String): String {
    return when (fusoHorario) {
        "America/Manaus"     -> "Horário Padrão do Amazonas (GMT-4)"
        "America/Sao_Paulo"  -> "Horário de Brasília (GMT-3)"
        "America/Belem"      -> "Horário de Belém (GMT-3)"
        "America/Fortaleza"  -> "Horário de Fortaleza (GMT-3)"
        "America/Recife"     -> "Horário de Recife (GMT-3)"
        "America/Maceio"     -> "Horário de Maceió (GMT-3)"
        "America/Bahia"      -> "Horário da Bahia (GMT-3)"
        "America/Cuiaba"     -> "Horário do Mato Grosso (GMT-4)"
        "America/Porto_Velho" -> "Horário de Porto Velho (GMT-4)"
        "America/Boa_Vista"  -> "Horário de Boa Vista (GMT-4)"
        "America/Rio_Branco" -> "Horário do Acre (GMT-5)"
        "America/Noronha"    -> "Horário de Fernando de Noronha (GMT-2)"
        else                 -> fusoHorario
    }
}
