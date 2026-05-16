package dev.fslab.comunicacao.escolar.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel

@Composable
fun CadastroScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    onCadastroSuccess: () -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val authState by authViewModel.authState.collectAsState()

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message
    val isRegistered = authState is AuthState.Registered

    LaunchedEffect(isRegistered) {
        if (isRegistered) onCadastroSuccess()
    }

    var nomeCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }

    var nomeError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var globalError by remember { mutableStateOf<String?>(null) }

    val confirmarSenhaError: String? =
        if (confirmarSenha.isNotEmpty() && senha != confirmarSenha) "As senhas não coincidem" else null

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            when {
                errorMessage.contains("não cadastrado", ignoreCase = true) ||
                errorMessage.contains("já está ativa", ignoreCase = true) ||
                errorMessage.contains("Dados inválidos", ignoreCase = true) -> emailError = errorMessage
                else -> globalError = errorMessage
            }
            authViewModel.clearError()
        }
    }

    fun validateAndSubmit() {
        nomeError = null; emailError = null; senhaError = null; globalError = null
        var hasError = false

        if (nomeCompleto.isBlank()) {
            nomeError = "Informe seu nome completo"
            hasError = true
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Digite um e-mail válido"
            hasError = true
        }
        val senhaValida = senha.length >= 8
                && senha.any { it.isUpperCase() }
                && senha.any { it.isLowerCase() }
                && senha.any { it.isDigit() }
        if (!senhaValida) {
            senhaError = "Mínimo 8 caracteres, 1 maiúscula, 1 minúscula e 1 número"
            hasError = true
        }
        if (senha != confirmarSenha) hasError = true

        if (!hasError) {
            authViewModel.registerUser(fullName = nomeCompleto, email = email, password = senha)
        }
    }

    val formPreenchido = nomeCompleto.isNotBlank()
            && email.isNotBlank()
            && senha.isNotBlank()
            && confirmarSenha.isNotBlank()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = colors.inputBorder,
        focusedBorderColor = colors.focusedIndicator,
        errorBorderColor = colors.error,
        cursorColor = colors.focusedIndicator,
        focusedLabelColor = colors.focusedIndicator,
        unfocusedLabelColor = colors.textSecondary,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        errorTextColor = colors.textPrimary,
        unfocusedContainerColor = colors.background,
        focusedContainerColor = colors.background,
        errorContainerColor = colors.background,
        errorLabelColor = colors.error,
        errorCursorColor = colors.error,
        errorSupportingTextColor = colors.error
    )

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
                text = "Criar conta",
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Insira o e-mail cadastrado pelo administrador da sua escola para ativar sua conta.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = nomeCompleto,
                onValueChange = { nomeCompleto = it; nomeError = null },
                label = { Text("Nome completo") },
                isError = nomeError != null,
                supportingText = nomeError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                enabled = !isLoading,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim(); emailError = null },
                label = { Text("E-mail") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it; senhaError = null },
                label = { Text("Senha") },
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                            tint = colors.textSecondary
                        )
                    }
                },
                isError = senhaError != null,
                supportingText = {
                    Text(
                        text = senhaError ?: "Mínimo 8 caracteres, 1 maiúscula, 1 minúscula e 1 número",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (senhaError != null) colors.error else colors.textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                label = { Text("Confirmar senha") },
                trailingIcon = {
                    IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                        Icon(
                            imageVector = if (confirmarSenhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmarSenhaVisivel) "Ocultar senha" else "Mostrar senha",
                            tint = colors.textSecondary
                        )
                    }
                },
                isError = confirmarSenhaError != null,
                supportingText = confirmarSenhaError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                colors = fieldColors
            )

            if (globalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = globalError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { globalError = null }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.mediumGray
                ),
                enabled = !isLoading && formPreenchido
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.buttonText,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Criar conta",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Já tem uma conta? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CadastroScreenPreview() {
    ComunicacaoEscolarTheme {
        CadastroScreen(authViewModel = androidx.lifecycle.viewmodel.compose.viewModel())
    }
}
