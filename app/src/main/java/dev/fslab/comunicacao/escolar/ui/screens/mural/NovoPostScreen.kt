package dev.fslab.comunicacao.escolar.ui.screens.mural

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.Docs
import dev.fslab.comunicacao.escolar.model.PostTarget
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.CreatePostState
import dev.fslab.comunicacao.escolar.ui.viewmodel.EditPostState
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralViewModel

@Composable
fun NovoPostScreen(
    schoolId: String,
    muralViewModel: MuralViewModel,
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    postToEdit: Docs? = null,
    turmas: List<Turma> = emptyList()
) {
    val isEditMode = postToEdit != null
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    val createPostState by muralViewModel.createPostState.collectAsState()
    val editPostState by muralViewModel.editPostState.collectAsState()

    var title by remember { mutableStateOf(postToEdit?.title ?: "") }
    var content by remember { mutableStateOf(postToEdit?.content ?: "") }
    var submitted by remember { mutableStateOf(false) }
    val turmasSelecionadas = remember {
        mutableStateListOf<String>().also { list ->
            if (postToEdit?.target?.scope == "class") {
                list.addAll(postToEdit.target.targetIds)
            }
        }
    }
    val selectedImages = remember { mutableStateListOf<Uri>() }
    val removedAttachmentIds = remember { mutableStateListOf<String>() }
    val token = remember { TokenManager.getAccessToken() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri ->
            if (!selectedImages.contains(uri)) selectedImages.add(uri)
        }
    }

    LaunchedEffect(createPostState) {
        if (createPostState is CreatePostState.Success) {
            muralViewModel.resetCreatePostState()
            onPostCreated()
        }
    }

    LaunchedEffect(editPostState) {
        if (editPostState is EditPostState.Success) {
            muralViewModel.resetEditPostState()
            onPostCreated()
        }
    }

    val isLoading = createPostState is CreatePostState.Loading || editPostState is EditPostState.Loading

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.focusedIndicator,
        unfocusedBorderColor = colors.inputBorder,
        focusedTextColor = colors.textInput,
        unfocusedTextColor = colors.textInput,
        cursorColor = colors.focusedIndicator,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        errorBorderColor = colors.error,
        errorContainerColor = colors.surface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AppHeader(
            title = if (isEditMode) "Editar Post" else "Novo Post",
            onBack = onBack,
            action = {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colors.textPrimary
                        )
                    }
                } else {
                    TextButton(onClick = {
                        submitted = true
                        if (title.isNotBlank() && content.isNotBlank()) {
                            val target = if (turmasSelecionadas.isEmpty()) {
                                PostTarget("all", emptyList())
                            } else {
                                PostTarget("class", turmasSelecionadas.toList())
                            }
                            if (isEditMode) {
                                muralViewModel.updatePost(
                                    postId = postToEdit!!.id,
                                    title = title,
                                    content = content,
                                    target = target,
                                    removedAttachmentIds = removedAttachmentIds.toList(),
                                    newImageUris = selectedImages.toList(),
                                    context = context
                                )
                            } else {
                                muralViewModel.createPost(
                                    schoolId = schoolId,
                                    title = title,
                                    content = content,
                                    target = target,
                                    imageUris = selectedImages.toList(),
                                    context = context
                                )
                            }
                        }
                    }) {
                        Text(
                            text = if (isEditMode) "Salvar" else "Publicar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TÍTULO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            "Título do post",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = submitted && title.isBlank(),
                    supportingText = if (submitted && title.isBlank()) {
                        { Text("Campo obrigatório") }
                    } else null,
                    colors = fieldColors
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "CONTEÚDO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = {
                        Text(
                            "Escreva o conteúdo do post...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    isError = submitted && content.isBlank(),
                    supportingText = if (submitted && content.isBlank()) {
                        { Text("Campo obrigatório") }
                    } else null,
                    colors = fieldColors
                )
            }

            if (turmas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DESTINATÁRIOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    PostTurmaMultiSelect(
                        turmas = turmas.map { it.id to it.nome },
                        selecionadas = turmasSelecionadas,
                        onToggle = { id ->
                            if (id in turmasSelecionadas) turmasSelecionadas.remove(id)
                            else turmasSelecionadas.add(id)
                        },
                        onSelecionarTodas = { turmasSelecionadas.clear() }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "IMAGENS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )

                val existingAttachments = if (isEditMode) {
                    postToEdit!!.attachments.filter { it !in removedAttachmentIds }
                } else emptyList()
                val totalCount = existingAttachments.size + selectedImages.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, colors.inputBorder, RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (totalCount == 0) "Adicionar imagens"
                               else "$totalCount imagem${if (totalCount > 1) "ns" else ""} selecionada${if (totalCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (totalCount == 0) colors.textSecondary else colors.textPrimary
                    )
                }

                if (totalCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingAttachments.forEach { attachmentId ->
                            Box(modifier = Modifier.size(80.dp)) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("${RetrofitClient.BASE_URL}attachments/$attachmentId")
                                        .addHeader("Authorization", "Bearer ${token ?: ""}")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(colors.lightGray, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { removedAttachmentIds.add(attachmentId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remover",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        selectedImages.forEach { uri ->
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { selectedImages.remove(uri) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remover",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val errorMessage = when {
                createPostState is CreatePostState.Error -> (createPostState as CreatePostState.Error).message
                editPostState is EditPostState.Error -> (editPostState as EditPostState.Error).message
                else -> null
            }
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PostTurmaMultiSelect(
    turmas: List<Pair<String, String>>,
    selecionadas: List<String>,
    onToggle: (String) -> Unit,
    onSelecionarTodas: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val turmasFiltradas by remember(turmas, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) turmas
            else turmas.filter { it.second.contains(searchQuery, ignoreCase = true) }
        }
    }

    val displayText = when {
        selecionadas.isEmpty() -> "Todas as turmas"
        selecionadas.size == 1 -> turmas.firstOrNull { it.first == selecionadas[0] }?.second ?: "1 turma"
        selecionadas.size == 2 -> turmas.filter { it.first in selecionadas }.joinToString(", ") { it.second }
        else -> "${selecionadas.size} turmas selecionadas"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    showMenu = !showMenu
                    if (!showMenu) searchQuery = ""
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selecionadas.isEmpty()) colors.textSecondary else colors.textPrimary
            )
            Icon(imageVector = Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = colors.textSecondary)
        }

        if (showMenu) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
            ) {
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar turma...", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
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
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        DropdownMenuItem(
                            text = {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Todas as turmas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selecionadas.isEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selecionadas.isEmpty()) colors.buttonContainer else colors.textSecondary
                                    )
                                    if (selecionadas.isEmpty()) {
                                        Icon(imageVector = Icons.Outlined.Check, contentDescription = null, tint = colors.buttonContainer, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            onClick = { onSelecionarTodas(); showMenu = false; searchQuery = "" }
                        )
                        HorizontalDivider(color = colors.inputBorder, thickness = 0.5.dp)
                    }

                    turmasFiltradas.forEach { (id, nome) ->
                        val selecionada = id in selecionadas
                        DropdownMenuItem(
                            text = {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selecionada) colors.buttonContainer else colors.textPrimary
                                    )
                                    if (selecionada) {
                                        Icon(imageVector = Icons.Outlined.Check, contentDescription = null, tint = colors.buttonContainer, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            onClick = { onToggle(id) }
                        )
                    }
                }
            }
        }
    }
}
