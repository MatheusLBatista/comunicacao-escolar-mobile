package dev.fslab.comunicacao.escolar.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarTheme
// Assumindo que LightComunicacaoEscolarColors existe no seu projeto,
// mas usarei cores hardcoded onde necessário para garantir a fidelidade visual da imagem.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mural",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            MuralBottomBar()
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                PostCard(
                    modifier = Modifier.padding(horizontal = 24.dp), // Margem lateral maior como na imagem
                    userName = "Mr. Anderson",
                    userRole = "Science Teacher",
                    timeAgo = "2h ago",
                    postTitle = "Annual Science Fair 2024",
                    postDescription = "Join us for a day of innovation and discovery! Students from all grades will showcase their amazing projects in the main hall.",
                    likes = 24,
                    onLikeClick = {}
                )
            }

            // Exemplo extra para ver a lista rolando
            item {
                PostCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    userName = "Sarah Mitchell",
                    userRole = "Math Teacher",
                    timeAgo = "5h ago",
                    postTitle = "Math Olympics Results",
                    postDescription = "The results are in! Check the attached PDF for the list of winners.",
                    likes = 42,
                    onLikeClick = {}
                )
            }
        }
    }
}

@Composable
private fun PostCard(
    modifier: Modifier = Modifier,
    userName: String,
    userRole: String,
    timeAgo: String,
    postTitle: String,
    postDescription: String,
    likes: Int,
    onLikeClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // --- Header do Usuário ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Simulado com imagem ou ícone colorido)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2F1)), // Um verde claro sutil
                contentAlignment = Alignment.Center
            ) {
                // Idealmente aqui iria um Image(painter = ...)
                Icon(
                    imageVector = Icons.Filled.Person, // Usando Person como placeholder do rosto
                    contentDescription = null,
                    tint = Color(0xFF26A69A),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$userRole • $timeAgo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Texto do Post ---
        Text(
            text = postTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = postDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666), // Cinza escuro para leitura
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Mídia / Anexo (O retângulo escuro da imagem) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f) // Proporção retangular
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1C1E21)), // Cor de fundo bem escura (quase preto)
            contentAlignment = Alignment.Center
        ) {
            // O retângulo interno mais claro
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(100.dp)
                    .border(1.dp, Color(0xFF33363A), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF25282B)), // Um cinza chumbo levemente mais claro
                contentAlignment = Alignment.Center
            ) {
                // Ícone de "Arquivo" ou "Prancheta"
                Icon(
                    imageVector = Icons.Outlined.DateRange, // Similar ao ícone de calendário/prancheta
                    contentDescription = null,
                    tint = Color(0xFF80DEEA), // O Ciano brilhante da imagem
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Rodapé (Likes e Paginação) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coração e contador
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = likes.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Paginação (Dots) centralizados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) Color.Gray else Color.LightGray)
                )
            }
        }
    }
}

@Composable
fun MuralBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val iconColor = Color.Black

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.Email, contentDescription = "Mail", tint = iconColor) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat", tint = iconColor) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Likes", tint = iconColor) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = iconColor) }
        )
        NavigationBarItem(
            selected = true, // Indicador de perfil ativo (como na imagem parece ter um foco na direita)
            onClick = {},
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = iconColor) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MuralScreenPreview() {
    ComunicacaoEscolarTheme {
        MuralScreen()
    }
}