package com.fireflow.fotografias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.fireflow.common.components.FireFlowEmptyState
import com.fireflow.common.components.FireFlowTopBar
import com.fireflow.fotografias.components.CameraPermissionHandler
import com.fireflow.fotografias.components.CameraPreview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fireflow.common.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun PhotosScreen(
    revisionId: Long,
    onBack: () -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        CameraPermissionHandler(
            onPermissionGranted = {
                CameraPreview(
                    onPhotoCaptured = { bitmap ->
                        viewModel.savePhoto(bitmap)
                        showCamera = false
                    },
                    onDismiss = { showCamera = false },
                    modifier = Modifier.fillMaxSize()
                )
            },
            onPermissionDenied = { showCamera = false }
        )
        return
    }

    Scaffold(
        topBar = {
            FireFlowTopBar(title = stringResource(R.string.photos_title), onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCamera = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.photos_take_desc))
            }
        }
    ) { padding ->
        if (isCapturing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (photos.isEmpty()) {
            FireFlowEmptyState(
                icon = Icons.Default.PhotoLibrary,
                title = stringResource(R.string.photos_empty_title),
                subtitle = stringResource(R.string.photos_empty_subtitle),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    Card(
                        modifier = Modifier.animateItem().fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photo.filePath)
                                    .crossfade(true)
                                    .size(512)
                                    .build(),
                                contentDescription = stringResource(R.string.photos_item_desc),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.deletePhoto(photo.id) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.photos_delete_desc),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
