package org.example.project                           // Our shared project package

import androidx.compose.runtime.Composable              // Marks this as a composable function
import androidx.compose.runtime.LaunchedEffect          // Runs side-effect when show changes
import java.awt.FileDialog                             // Native file chooser dialog (AWT/Swing)
import java.awt.Frame                                   // Parent frame reference (can be null)
import java.io.File                                     // Represents selected files

/**
 * Desktop (JVM) actual implementation of cross-platform ImagePicker.
 * Shows the native system file dialog → lets user select image file(s)
 * → reads selected files as ByteArray → delivers to callback.
 *
 * @param show When true, open the native file chooser dialog
 * @param allowMultiple When true → allows selecting multiple images (if OS supports it)
 * @param onImagesSelected Callback receiving list of image bytes (empty on cancel/no selection)
 */
@Composable
internal actual fun ImagePicker(                      // Actual impl for Desktop/JVM target
    show: Boolean,                                    // Flag to trigger file dialog
    allowMultiple: Boolean,                           // Single or multi file selection
    onImagesSelected: (List<ByteArray>) -> Unit       // Returns loaded image bytes
) {
    LaunchedEffect(show) {                            // Only run when 'show' value changes
        if (show) {                                   // Only proceed if show == true

            val dialog = FileDialog(                  // Create native file chooser dialog
                null as Frame?,                       // No parent frame (floating dialog)
                "Select Images",                      // Dialog title
                FileDialog.LOAD                       // Mode: open/load files
            ).apply {
                isMultipleMode = allowMultiple        // Enable multi-selection if requested
                // (supported on macOS, Windows 10+, Linux with modern DE)

                setFilenameFilter { _, name ->        // Simple client-side filter for images
                    val lowercase = name.lowercase()  // Case-insensitive check
                    lowercase.endsWith(".jpg") ||     // Common JPEG
                            lowercase.endsWith(".jpeg") ||    // Alternative JPEG
                            lowercase.endsWith(".png")        // PNG support
                }
            }

            dialog.isVisible = true                   // Show the dialog (blocks until closed)

            val selectedFiles = dialog.files          // Get array of selected File objects
                .map { it.readBytes() }               // Read each file fully into ByteArray

            onImagesSelected(selectedFiles)           // Deliver results to callback
            // Note: if user cancels → files is empty array → we send emptyList()
        }
    }
}