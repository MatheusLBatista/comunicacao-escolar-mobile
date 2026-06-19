package dev.fslab.comunicacao.escolar.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevLoginSheet(
    onDismiss: () -> Unit,
    onSelectUser: (email: String, password: String) -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.inputBorder)
            )
        }
    ) {
        val sheetView = LocalView.current
        val isDark = colors.isDark
        val bgColor = colors.background
        SideEffect {
            val window = (sheetView.parent as? DialogWindowProvider)?.window
            if (window != null) {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
                window.navigationBarColor = bgColor.toArgb()
                WindowCompat.getInsetsController(window, sheetView)
                    .isAppearanceLightNavigationBars = !isDark
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Login rápido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "  DEV",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            devUsers.forEach { user ->
                Button(
                    onClick = {
                        onSelectUser(user.email, user.password)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonContainer,
                        contentColor = colors.buttonText,
                        disabledContainerColor = colors.inputBorder,
                        disabledContentColor = colors.textSecondary
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = user.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.buttonText
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.buttonText.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
