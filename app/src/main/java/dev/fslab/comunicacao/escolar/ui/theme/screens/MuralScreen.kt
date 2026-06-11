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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.fslab.comunicacao.escolar.R
import dev.fslab.comunicacao.escolar.ui.components.AppHeader
import dev.fslab.comunicacao.escolar.model.MuralResponse
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
import dev.fslab.comunicacao.escolar.ui.viewmodel.AuthViewModel
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralState
import dev.fslab.comunicacao.escolar.ui.viewmodel.MuralViewModel
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.viewmodel.LikeState
import dev.fslab.comunicacao.escolar.ui.viewmodel.LikeViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.fslab.comunicacao.escolar.model.ApiUser
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.util.DateUtils
import dev.fslab.comunicacao.escolar.model.Docs

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.derivedStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralScreen(
	schoolId: String = "",
	muralViewModel: MuralViewModel = viewModel(),
	authViewModel: AuthViewModel = viewModel(),
	navController: NavController? = null
) {
	val colors = LocalComunicacaoEscolarColors.current
	val muralState by muralViewModel.muralState.collectAsState()
	val posts by muralViewModel.posts.collectAsState()
	val currentUser by authViewModel.currentUser.collectAsState()

	val view = LocalView.current
	val listState = rememberLazyListState()
	var isRefreshing by remember { mutableStateOf(false) }

	val isAtBottom by remember {
		derivedStateOf {
			val layoutInfo = listState.layoutInfo
			val visibleItemsInfo = layoutInfo.visibleItemsInfo
			if (layoutInfo.totalItemsCount == 0) {
				false
			} else {
				val lastVisibleItem = visibleItemsInfo.lastOrNull()
				(lastVisibleItem?.index ?: 0) >= layoutInfo.totalItemsCount - 1
			}
		}
	}

	LaunchedEffect(Unit) {
		muralViewModel.clearError()
	}

	LaunchedEffect(currentUser?.schoolId) {
		currentUser?.schoolId?.let { id ->
			muralViewModel.getPosts(id)
		}
	}

	LaunchedEffect(isAtBottom, posts?.data?.docs?.size) {
		if (isAtBottom && muralState is MuralState.Success) {
			currentUser?.schoolId?.let { id ->
				muralViewModel.getPosts(id, loadMore = true)
			}
		}
	}

	LaunchedEffect(muralState) {
		if (muralState !is MuralState.Loading) {
			isRefreshing = false
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
	) {
		AppHeader("Mural")

		Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
			when (muralState) {
				MuralState.Idle -> {
					Text("Carregando posts...", modifier = Modifier.align(Alignment.Center))
				}
				MuralState.Loading -> {
					if (!isRefreshing) {
						CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
					}
				}
				is MuralState.Error -> {
					Text(
						text = (muralState as MuralState.Error).message,
						color = Color.Red,
						modifier = Modifier.padding(16.dp).align(Alignment.Center)
					)
				}
				is MuralState.Success -> {
					val docs = posts?.data?.docs ?: emptyList()
					if (docs.isEmpty()) {
						Text("Nenhum post encontrado", modifier = Modifier.align(Alignment.Center))
					} else {
						PullToRefreshBox(
							isRefreshing = isRefreshing,
							onRefresh = {
								isRefreshing = true
								currentUser?.schoolId?.let { id ->
									muralViewModel.getPosts(id, forceRefresh = true)
								}
							},
							modifier = Modifier.fillMaxSize()
						) {
							LazyColumn(
								state = listState,
								modifier = Modifier.fillMaxSize()
							) {
								itemsIndexed(docs) { index, post ->
									MuralPostCard(post, currentUser = currentUser!!)
									Spacer(modifier = Modifier.height(16.dp))
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
fun PostAttachments(
    attachments: List<String>,
    muralViewModel: MuralViewModel = viewModel()
) {
	if (attachments.isEmpty()) return

	val context = LocalContext.current
	var showLightbox by remember { mutableStateOf(false) }
	var initialPageIndex by remember { mutableIntStateOf(0) }
	
	val token = dev.fslab.comunicacao.escolar.network.TokenManager.getAccessToken()

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		val visibleCount = attachments.size.coerceAtMost(4)

		when (visibleCount) {
			1 -> {
				AttachmentItem(attachments[0], 200.dp, token) {
					initialPageIndex = 0
					showLightbox = true
				}
			}
			2 -> {
				Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
					Box(modifier = Modifier.weight(1f)) {
						AttachmentItem(attachments[0], 150.dp, token) { initialPageIndex = 0; showLightbox = true }
					}
					Box(modifier = Modifier.weight(1f)) {
						AttachmentItem(attachments[1], 150.dp, token) { initialPageIndex = 1; showLightbox = true }
					}
				}
			}
			3 -> {
				Row(modifier = Modifier.fillMaxWidth().height(200.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
					Box(modifier = Modifier.weight(1.5f)) {
						AttachmentItem(attachments[0], 200.dp, token) { initialPageIndex = 0; showLightbox = true }
					}
					Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
						Box(modifier = Modifier.weight(1f)) {
							AttachmentItem(attachments[1], 98.dp, token) { initialPageIndex = 1; showLightbox = true }
						}
						Box(modifier = Modifier.weight(1f)) {
							AttachmentItem(attachments[2], 98.dp, token) { initialPageIndex = 2; showLightbox = true }
						}
					}
				}
			}
			else -> { // 4 ou mais
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
						Box(modifier = Modifier.weight(1f)) {
							AttachmentItem(attachments[0], 120.dp, token) { initialPageIndex = 0; showLightbox = true }
						}
						Box(modifier = Modifier.weight(1f)) {
							AttachmentItem(attachments[1], 120.dp, token) { initialPageIndex = 1; showLightbox = true }
						}
					}
					Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
						Box(modifier = Modifier.weight(1f)) {
							AttachmentItem(attachments[2], 120.dp, token) { initialPageIndex = 2; showLightbox = true }
						}
						Box(modifier = Modifier.weight(1f)) {
							AttachmentOverlayItem(attachments[3], 120.dp, attachments.size - 4, token) { 
								initialPageIndex = 3
								showLightbox = true 
							}
						}
					}
				}
			}
		}
	}

	if (showLightbox) {
		val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { attachments.size })
		
		Dialog(
			onDismissRequest = { showLightbox = false },
			properties = DialogProperties(usePlatformDefaultWidth = false)
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Color.Black.copy(alpha = 0.95f))
			) {
				HorizontalPager(
					state = pagerState,
					modifier = Modifier.fillMaxSize(),
					pageSpacing = 16.dp
				) { pageIndex ->
					Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						val attachmentUrl = "${RetrofitClient.BASE_URL}attachments/${attachments[pageIndex]}"
						SubcomposeAsyncImage(
							model = ImageRequest.Builder(context)
								.data(attachmentUrl)
								.addHeader("Authorization", "Bearer $token")
								.crossfade(true)
								.build(),
							contentDescription = "Imagem expandida",
							modifier = Modifier.fillMaxSize().clickable { showLightbox = false },
							contentScale = ContentScale.Fit,
							loading = {
								Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
									CircularProgressIndicator(color = Color.White)
								}
							}
						)
					}
				}

				Text(
					text = "${pagerState.currentPage + 1} / ${attachments.size}",
					color = Color.White,
					modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
					style = MaterialTheme.typography.bodyMedium
				)

				IconButton(
					onClick = { showLightbox = false },
					modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
				) {
					Icon(
						painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
						contentDescription = "Fechar",
						tint = Color.White
					)
				}
			}
		}
	}
}

