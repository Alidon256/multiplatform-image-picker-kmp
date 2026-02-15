package org.example.project

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit,
) {
    val context = LocalContext.current

    // Launcher for Multiple Images
    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            val bytesList = uris.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            onImagesSelected(bytesList)
        }
    )

    // Launcher for Single Image
    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val bytes = uri?.let { context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() } }
            onImagesSelected(if (bytes != null) listOf(bytes) else emptyList())
        }
    )

    LaunchedEffect(show) {
        if (show) {
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (allowMultiple) {
                multipleLauncher.launch(request)
            } else {
                singleLauncher.launch(request)
            }
        }
    }
}