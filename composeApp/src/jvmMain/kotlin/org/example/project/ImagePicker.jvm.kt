package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.FileDialog
import java.awt.Frame

@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    LaunchedEffect(show){
        if(show){
            val dialog = FileDialog(
                null as Frame?,
                "Select Images",
                FileDialog.LOAD
            ).apply {
                isMultipleMode = allowMultiple

                setFilenameFilter{_, name ->
                    val lowercase = name.lowercase()
                    lowercase.endsWith(".jpg") ||
                            lowercase.endsWith("jpeg") ||
                            lowercase.endsWith(".png")
                }
            }

            dialog.isVisible = true

            val selectedFiles = dialog.files
                .map { it.readBytes() }

            onImagesSelected(selectedFiles)
        }
    }
}