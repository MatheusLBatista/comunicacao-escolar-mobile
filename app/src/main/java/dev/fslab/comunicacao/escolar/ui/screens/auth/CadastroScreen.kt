package dev.fslab.comunicacao.escolar.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel

/**
 * CadastroScreen — Tela de auto-cadastro.
 *
 * Permite que responsáveis e professores criem suas próprias contas.
 * Após o cadastro bem-sucedido, o usuário é redirecionado de volta ao login.
 * O acesso ao aplicativo depende de aprovação do administrador da escola.
 */
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

    // Navega de volta ao login após cadastro bem-sucedido
    LaunchedEffect(isRegistered) {
        if (isRegistered) {
            onCadastroSuccess()
        }
    }

    // Garante ícones escuros nas system bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    var nomeCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }

    val senhasConferem = senha == confirmarSenha || confirmarSenha.isEmpty()
    val formValido = nomeCompleto.isNotBlank()
            && email.isNotBlank()
            && senha.length >= 8
            && senha == confirmarSenha

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // Botão Voltar
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(start = 0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título
        Text(
            text = "Criar conta",
            style = MaterialTheme.typography.displayMedium,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo
        Text(
            text = "Após o cadastro, aguarde o administrador vincular sua conta à escola.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Campo Nome completo
        OutlinedTextField(
            value = nomeCompleto,
            onValueChange = { nomeCompleto = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.inputBorder,
                focusedBorderColor = colors.primaryDark,
                cursorColor = colors.primaryDark,
                focusedLabelColor = colors.primaryDark,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.inputBorder,
                focusedBorderColor = colors.primaryDark,
                cursorColor = colors.primaryDark,
                focusedLabelColor = colors.primaryDark,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Senha
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading,
            supportingText = {
                Text(
                    text = "Mínimo 8 caracteres, 1 maiúscula, 1 minúscula e 1 número",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.inputBorder,
                focusedBorderColor = colors.primaryDark,
                cursorColor = colors.primaryDark,
                focusedLabelColor = colors.primaryDark,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Confirmar Senha
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
            isError = !senhasConferem,
            supportingText = if (!senhasConferem) {
                { Text("As senhas não coincidem", color = colors.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (senhasConferem) colors.inputBorder else colors.error,
                focusedBorderColor = if (senhasConferem) colors.primaryDark else colors.error,
                errorBorderColor = colors.error,
                cursorColor = colors.primaryDark,
                focusedLabelColor = colors.primaryDark,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mensagem de erro da API
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { authViewModel.clearError() }
            )
        }

        // Mensagem de sucesso
        if (isRegistered) {
            Text(
                text = (authState as AuthState.Registered).message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.success,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // Botão Criar conta
        Button(
            onClick = {
                authViewModel.registerUser(
                    fullName = nomeCompleto,
                    email = email,
                    password = senha
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryDark,
                disabledContainerColor = colors.mediumGray
            ),
            enabled = !isLoading && formValido
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colors.textOnPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Criar conta",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textOnPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Já tem conta?
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CadastroScreenPreview() {
    ComunicacaoEscolarTheme {
        CadastroScreen(authViewModel = androidx.lifecycle.viewmodel.compose.viewModel())
    }
}
