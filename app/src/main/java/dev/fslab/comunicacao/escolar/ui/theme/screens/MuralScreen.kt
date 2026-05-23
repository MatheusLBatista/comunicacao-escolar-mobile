package dev.fslab.comunicacao.escolar.ui.theme.screens

import android.app.Activity
import android.util.Log
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.navigation.compose.rememberNavController
import dev.fslab.comunicacao.escolar.R
import dev.fslab.comunicacao.escolar.model.Docs
import dev.fslab.comunicacao.escolar.model.MuralResponse
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.theme.Poppins
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralState
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralViewModel
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.navigation.Screen
import dev.fslab.comunicacao.escolar.navigation.navigateSafely
import dev.fslab.comunicacao.escolar.ui.viewmodel.LikeState
import dev.fslab.comunicacao.escolar.ui.viewmodel.LikeViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.ApiUser
import dev.fslab.comunicacao.escolar.network.RetrofitClient

@Composable
fun MuralScreen(
	schoolId: String = "",
	muralViewModel: MuralViewModel = viewModel(),
	authViewModel: AuthViewModel = viewModel(),
	navController: NavController? = null
) {

	val background = Color(0xFFF4F4F5)
	val textPrimary = Color(0xFF000000)
	val textSecondary = Color(0xFF40484C)
	val likeColor = Color(0xFF40484C)

	val colors = LocalComunicacaoEscolarColors.current

	val muralState by muralViewModel.muralState.collectAsState()
	val posts by muralViewModel.posts.collectAsState()
	val currentUser by authViewModel.currentUser.collectAsState()

	val context = LocalContext.current
	val view = LocalView.current

	val snackbarHostState = remember { SnackbarHostState() }
	var isLiked by remember { mutableStateOf(false) }
	var likeCount by remember { mutableIntStateOf(24) }
	val muralImageResId = remember {
		context.resources.getIdentifier("mural_ciencias", "drawable", context.packageName)
	}

	LaunchedEffect(Unit) {
		muralViewModel.clearError()
	}


	LaunchedEffect(currentUser?.schoolId) {
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
			.background(colors.background)
			.statusBarsPadding()
			.navigationBarsPadding()
			.padding(horizontal = 24.dp)
	) {
		Text(
			text = "Mural",
			color = colors.textPrimary,
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
				if(posts!!.data.docs.isEmpty()) {
					Text("Nenhum post encontrado")
				} else {
					LazyColumn {
						itemsIndexed(posts!!.data.docs) { index,
							post ->
							MuralPostCard(post, currentUser = currentUser!!)
							Spacer(modifier = Modifier.height(16.dp))
						}
					}
				}
			}
		}
	}
}

@Composable
fun PostAttachments(attachments: List<String>) {
	if (attachments.isEmpty()) return

	val context = LocalContext.current
	
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		attachments.forEach { url ->
			// Corrige localhost para 10.0.2.2 caso esteja rodando no emulador
//			val formattedUrl = url.replace("localhost", "10.0.2.2")
			
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(200.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(Color.LightGray.copy(alpha = 0.3f)),
				contentAlignment = Alignment.Center
			) {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(url)
						.crossfade(true)
						.build(),
					contentDescription = "Imagem do post",
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
					loading = {
						CircularProgressIndicator(
							modifier = Modifier.size(24.dp),
							strokeWidth = 2.dp,
							color = MaterialTheme.colorScheme.primary
						)
					},
					error = {
						Icon(
							painter = painterResource(id = android.R.drawable.ic_menu_report_image),
							contentDescription = "Erro ao carregar imagem",
							tint = Color.Gray
						)
					}
				)
			}
		}
	}
}

@Composable
fun MuralPostCard(
	post: Docs,
	likeViewModel: LikeViewModel = viewModel(key = post.id),
	currentUser: User
) {
	val colors = LocalComunicacaoEscolarColors.current
	val context = LocalContext.current

	val likeState by likeViewModel.likeState.collectAsState()
	
	var isLiked by remember { mutableStateOf(post.userLiked?.contains(currentUser.id) == true) }
	var likesCount by remember { mutableIntStateOf(post.likesCount ?: 0) }
	var author by remember { mutableStateOf<ApiUser?>(null) }

	LaunchedEffect(post.authorId) {
		if (post.authorId.isNotEmpty()) {
			try {
				val response = RetrofitClient.userApi.getById(post.authorId)
				if (response.isSuccess()) {
					author = response.data
				}
			} catch (e: Exception) {
				Log.e("MuralPostCard", "Erro ao carregar autor: ${e.message}")
			}
		}
	}

	LaunchedEffect(likeState) {
		if (likeState is LikeState.Success) {
			val response = (likeState as LikeState.Success).like
			val newlyLiked = response.data?.id != null
			
			if (newlyLiked != isLiked) {
				isLiked = newlyLiked
				if (newlyLiked) {
					likesCount++
				} else {
					if (likesCount > 0) likesCount--
				}
			}
		}
	}

	Column(modifier = Modifier
		.fillMaxWidth()
		.background(colors.background, RoundedCornerShape(12.dp))
		.padding(16.dp)
	){
		// Seção do Autor
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(bottom = 12.dp)
		) {
			val avatarModifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(Color.LightGray)

			if (author?.avatarUrl != null) {
				val avatarUrl = author?.avatarUrl?.replace("localhost", "10.0.2.2")
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(avatarUrl)
						.crossfade(true)
						.build(),
					contentDescription = "Avatar de ${author?.fullName}",
					modifier = avatarModifier,
					contentScale = ContentScale.Crop,
					error = painterResource(id = android.R.drawable.ic_menu_report_image),
					placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
				)
			} else {
				Icon(
					imageVector = Icons.Default.AccountCircle,
					contentDescription = "Avatar Padrão",
					modifier = avatarModifier,
					tint = Color.Gray
				)
			}
			
			Spacer(modifier = Modifier.width(8.dp))
			
			Text(
				text = author?.fullName ?: "Carregando...",
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.Medium,
				color = colors.textPrimary
			)
		}

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
		
		// Anexos de Imagem
		PostAttachments(post.attachments)

		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "Público: ${post.target.scope}",
			style = MaterialTheme.typography.labelSmall
		)
		Spacer(modifier = Modifier.height(24.dp))
		Row (
			modifier = Modifier.clickable {
				if (likeState !is LikeState.Loading) {
					likeViewModel.postLike(post.id)
				}
			},
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon (
				imageVector = if(isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
				contentDescription = "Like",
				tint = Color.Red,
				modifier = Modifier.size(24.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "$likesCount",
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold
			)
			
			if (likeState is LikeState.Loading) {
				Spacer(modifier = Modifier.width(8.dp))
				CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
			}
		}
		
		if (likeState is LikeState.Error) {
			Text(
				text = (likeState as LikeState.Error).message,
				color = Color.Red,
				style = MaterialTheme.typography.labelSmall,
				modifier = Modifier.padding(top = 8.dp)
			)
		}
	}
}