@Composable
fun AttachmentItem(
    attachmentId: String,
    height: androidx.compose.ui.unit.Dp,
    token: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
	val attachmentUrl = "${RetrofitClient.BASE_URL}attachments/$attachmentId"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(attachmentUrl)
				.addHeader("Authorization", "Bearer $token")
				.crossfade(true)
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize(),
			loading = {
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
				}
			},
			error = {
				Icon(
					painter = painterResource(id = android.R.drawable.ic_menu_report_image),
					contentDescription = "Erro ao carregar",
					tint = Color.Gray
				)
			}
		)
    }
}

@Composable
fun AttachmentOverlayItem(
    attachmentId: String,
    height: androidx.compose.ui.unit.Dp,
    remainingCount: Int,
    token: String?,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        AttachmentItem(attachmentId, height, token, onClick)
        if (remainingCount > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remainingCount",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MuralPostCard(
	post: Docs,
	likeViewModel: LikeViewModel = viewModel(key = post.id),
	muralViewModel: MuralViewModel = viewModel(),
	currentUser: User
) {
	val colors = LocalComunicacaoEscolarColors.current
	val context = LocalContext.current

	val likeState by likeViewModel.likeState.collectAsState()
	val authors by muralViewModel.authors.collectAsState()
	
	var isLiked by remember { mutableStateOf(post.userLiked?.contains(currentUser.id) == true) }
	var likesCount by remember { mutableIntStateOf(post.likesCount ?: 0) }
	
	val author = authors[post.authorId]

	LaunchedEffect(post.authorId) {
		muralViewModel.fetchAuthor(post.authorId)
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
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(bottom = 12.dp)
		) {
			val avatarModifier = Modifier
				.size(32.dp)
				.clip(CircleShape)
				.background(Color.LightGray)

			if (author?.avatarUrl != null) {
				val avatarUrl = author.avatarUrl.replace("localhost", "10.0.2.2")
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(avatarUrl)
						.addHeader("Authorization", "Bearer ${dev.fslab.comunicacao.escolar.network.TokenManager.getAccessToken()}")
						.crossfade(true)
						.build(),
					contentDescription = "Avatar de ${author.fullName}",
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
			
			Column {
				Text(
					text = author?.fullName ?: "Carregando...",
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Medium,
					color = colors.textPrimary
				)
				val timeAgo = DateUtils.getTimeAgo(post.createdAt)
				if (timeAgo.isNotEmpty()) {
					Text(
						text = timeAgo,
						style = MaterialTheme.typography.labelSmall,
						color = Color.Gray
					)
				}
			}
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
		
		PostAttachments(post.attachments, muralViewModel)

		Spacer(modifier = Modifier.height(8.dp))
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
