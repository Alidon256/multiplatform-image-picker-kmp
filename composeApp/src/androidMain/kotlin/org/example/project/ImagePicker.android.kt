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
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    val context = LocalContext.current

    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = {uris : List<Uri> ->
            val bytesList = uris.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            onImagesSelected(bytesList)
        }
    )

    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {uri: Uri? ->
            val bytes = uri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            onImagesSelected(if (bytes!= null) listOf(bytes) else emptyList())
        }

    )

    LaunchedEffect(show) {
        if (show){
            val request = PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
            if (allowMultiple){
                multipleLauncher.launch(request)
            }else{
                singleLauncher.launch(request)
            }
        }
    }
}