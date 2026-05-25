package dev.fslab.comunicacao.escolar.ui.screens.admin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner  // lifecycle-runtime-compose não incluído; usar este até adicionar a dependência
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.ui.theme.LocalComunicacaoEscolarColors
import dev.fslab.comunicacao.escolar.ui.viewmodel.PortariaState
import dev.fslab.comunicacao.escolar.ui.viewmodel.PortariaViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PortariaQrScanScreen(
    user: User,
    onBack: () -> Unit,
    viewModel: PortariaViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val s = state) {
            is PortariaState.Scanning -> {
                if (hasCameraPermission) {
                    ScannerView(
                        onBack = onBack,
                        onQrDetected = { viewModel.onQrDetected(it) }
                    )
                } else {
                    NoCameraPermission(colors = colors, onBack = onBack, onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    })
                }
            }

            is PortariaState.Loading, is PortariaState.Confirming -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (s is PortariaState.Confirming) "Registrando saída..." else "Buscando autorização...",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is PortariaState.Detail -> {
                AuthorizationDetailView(
                    doc = s.doc,
                    isValid = s.isValid,
                    invalidReason = s.invalidReason,
                    onBack = { viewModel.resetToScanning() },
                    onConfirm = { viewModel.confirmarSaida(s.doc, user) }
                )
            }

            is PortariaState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = s.message,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ActionButton(
                            text = "Escanear novamente",
                            colors = colors,
                            onClick = { viewModel.resetToScanning() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Voltar",
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBack() }
                        )
                    }
                }
            }

            is PortariaState.Success -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Saída registrada!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${s.studentName} foi liberado(a) com sucesso.",
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        ActionButton(
                            text = "Escanear próximo",
                            colors = colors,
                            onClick = { viewModel.resetToScanning() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Voltar",
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerView(
    onBack: () -> Unit,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy ->
                                processImageProxy(proxy, scanner) { raw ->
                                    onQrDetected(raw)
                                }
                            }
                        }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanner overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Dark overlay with transparent center would require Canvas; use semi-transparent overlay instead
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            )
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
            Text(
                text = "Escanear QR Code",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        // Bottom hint
        Text(
            text = "Posicione o QR Code dentro do quadro",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.let { onDetected(it) }
        }
        .addOnCompleteListener { imageProxy.close() }
}

@Composable
private fun AuthorizationDetailView(
    doc: AutorizacaoSaidaDoc,
    isValid: Boolean,
    invalidReason: String?,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalComunicacaoEscolarColors.current
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")) }
    val isoFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }

    fun formatDate(iso: String): String =
        runCatching { isoFormatter.parse(iso)?.let { dateFormatter.format(it) } }.getOrNull() ?: iso

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = colors.textPrimary
                )
            }
            Text(
                text = "Autorização de Saída",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status badge
            val (badgeBg, badgeText, badgeLabel) = if (isValid)
                Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "Autorização válida")
            else
                Triple(Color(0xFFFFEBEE), Color(0xFFC62828), invalidReason ?: "Autorização inválida")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(text = badgeLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = badgeText)
            }

            // Student info
            Text(
                text = "ALUNO",
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.lightGray),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = doc.student?.avatarUrl
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(avatarUrl).crossfade(true).build(),
                            contentDescription = doc.student?.fullName,
                            modifier = Modifier.size(48.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = colors.iconGray, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = doc.student?.fullName ?: "—",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.lightGray))

            // Authorized person
            Text(
                text = "PESSOA AUTORIZADA",
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val person = doc.authorizedPerson
                if (!person?.photoUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.lightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(person?.photoUrl).crossfade(true).build(),
                            contentDescription = person?.name,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                DetailRow(label = "Nome", value = person?.name ?: "—", colors = colors)
                DetailRow(label = "Documento", value = person?.document ?: "—", colors = colors)
                DetailRow(label = "Parentesco", value = person?.relationship ?: "—", colors = colors)
            }

            // Validity
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "Válido de", value = formatDate(doc.validFrom), colors = colors)
                DetailRow(label = "Válido até", value = formatDate(doc.validUntil), colors = colors)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isValid) {
                ActionButton(
                    text = "Confirmar Saída",
                    colors = colors,
                    onClick = onConfirm
                )
            } else {
                ActionButton(
                    text = "Escanear novamente",
                    colors = colors,
                    onClick = onBack
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label:", fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.width(110.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
}

@Composable
private fun ActionButton(
    text: String,
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.buttonContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.buttonText
        )
    }
}

@Composable
private fun NoCameraPermission(
    colors: dev.fslab.comunicacao.escolar.ui.theme.ComunicacaoEscolarColors,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(colors.background).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Permissão de câmera necessária",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para escanear QR Codes, permita o acesso à câmera nas configurações.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        ActionButton(text = "Solicitar permissão", colors = colors, onClick = onRequestPermission)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Voltar",
            fontSize = 14.sp,
            color = colors.textSecondary,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBack
            )
        )
    }
}