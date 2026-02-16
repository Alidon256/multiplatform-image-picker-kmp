package org.example.project                           // Our shared project package

import androidx.compose.runtime.Composable              // Marks this as a composable function
import androidx.compose.runtime.LaunchedEffect          // Runs side-effect when show changes
import kotlinx.browser.document                        // Browser DOM access
import org.khronos.webgl.ArrayBuffer                   // WebGL typed array buffer
import org.khronos.webgl.Int8Array                      // 8-bit integer typed array
import org.w3c.dom.HTMLInputElement                    // HTML <input> element type
import org.w3c.files.FileReader                        // Reads file contents asynchronously
import org.w3c.files.get                               // Extension to access file list by index

/**
 * Web (JavaScript/Wasm) actual implementation of cross-platform ImagePicker.
 * Creates a hidden <input type="file"> element → triggers file selection dialog
 * → reads selected image(s) as ByteArray → delivers to callback.
 *
 * @param show When true, programmatically trigger the file picker dialog
 * @param allowMultiple When true → allows selecting multiple images
 * @param onImagesSelected Callback receiving list of image bytes (empty on cancel/no selection)
 */
@Composable
internal actual fun ImagePicker(                      // Actual impl for Web/JS target
    show: Boolean,                                    // Flag to trigger file picker
    allowMultiple: Boolean,                           // Single or multi file selection
    onImagesSelected: (List<ByteArray>) -> Unit       // Returns loaded image bytes
) {
    LaunchedEffect(show) {                            // Only run when 'show' value changes
        if (show) {                                   // Only proceed if show == true
            val input = document.createElement("input") as HTMLInputElement  // Create hidden file input
            input.type = "file"                           // Must be file input
            input.accept = "image/*"                      // Restrict to image files only
            input.multiple = allowMultiple                // Enable multi-select if requested

            input.onchange = { event ->                   // Fired when user selects file(s)
                val files = (event.target as? HTMLInputElement)?.files  // Get FileList
                if (files != null && files.length > 0) {  // At least one file selected
                    val byteArrays = mutableListOf<ByteArray>()  // Collect all results here
                    var processed = 0                         // Counter to know when all are done

                    for (i in 0 until files.length) {     // Loop over each selected file
                        val reader = FileReader()             // Create new reader per file

                        reader.onload = { loadEvent ->        // Called when file reading finishes
                            val buffer = loadEvent.target.asDynamic().result as? ArrayBuffer  // Get raw data
                            if (buffer != null) {             // Successfully read
                                byteArrays.add(Int8Array(buffer).unsafeCast<ByteArray>())  // Convert to Kotlin ByteArray
                            }
                            processed++                       // One more file processed

                            if (processed == files.length) {  // All files finished reading
                                onImagesSelected(byteArrays)      // Deliver complete list
                            }
                        }

                        reader.readAsArrayBuffer(files[i]!!)  // Start async reading of file as bytes
                    }
                } else {                                      // User cancelled or no files
                    onImagesSelected(emptyList())             // Return empty list (consistent API)
                }
            }

            input.click()                                 // Programmatically open file picker dialog
        }
    }
}