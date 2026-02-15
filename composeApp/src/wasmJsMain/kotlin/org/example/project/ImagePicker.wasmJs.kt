package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

/**
 * A Web-specific implementation of the ImagePicker using HTML5 File API for Kotlin/Wasm.
 * Handles multiple file selection and converts Web ArrayBuffers to Kotlin ByteArrays.
 *
 * @param show Boolean flag to trigger the file picker dialog.
 * @param allowMultiple If true, allows selecting more than one image.
 * @param onImagesSelected Callback invoked with a list of [ByteArray]. Empty list if cancelled.
 */
@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val input = document.createElement("input") as HTMLInputElement
            input.apply {
                type = "file"
                accept = "image/*"
                multiple = allowMultiple
            }

            input.onchange = { event ->
                val files = (event.target as? HTMLInputElement)?.files
                val fileCount = files?.length ?: 0

                if (files != null && fileCount > 0) {
                    val selectedImages = mutableListOf<ByteArray>()
                    var processedCount = 0

                    for (i in 0 until fileCount) {
                        val file = files[i] ?: continue
                        val reader = FileReader()

                        reader.onload = { _ ->
                            val result = reader.result
                            if (result is ArrayBuffer) {
                                // Convert JS ArrayBuffer to Kotlin ByteArray
                                val int8Array = Int8Array(result)
                                val byteArray = ByteArray(int8Array.length) { index -> int8Array[index] }
                                selectedImages.add(byteArray)
                            }

                            processedCount++
                            // Only return once all selected files are processed
                            if (processedCount == fileCount) {
                                onImagesSelected(selectedImages)
                            }
                        }

                        reader.onerror = {
                            processedCount++
                            if (processedCount == fileCount) {
                                onImagesSelected(selectedImages)
                            }
                        }

                        reader.readAsArrayBuffer(file)
                    }
                } else {
                    onImagesSelected(emptyList())
                }
            }

            input.click()
        }
    }
}