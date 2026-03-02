package dev.fslab.comunicacao.escolar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.R

/**
 * Poppins - Família de fontes do projeto
 */
val Poppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

/**
 * Typography - Definição de todos os estilos de texto da aplicação
 *
 * Em vez de definir fonte, tamanho, peso e espaçamento em cada Text(),
 * criamos estilos reutilizáveis. Assim, toda a aplicação usa a mesma
 * tipografia de forma consistente.
 *
 * Hierarquia de tamanhos:
 * Display (maior) > Headline > Title > Body > Label (menor)
 *
 * Uso:
 * - display*: Títulos muito grandes (páginas principais)
 * - headline*: Títulos menores (seções)
 * - title*: Cabeçalhos (cards, botões)
 * - body*: Texto principal da aplicação
 * - label*: Rótulos pequenos (badges, tags)
 */
val Typography = Typography(

    // DISPLAY - Títulos muito grandes
    // Uso: Títulos principais de páginas

    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,        // Muito destaque
        fontSize = 32.sp,                    // Tamanho muito grande
        lineHeight = 40.sp,                  // Distância entre linhas
        letterSpacing = 0.sp                 // Sem espaço extra entre letras
    ),
    displayMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),


    // HEADLINE - Títulos menores
    // Uso: Títulos de seções, subtítulos principais

    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,        // Muito destaque
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,    // Destaque moderado
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Poppins,     // Família de fonte padrão do sistema
        fontWeight = FontWeight.SemiBold,    // Peso da fonte para dar mais destaque
        fontSize = 16.sp,
        lineHeight = 24.sp,                  // Distância entre as linhas
        letterSpacing = 0.sp                 // Espaçamento entre as letras
    ),


    // TITLE - Cabeçalhos de cards e elementos
    // Uso: Títulos de cards, cabeçalhos de seções pequenas

    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp               // Espaço ligeiramente maior entre letras
    ),
    titleSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,      // Destaque leve
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),


    // BODY - Texto principal da aplicação
    // Uso: Parágrafos, descrições, conteúdo principal

    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,      // Sem destaque
        fontSize = 16.sp,                    // Legível
        lineHeight = 24.sp,                  // Espaçamento confortável
        letterSpacing = 0.5.sp               // Pequeno espaço entre letras
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),


    // LABEL - Rótulos pequenos
    // Uso: Badges, tags, labels em botões

    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,    // Um pouco de destaque
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)