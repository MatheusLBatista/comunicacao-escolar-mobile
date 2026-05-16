package dev.fslab.comunicacao.escolar.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors

@Composable
internal fun TurmaDropdownField(
    turmas: List<Turma>,
    selectedTurma: Turma?,
    onTurmaSelected: (Turma) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecione a turma...",
    enabled: Boolean = true,
    disabledText: String = placeholder,
    clearOptionLabel: String? = null,
    onClearSelected: (() -> Unit)? = null
) {
    val colors = LocalComunicacaoEscolarColors.current
    val density = LocalDensity.current
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var boxWidth by remember { mutableIntStateOf(0) }
    var openUpward by remember { mutableStateOf(false) }

    val turmasFiltradas by remember(turmas, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) turmas
            else turmas.filter { it.nome.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                boxWidth = coords.size.width
                val rootY = coords.positionInRoot().y.toInt()
                val anchorBottom = rootY + coords.size.height
                val maxPopupPx = with(density) { 300.dp.toPx() }.toInt()
                openUpward = anchorBottom + maxPopupPx > view.height
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .then(
                    if (enabled) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenu = true } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!enabled) disabledText else selectedTurma?.nome ?: placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled && selectedTurma != null) colors.textPrimary else colors.textSecondary
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        if (enabled && showMenu) {
            val gapPx = with(density) { 2.dp.toPx() }.toInt()
            val positionProvider = remember(openUpward) {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        return if (openUpward) {
                            IntOffset(anchorBounds.left, anchorBounds.top - popupContentSize.height - gapPx)
                        } else {
                            IntOffset(anchorBounds.left, anchorBounds.bottom + gapPx)
                        }
                    }
                }
            }

            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { showMenu = false; searchQuery = "" },
                properties = PopupProperties(focusable = true)
            ) {
                val widthDp = with(density) { boxWidth.toDp() }

                val searchField: @Composable () -> Unit = {
                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Buscar turma...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
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
                }

                val itemsList: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (clearOptionLabel != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        clearOptionLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                },
                                onClick = { onClearSelected?.invoke(); showMenu = false; searchQuery = "" }
                            )
                        }
                        turmasFiltradas.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        t.nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary
                                    )
                                },
                                onClick = { onTurmaSelected(t); showMenu = false; searchQuery = "" }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .width(widthDp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                ) {
                    if (openUpward) {
                        itemsList()
                        searchField()
                    } else {
                        searchField()
                        itemsList()
                    }
                }
            }
        }
    }
}
