/*package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
internal actual fun ImagePicker(
    show: Boolean,
    onImageSelected: (imageData: ByteArray?) -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val dialog = FileDialog(null as Frame?, "Choose a Profile Picture", FileDialog.LOAD)
            dialog.setFilenameFilter { _, name ->
                name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")
            }
            dialog.isVisible = true

            if (dialog.file != null) {
                val file = File(dialog.directory + dialog.file)
                onImageSelected(file.readBytes())
            } else {
                onImageSelected(null)
            }
        }
    }
}*/
package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val dialog = FileDialog(null as Frame?, "Select Images", FileDialog.LOAD).apply {
                isMultipleMode = allowMultiple
                setFilenameFilter { _, name ->
                    val lowercase = name.lowercase()
                    lowercase.endsWith(".jpg") || lowercase.endsWith(".png") || lowercase.endsWith(".jpeg")
                }
            }
            dialog.isVisible = true

            val selectedFiles = dialog.files.map { it.readBytes() }
            onImagesSelected(selectedFiles)
        }
    }
}