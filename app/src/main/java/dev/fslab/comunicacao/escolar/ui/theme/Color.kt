package dev.fslab.comunicacao.escolar.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PALETA DE CORES DA APLICAÇÃO
 *
 * As cores são organizadas em:
 * 1. Cores Primárias - Cores principais da marca (azul)
 * 2. Cores Secundárias - Cores de suporte (cinza)
 * 3. Cores de Texto - Para diferentes níveis de hierarquia
 * 4. Cores de Estado - Erro, sucesso, alerta
 * 5. Cores de Superfície - Fundos e cards
 * 6. Cores de Compatibilidade - Para Material Design 3
 *
 * Usando uma paleta de cores consistente, toda a aplicação
 * fica visualmente harmônica e profissional.
 */



// Cores Primárias - Roxo/Violeta (Cor principal da marca)

val PrimaryBlue = Color(0xFF7C6AF6)        // Roxo vibrante - botões principais
val PrimaryBlueDark = Color(0xFF5B4CCF)    // Roxo escuro - estado pressionado
val PrimaryLightBlue = Color(0xFFF5F3FF)   // Roxo claro - fundos e backgrounds



// Cores de Texto

val TextPrimary = Color(0xFF111827)        // Texto principal - títulos e labels
val TextSecondary = Color(0xFF6B7280)      // Texto secundário - descrições
val TextTertiary = Color(0xFF4B5563)       // Texto terciário - links e detalhes
val TextOnPrimary = Color.White            // Texto sobre fundo primário (azul)
val TextBlack = Color.Black                // Texto preto - campos de input



// Cores Secundárias - Cinzas (Textos e bordas)

val SecondaryGray = Color(0xFF4B5563)      // Cinza para textos secundários
val LightGray = Color(0xFFF3F4F6)          // Cinza muito claro - fundos
val MediumGray = Color(0xFF9CA3AF)         // Cinza médio - placeholder/desabilitado
val DarkGray = Color(0xFF6B7280)           // Cinza escuro - descrições
val BorderGray = Color(0xFFE5E7EB)         // Cinza para bordas
val InputBorderGray = Color(0xFFD4D0E8)    // Cinza com tinta roxa para bordas de inputs
val IconGray = Color(0xFF374151)           // Cinza para ícones



// Cores de Superfície

val SurfaceWhite = Color.White             // Fundo de cards
val SurfaceLight = Color(0xFFEDE9FE)       // Fundo gradiente secundário (violeta claro)
val BackgroundGradientStart = PrimaryLightBlue  // Início do gradiente de fundo
val BackgroundGradientEnd = Color(0xFFEDE9FE)   // Fim do gradiente de fundo (violeta claro)



// Cores de Estado - Erro

val ErrorBackground = Color(0xFFFFEBEE)    // Fundo vermelho claro para erros
val ErrorText = Color(0xFFC62828)          // Texto vermelho para erros
val ErrorButton = Color(0xFFEF4444)        // Botão vermelho (logout, cancelar)



// Cores de Estado - Sucesso

val SuccessBackground = Color(0xFFE8F5E9)  // Fundo verde claro para sucesso
val SuccessText = Color(0xFF2E7D32)        // Texto verde para sucesso



// Cores para Dashboard Admin Geral

val CardBlue = Color(0xFF7C6AF6)           // Card roxo - Instituições Ativas
val CardOrange = Color(0xFFD97706)         // Card laranja/amarelo - Pendentes Aprovação
val CardGreen = Color(0xFF16A34A)          // Card verde - Filas Ativas
val CardYellow = Color(0xFFFBBF24)         // Card amarelo - Aguardando
val CardPurple = Color(0xFFA855F7)         // Card roxo - Senhas Emitidas
val WarningOrange = Color(0xFFFBBF24)      // Ícone de alerta laranja
val StatusOperational = Color(0xFF22C55E)  // Status operacional verde



// Cores da LoginScreen

val DarkBlue = Color(0xFF283543)           // Azul escuro - títulos e botão principal
val LoginBackground = Color(0xFFFFFFFF)    // Fundo branco da tela de login
val LoginBorder = Color(0xFFDDDDDD)        // Bordas dos campos de input
val LoginGrayText = Color(0xFF7F8C8D)      // Texto cinza - subtítulos e placeholders
val FooterGray = Color(0xFFBDC3C7)         // Texto do rodapé
val GoogleRed = Color(0xFFDB4437)          // Cor do ícone Google



// Cores de Compatibilidade com Material Design 3

val Purple80 = Color(0xFFD0BCFF)           // Para tema escuro
val PurpleGrey80 = Color(0xFFCCC2DC)       // Para tema escuro
val Pink80 = Color(0xFFEFB8C8)             // Para tema escuro

val Purple40 = Color(0xFF6650A4)           // Para tema claro
val PurpleGrey40 = Color(0xFF625B71)       // Para tema claro
val Pink40 = Color(0xFF7D5260)             // Para tema claro
