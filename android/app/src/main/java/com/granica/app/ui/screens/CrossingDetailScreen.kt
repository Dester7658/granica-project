package com.granica.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.granica.app.AppSettings
import com.granica.app.R
import com.granica.app.data.api.NetworkModule
import com.granica.app.data.model.CameraDto
import com.granica.app.data.model.CrossingDto
import com.granica.app.ui.UiState
import com.granica.app.ui.components.GranicaHeader
import com.granica.app.ui.viewmodel.CrossingDetailViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossingDetailScreen(
    viewModel: CrossingDetailViewModel,
    settings: AppSettings,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    Scaffold(
        topBar = {
            GranicaHeader(
                title = (state as? UiState.Success)?.data?.name.orEmpty(),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            tint = androidx.compose.ui.graphics.Color.White,
                            contentDescription = stringResource(
                                if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                            )
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }

            is UiState.Error -> ErrorState(
                message = s.message,
                modifier = Modifier.fillMaxSize().padding(padding),
                onRetry = { viewModel.load() }
            )

            is UiState.Success -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    val firstImageCamera = s.data.cameras.firstOrNull { it.snapshotUrl != null || it.pageUrl != null }
                    val aiState by viewModel.aiEstimate.collectAsState()
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = { firstImageCamera?.let { viewModel.estimateWithAi(it.id) } },
                            enabled = firstImageCamera != null && aiState !is UiState.Loading
                        ) {
                            Text("Оценить очередь с ИИ")
                        }
                        when (val estimate = aiState) {
                            is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                            is UiState.Success -> Text(
                                "ИИ: ${estimate.data.vehicleCount} машин, примерно ${estimate.data.estimatedWaitMinutes} мин. Уверенность: ${estimate.data.confidence}",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            is UiState.Error -> Text(
                                "Оценка ИИ недоступна: ${estimate.message}",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                            null -> Unit
                        }
                    }
                    s.data.waitMinutes?.let { minutes ->
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(settings.uiText("wait_time"), style = MaterialTheme.typography.labelLarge)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                            WaitBadge(minutes, settings)
                        }
                    }
                }
                items(s.data.cameras) { camera ->
                    CameraCard(crossing = s.data, camera = camera, defaultEnabled = !settings.disableCameras, settings = settings)
                }
            }
        }
    }
}

@Composable
private fun CameraCard(crossing: CrossingDto, camera: CameraDto, defaultEnabled: Boolean = false, settings: AppSettings) {
    var isEnabled by remember(camera.id, defaultEnabled) { mutableStateOf(defaultEnabled) }
    var isFullscreen by remember(camera.id) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Text(settings.localizedRouteLabel(camera.name), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isEnabled) {
                DisabledCamera(settings = settings, onEnable = { isEnabled = true })
            } else {
                CameraDisplay(
                    crossing = crossing,
                    camera = camera,
                    modifier = Modifier.clickable { isFullscreen = true }
                )
            }
        }
        if (isEnabled && (camera.type == "webview" || camera.pageUrl != null) && camera.snapshotUrl == null && camera.streamUrl == null) {
            Button(onClick = { isFullscreen = true }, modifier = Modifier.padding(top = 8.dp)) {
                Text(settings.uiText("open_fullscreen"))
            }
        }
    }

    if (isFullscreen) {
        FullScreenCamera(
            crossing = crossing,
            camera = camera,
            onDismiss = { isFullscreen = false }
        )
    }
}

@Composable
private fun DisabledCamera(settings: AppSettings, onEnable: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clickable(onClick = onEnable),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(settings.uiText("camera_disabled"), style = MaterialTheme.typography.titleMedium)
            Text(settings.uiText("tap_to_enable"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun isDirectImageUrl(url: String): Boolean {
    val lower = url.lowercase()
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp")
}

@Composable
private fun CameraDisplay(crossing: CrossingDto, camera: CameraDto, modifier: Modifier = Modifier) {
    when {
        camera.streamUrl != null && (camera.type == "hls" || camera.type == "mjpeg" || camera.type == "stream") -> {
            VideoCamera(streamUrl = camera.streamUrl, modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f))
        }
        camera.snapshotUrl != null -> {
            SnapshotCamera(
                crossing = crossing,
                camera = camera,
                modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f)
            )
        }
        camera.pageUrl != null -> {
            if (isDirectImageUrl(camera.pageUrl)) {
                DirectImageCamera(imageUrl = camera.pageUrl, modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f))
            } else {
                WebViewCamera(url = camera.pageUrl, modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f))
            }
        }
        camera.type == "webview" && camera.pageUrl != null -> {
            if (isDirectImageUrl(camera.pageUrl)) {
                DirectImageCamera(imageUrl = camera.pageUrl, modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f))
            } else {
                WebViewCamera(url = camera.pageUrl, modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f))
            }
        }
        else -> {
            DisabledCamera(settings = AppSettings(LocalContext.current), onEnable = {})
        }
    }
}

@Composable
private fun DirectImageCamera(imageUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Camera image",
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun SnapshotCamera(crossing: CrossingDto, camera: CameraDto, modifier: Modifier = Modifier) {
    val refreshMillis = (camera.refreshSeconds ?: 15) * 1000L
    var reloadTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(camera.id) {
        while (true) {
            delay(refreshMillis)
            reloadTick++
        }
    }

    AsyncImage(
        model = camera.snapshotUrl ?: (NetworkModule.snapshotUrl(crossing.id, camera.id) + "&tick=$reloadTick"),
        contentDescription = camera.name,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
private fun VideoCamera(streamUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player = remember(context, streamUrl) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(streamUrl))
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                controllerAutoShow = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}

@Composable
private fun WebViewCamera(url: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            android.webkit.WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.setSupportZoom(false)
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.databaseEnabled = true
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                webViewClient = android.webkit.WebViewClient()
                webChromeClient = android.webkit.WebChromeClient()
                setBackgroundColor(android.graphics.Color.BLACK)
                loadUrl(url)
            }
        }
    )
}

@Composable
private fun FullScreenCamera(crossing: CrossingDto, camera: CameraDto, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        if (camera.pageUrl != null && isDirectImageUrl(camera.pageUrl)) {
            DirectImageCamera(imageUrl = camera.pageUrl, modifier = Modifier.fillMaxSize())
        } else if (camera.type == "webview" && camera.pageUrl != null) {
            WebViewCamera(url = camera.pageUrl, modifier = Modifier.fillMaxSize())
        } else {
            ZoomableSnapshot(crossing = crossing, camera = camera, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun ZoomableSnapshot(crossing: CrossingDto, camera: CameraDto, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
    }
    Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = NetworkModule.snapshotUrl(crossing.id, camera.id) + "&full=1",
            contentDescription = camera.name,
            modifier = Modifier
                .fillMaxWidth()
                .transformable(transformState)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
    }
}

