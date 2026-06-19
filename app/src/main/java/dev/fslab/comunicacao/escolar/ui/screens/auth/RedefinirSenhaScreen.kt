package dev.fslab.comunicacao.escolar.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel

@Composable
fun RedefinirSenhaScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val authState by authViewModel.authState.collectAsState()
    
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    var codigo by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.PasswordResetSuccess) {
            onNavigateToLogin()
            authViewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Redefinir Senha",
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Insira o código de 6 dígitos enviado para o seu e-mail e crie uma nova senha.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = codigo,
                onValueChange = { input -> 
                    val limpo = input.replace("[^a-zA-Z0-9]".toRegex(), "").uppercase()
                    codigo = limpo.take(6)
                },
                label = { Text("Código de 6 dígitos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.inputBorder,
                    focusedBorderColor = colors.focusedIndicator,
                    errorBorderColor = colors.error,
                    cursorColor = colors.focusedIndicator,
                    focusedLabelColor = colors.focusedIndicator,
                    unfocusedLabelColor = colors.textSecondary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    unfocusedContainerColor = colors.background,
                    focusedContainerColor = colors.background
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = novaSenha,
                onValueChange = { novaSenha = it },
                label = { Text("Nova Senha") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(imageVector = icon, contentDescription = "Alternar visibilidade", tint = colors.textSecondary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.inputBorder,
                    focusedBorderColor = colors.focusedIndicator,
                    errorBorderColor = colors.error,
                    cursorColor = colors.focusedIndicator,
                    focusedLabelColor = colors.focusedIndicator,
                    unfocusedLabelColor = colors.textSecondary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    unfocusedContainerColor = colors.background,
                    focusedContainerColor = colors.background
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { authViewModel.resetPasswordByCode(codigo, novaSenha) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                enabled = !isLoading && codigo.length == 6 && novaSenha.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.mediumGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.textOnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Redefinir Senha",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.buttonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Voltar para o login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
