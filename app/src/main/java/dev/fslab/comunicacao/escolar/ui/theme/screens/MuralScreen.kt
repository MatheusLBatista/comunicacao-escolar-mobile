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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.comunicacao.escolar.R
import dev.fslab.comunicacao.escolar.model.MuralResponse
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.Poppins
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralState
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralViewModel

@Composable
fun MuralScreen(
	schoolId: String = "",
	muralViewModel: MuralViewModel = viewModel(),
	authViewModel: AuthViewModel = viewModel()
) {
	val background = Color(0xFFF4F4F5)
	val textPrimary = Color(0xFF000000)
	val textSecondary = Color(0xFF40484C)
	val likeColor = Color(0xFF40484C)

	val muralState by muralViewModel.muralState.collectAsState()
	val posts by muralViewModel.posts.collectAsState()
	val currentUser by authViewModel.currentUser.collectAsState()

	val context = LocalContext.current
	val view = LocalView.current

	var isLiked by remember { mutableStateOf(false) }
	var likeCount by remember { mutableIntStateOf(24) }
	val muralImageResId = remember {
		context.resources.getIdentifier("mural_ciencias", "drawable", context.packageName)
	}


	LaunchedEffect(currentUser) {
		currentUser?.schoolId?.let  {
			schoolId -> muralViewModel.getPosts(schoolId)
		}
	}
	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as Activity).window
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

		when(muralState) {
			MuralState.Idle -> {
				Text("Carregando posts...")
			}
			MuralState.Loading -> {
				CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
			}
			is MuralState.Error -> {
				Text(
					text = (muralState as MuralState.Error).message,
					color = Color.Red,
					modifier = Modifier.padding(16.dp)
				)
			}
			is MuralState.Success -> {
				if(posts.isEmpty()) {
					Text("Nenhum post encontrado")
				} else {
					LazyColumn {
						itemsIndexed(posts) { index,
							post ->
							MuralPostCard(post)
							Spacer(modifier = Modifier.height(16.dp))
						}
					}
				}
			}
		}
	}
}

@Composable
fun MuralPostCard(post: MuralResponse) {
	Column(modifier = Modifier
		.fillMaxWidth()
		.background(Color.White, RoundedCornerShape(12.dp))
		.padding(16.dp)
	){
		Text(
			text = post.title,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.SemiBold
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = post.content,
			style = MaterialTheme.typography.bodySmall,
			color = Color.Gray
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "Público: ${post.target.scope}",
			style = MaterialTheme.typography.labelSmall
		)
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MuralScreenPreview() {
	ComunicacaoEscolarTheme {
		MuralScreen()
	}
}
