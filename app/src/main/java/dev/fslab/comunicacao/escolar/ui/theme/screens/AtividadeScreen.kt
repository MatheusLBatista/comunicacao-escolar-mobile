package dev.fslab.comunicacao.escolar.ui.theme.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import dev.fslab.comunicacao.escolar.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.LightComunicacaoEscolarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtividadeScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onBackClick: () -> Unit = {}

){
    val colors = LightComunicacaoEscolarColors
    
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.atividadeBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Atividades",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Neo - 24/03/2026",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Presença",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Entrada às 8h15",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Humor",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Muito energético e brincalhão esta manhã; participou ativamente das brincadeiras em grupo.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Café da Manhã",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Comeu todo o mingau de aveia e meia banana.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Almoço",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Comeu bem. Arroz, feijão, carne bovina e salada.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Soneca",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Dormiu das 13h00 às 14h30 sem interrupções.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Lanche",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Fatias de maçã e iogurte.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Banho",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                Text (
                    text = "Tomou banho 2 vezes pois brincou na areia.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top=8.dp, start = 16.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                horizontalAlignment = Alignment.Start
            ){
                Text(
                    text = "Anexos",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp)
                )
                val anexos = listOf(
                    R.drawable.avatar_leo,
                    R.drawable.avatar_neo
                )
                AnexosCarousel(
                    images = anexos,
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                )
            }

        }
    }
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .imePadding(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box()
//    }
//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnexosCarousel(
    images: List<Int>,
    modifier: Modifier = Modifier
){
    val pagerState = rememberPagerState(pageCount = {images.size})
    Column(
        modifier = modifier.fillMaxWidth()
    ){
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            page ->
            Image(
                painter = painterResource(id=images[page]),
                contentDescription = "Anexo ${page + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Spacer(modifier = Modifier
            .size(8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) {
                index ->
                val selected = pagerState.currentPage == index
                Box (
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if(selected) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (selected) Color.Black else Color.LightGray)
                )
            }
        }
    }
}