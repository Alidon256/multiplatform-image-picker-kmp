package org.example.project                           // Our shared project package

import androidx.compose.runtime.Composable              // Marks this as a composable function
import androidx.compose.runtime.LaunchedEffect          // Runs side-effect when show changes
import kotlinx.browser.document                        // Browser DOM access
import org.khronos.webgl.ArrayBuffer                   // WebGL typed array buffer type
import org.khronos.webgl.Int8Array                      // 8-bit signed integer array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement                    // HTML <input> element
import org.w3c.files.FileReader                        // Asynchronous file reader
import org.w3c.files.get                               // Safe indexed access to FileList

/**
 * Web (Kotlin/JS or Kotlin/Wasm) actual implementation of cross-platform ImagePicker.
 * Uses hidden <input type="file"> → triggers native file picker → reads images as ByteArray.
 *
 * @param show When true, programmatically open the file selection dialog
 * @param allowMultiple When true → allow selecting multiple images at once
 * @param onImagesSelected Callback receiving list of successfully read image bytes
 *                         (empty list if cancelled, no files, or all reads failed)
 */
@Composable
internal actual fun ImagePicker(                      // Actual impl for Web target
    show: Boolean,                                    // Control flag to trigger picker
    allowMultiple: Boolean,                           // Single or multi-selection mode
    onImagesSelected: (List<ByteArray>) -> Unit       // Returns loaded image bytes
) {
    LaunchedEffect(show) {                            // Run only when 'show' changes value
        if (show) {                                   // Only launch when flag is true

            val input = document.createElement("input") as HTMLInputElement  // Create hidden file input

            input.apply {                             // Configure the input in one block
                type = "file"                         // Must be a file picker
                accept = "image/*"                    // Restrict dialog to image files
                multiple = allowMultiple              // Enable multi-select if requested
            }

            input.onchange = { event ->               // Triggered when user selects file(s)
                val files = (event.target as? HTMLInputElement)?.files  // Get the FileList
                val fileCount = files?.length ?: 0    // Number of selected files (0 = cancel)

                if (files != null && fileCount > 0) { // At least one file was chosen
                    val selectedImages = mutableListOf<ByteArray>()  // Collect all results
                    var processedCount = 0            // Track how many files finished

                    for (i in 0 until fileCount) {    // Process each selected file
                        val file = files[i] ?: continue  // Safe access – skip if null

                        val reader = FileReader()     // New reader for each file

                        reader.onload = { _ ->        // Success callback when reading done
                            val result = reader.result  // Raw result from FileReader
                            if (result is ArrayBuffer) {  // Expected binary data format
                                val int8Array = Int8Array(result)  // View buffer as signed bytes
                                val byteArray = ByteArray(int8Array.length) { idx ->
                                    int8Array[idx]                // Copy to Kotlin ByteArray
                                }
                                selectedImages.add(byteArray)     // Store result
                            }
                            processedCount++          // One more file completed

                            if (processedCount == fileCount) {  // All files processed
                                onImagesSelected(selectedImages)  // Deliver final list
                            }
                        }

                        reader.onerror = {            // Handle read error for this file
                            processedCount++          // Count it as done (even if failed)
                            if (processedCount == fileCount) {
                                onImagesSelected(selectedImages)  // Send what we have
                            }
                        }

                        reader.readAsArrayBuffer(file)  // Start async read as raw bytes
                    }
                } else {                              // No files selected (cancelled)
                    onImagesSelected(emptyList())     // Return empty list – consistent API
                }
            }

            input.click()                             // Simulate click → open file dialog
        }
    }
}