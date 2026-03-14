package dev.fslab.comunicacao.escolar.ui.theme.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import dev.fslab.comunicacao.escolar.R
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.Poppins

@Composable
fun MuralScreen() {
	val background = Color(0xFFF4F4F5)
	val textPrimary = Color(0xFF000000)
	val textSecondary = Color(0xFF40484C)
	val likeColor = Color(0xFF40484C)

	var isLiked by remember { mutableStateOf(false) }
	var likeCount by remember { mutableIntStateOf(24) }
	val context = LocalContext.current
	val muralImageResId = remember {
		context.resources.getIdentifier("mural_ciencias", "drawable", context.packageName)
	}

	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as Activity).window
			window.statusBarColor = background.toArgb()
			window.navigationBarColor = background.toArgb()
			val insetsController = WindowCompat.getInsetsController(window, view)
			insetsController.isAppearanceLightStatusBars = true
			insetsController.isAppearanceLightNavigationBars = true
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(background)
			.statusBarsPadding()
			.navigationBarsPadding()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 24.dp)
	) {
		Text(
			text = "Mural",
			color = textPrimary,
			style = MaterialTheme.typography.headlineLarge.copy(
				fontFamily = Poppins,
				fontWeight = FontWeight.SemiBold,
				fontSize = 18.sp
			),
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 10.dp)
		)

		Spacer(modifier = Modifier.height(22.dp))

		Row(verticalAlignment = Alignment.CenterVertically) {
			Image(
				painter = painterResource(id = R.drawable.avatar_neo),
				contentDescription = "Avatar do professor",
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.size(44.dp)
					.clip(CircleShape)
			)

			Column(modifier = Modifier.padding(start = 12.dp)) {
				Text(
					text = "Prof. Anderson",
					color = textPrimary,
					style = MaterialTheme.typography.bodyMedium.copy(
						fontFamily = Poppins,
						fontWeight = FontWeight.SemiBold,
						fontSize = 14.sp
					)
				)
				Text(
					text = "Escola das Flores • 2h atrás",
					color = textSecondary,
					style = MaterialTheme.typography.bodySmall.copy(
						fontFamily = Poppins,
						fontWeight = FontWeight.Normal,
						fontSize = 12.sp
					)
				)
			}
		}

		Spacer(modifier = Modifier.height(18.dp))

		Text(
			text = "Feira de Ciências Anual 2026",
			color = textPrimary,
			style = MaterialTheme.typography.titleLarge.copy(
				fontFamily = Poppins,
				fontWeight = FontWeight.SemiBold,
				fontSize = 16.sp
			)
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "Junte-se a nós para um dia de inovação e descoberta! Alunos de todas as turmas estarão apresentando seus projetos incríveis no salão principal. Venha prestigiar o talento dos nossos futuros cientistas!",
			color = textSecondary,
			style = MaterialTheme.typography.bodyMedium.copy(
				fontFamily = Poppins,
				fontWeight = FontWeight.Normal,
				fontSize = 14.sp
			)
		)

		Spacer(modifier = Modifier.height(22.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(4f / 3f)
				.clip(RoundedCornerShape(36.dp)),
			contentAlignment = Alignment.Center
		) {
			if (muralImageResId != 0) {
				Image(
					painter = painterResource(id = muralImageResId),
					contentDescription = "Imagem da publicação",
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			} else {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(
							brush = Brush.radialGradient(
								colors = listOf(Color(0xFF5D6672), Color(0xFF0F1621), Color(0xFF050A13)),
								radius = 900f
							)
						),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Filled.Science,
						contentDescription = "Ilustração da publicação",
						tint = Color(0xFF27E4E2),
						modifier = Modifier.size(92.dp)
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(18.dp))

		Row(verticalAlignment = Alignment.CenterVertically) {
			IconButton(
				onClick = {
					isLiked = !isLiked
					likeCount += if (isLiked) 1 else -1
				},
				modifier = Modifier.size(34.dp)
			) {
				Icon(
					imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
					contentDescription = "Curtidas",
					tint = likeColor,
					modifier = Modifier.size(28.dp)
				)
			}
			Text(
				text = likeCount.toString(),
				color = likeColor,
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.padding(start = 10.dp)
			)
		}

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MuralScreenPreview() {
	ComunicacaoEscolarTheme {
		MuralScreen()
	}
}
