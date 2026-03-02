package dev.fslab.comunicacao.escolar.ui.theme.screens

import android.app.Activity
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import dev.fslab.comunicacao.escolar.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LightComunicacaoEscolarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onEsqueciSenha: (String) -> Unit = {},
    onRegister: () -> Unit = {},
    onLogin: () -> Unit = {},
    onLoginGoogle: () -> Unit = {}
) {
    // LoginScreen sempre usa cores light, independente do tema do sistema
    val colors = LightComunicacaoEscolarColors

    // Força status bar e navigation bar brancas com ícones escuros
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.loginBackground.toArgb()
            window.navigationBarColor = colors.loginBackground.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var lembrarMe by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.loginBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            // Título
            Text(
                text = "Bem-vindo!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.loginDarkBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Sistema de comunicação escolar.",
                fontSize = 15.sp,
                color = colors.loginGrayText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo E-mail
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text("E-mail", color = colors.loginGrayText)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.loginBorder,
                    focusedBorderColor = colors.loginDarkBlue,
                    cursorColor = colors.loginDarkBlue,
                    focusedTextColor = colors.loginDarkBlue,
                    unfocusedTextColor = colors.loginDarkBlue,
                    unfocusedContainerColor = colors.surface,
                    focusedContainerColor = colors.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Senha
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = {
                    Text("Senha", color = colors.loginGrayText)
                },
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                            tint = colors.loginGrayText
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.loginBorder,
                    focusedBorderColor = colors.loginDarkBlue,
                    cursorColor = colors.loginDarkBlue,
                    focusedTextColor = colors.loginDarkBlue,
                    unfocusedTextColor = colors.loginDarkBlue,
                    unfocusedContainerColor = colors.surface,
                    focusedContainerColor = colors.surface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lembrar-me e Esqueceu a senha?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = lembrarMe,
                        onCheckedChange = { lembrarMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.loginDarkBlue,
                            uncheckedColor = colors.loginBorder
                        )
                    )
                    Text(
                        text = "Ativar biometria",
                        fontSize = 13.sp,
                        color = colors.loginGrayText
                    )
                }
                Text(
                    text = "Esqueceu a senha?",
                    fontSize = 13.sp,
                    color = colors.loginDarkBlue,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onEsqueciSenha(email) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Entrar
            Button(
                onClick = { onLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.loginDarkBlue
                )
            ) {
                Text(
                    text = "Entrar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Entrar com Google
            OutlinedButton(
                onClick = { onLoginGoogle() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, colors.loginBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 0.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Entrar com Google",
                    fontSize = 14.sp,
                    color = colors.loginDarkBlue,
                    fontWeight = FontWeight.Medium,

                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Não tem uma conta? Cadastre-se
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Não tem uma conta? ",
                    fontSize = 13.sp,
                    color = colors.loginGrayText
                )
                Text(
                    text = "Cadastre-se",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    color = colors.loginDarkBlue,
                    modifier = Modifier.clickable { onRegister() }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ComunicacaoEscolarTheme {
        LoginScreen()
    }
}
