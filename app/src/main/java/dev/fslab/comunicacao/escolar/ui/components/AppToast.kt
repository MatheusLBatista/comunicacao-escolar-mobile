package dev.fslab.comunicacao.escolar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.ui.theme.ErrorBackground
import dev.fslab.comunicacao.escolar.ui.theme.ErrorText
import dev.fslab.comunicacao.escolar.ui.theme.SuccessBackground
import dev.fslab.comunicacao.escolar.ui.theme.SuccessText
import kotlinx.coroutines.delay

enum class ToastType { ERROR, SUCCESS }

class AppToastState {
    var message by mutableStateOf<String?>(null)
        private set
    var type by mutableStateOf(ToastType.ERROR)
        private set

    fun showError(msg: String) { type = ToastType.ERROR; message = msg }
    fun showSuccess(msg: String) { type = ToastType.SUCCESS; message = msg }
    fun dismiss() { message = null }
}

@Composable
fun rememberAppToastState() = remember { AppToastState() }

@Composable
fun BoxScope.AppToast(state: AppToastState) {
    val message = state.message

    LaunchedEffect(message) {
        if (message != null) {
            delay(3500)
            state.dismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(250)
        ) + fadeOut(tween(200)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        val isError = state.type == ToastType.ERROR
        val bgColor = if (isError) ErrorBackground else SuccessBackground
        val textColor = if (isError) ErrorText else SuccessText
        val icon = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
