package dev.fslab.comunicacao.escolar.ui.screens.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.fslab.comunicacao.escolar.network.GoogleSignInConfig
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fslab.comunicacao.escolar.BuildConfig
import dev.fslab.comunicacao.escolar.R

import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthState
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeMode
import dev.fslab.comunicacao.escolar.ui.viewmodel.ThemeViewModel

/**
 * LoginScreen — Tela de autenticação.
 *
 * Segue o padrão de UI definido no protótipo (branco, tipografia Poppins,
 * botão escuro arredondado). Usa somente cores semânticas do tema.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToCadastro: () -> Unit = {},
    onNavigateToRecuperarSenha: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val colors = LocalComunicacaoEscolarColors.current
    val authState by authViewModel.authState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var lembrarMe by remember { mutableStateOf(false) }
    var showDevSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
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
                text = "Bem-vindo!",
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Entre na sua conta para acessar a plataforma escolar.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
                lineHeight = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { authViewModel.clearError() }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it.trim()
                    if (errorMessage != null) authViewModel.clearError()
                },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading,
                isError = errorMessage != null,
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
                value = senha,
                onValueChange = { 
                    senha = it
                    if (errorMessage != null) authViewModel.clearError()
                },
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
                isError = errorMessage != null,
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { lembrarMe = !lembrarMe }
                ) {
                    Checkbox(
                        checked = lembrarMe,
                        onCheckedChange = { lembrarMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.textPrimary,
                            checkmarkColor = colors.background,
                            uncheckedColor = colors.inputBorder
                        )
                    )
                    Text(
                        text = "Lembrar-me",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = "Esqueceu a senha?",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToRecuperarSenha() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.loginUser(email, senha) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonContainer,
                    contentColor = colors.buttonText,
                    disabledContainerColor = colors.mediumGray
                ),
                enabled = !isLoading && email.isNotBlank() && senha.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.textOnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Entrar",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = colors.buttonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(GoogleSignInConfig.WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context,
                            )
                            val googleCredential =
                                GoogleIdTokenCredential.createFrom(result.credential.data)
                            authViewModel.loginWithGoogle(googleCredential.idToken)
                        } catch (_: GetCredentialCancellationException) {
                            // Usuário cancelou — sem ação
                        } catch (_: NoCredentialException) {
                            // Sem credenciais salvas — sem ação (o seletor do Google já cuida de oferecer contas se configurado)
                        } catch (_: GetCredentialException) {
                            authViewModel.setError("Falha ao iniciar login com Google. Tente novamente.")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, colors.inputBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.background
                ),
                enabled = !isLoading
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Entrar com Google",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Não tem uma conta? ",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Text(
                    text = "Cadastrar-se",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToCadastro() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (BuildConfig.DEV_LOGIN_ENABLED) {
            androidx.compose.material3.TextButton(
                onClick = { showDevSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "DEV",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
            }
        }

        IconButton(
            onClick = {
                themeViewModel.setThemeMode(
                    if (colors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = if (colors.isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                contentDescription = if (colors.isDark) "Mudar para tema claro" else "Mudar para tema escuro",
                tint = colors.textSecondary
            )
        }
    }

    if (BuildConfig.DEV_LOGIN_ENABLED && showDevSheet) {
        DevLoginSheet(
            onDismiss = { showDevSheet = false },
            onSelectUser = { devEmail, devPassword ->
                email = devEmail
                senha = devPassword
                authViewModel.loginUser(devEmail, devPassword)
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    ComunicacaoEscolarTheme {
        LoginScreen(
            authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            themeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        )
    }
}
