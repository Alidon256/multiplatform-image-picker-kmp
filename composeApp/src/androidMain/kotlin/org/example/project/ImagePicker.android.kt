package org.example.project                           // Our app's package name

import android.content.Context                         // Needed for ContentResolver
import android.net.Uri                                 // URI type for picked media
import androidx.activity.compose.rememberLauncherForActivityResult  // Compose Activity Result API
import androidx.activity.result.PickVisualMediaRequest  // Request builder for photo picker
import androidx.activity.result.contract.ActivityResultContracts   // Modern picker contracts
import androidx.compose.runtime.Composable               // Compose function marker
import androidx.compose.runtime.LaunchedEffect           // Side-effect launcher
import androidx.compose.ui.platform.LocalContext         // Gets current context in Compose

/**
 * Android actual implementation of cross-platform ImagePicker using modern Photo Picker.
 * Opens system picker → reads selected images as ByteArray → calls callback.
 *
 * @param show Launch picker when true (usually tied to a state variable)
 * @param allowMultiple true = multi-select, false = single image
 * @param onImagesSelected Receives list of successfully read image bytes (empty on cancel/failure)
 */
@Composable
internal actual fun ImagePicker(                      // Actual impl for Android
    show: Boolean,                                    // Control flag to show picker
    allowMultiple: Boolean,                           // Single or multi selection mode
    onImagesSelected: (List<ByteArray>) -> Unit       // Callback with loaded image bytes
) {
    val context = LocalContext.current                // Get current Activity context safely

    // ────────────────────────────────────────────────────────────────
    // Multi-select launcher (used when allowMultiple = true)
    // ────────────────────────────────────────────────────────────────
    val multipleLauncher = rememberLauncherForActivityResult(   // Remember launcher across recompositions
        contract = ActivityResultContracts.PickMultipleVisualMedia(),  // Contract for picking many items
        onResult = { uris: List<Uri> ->                   // Called when user finishes picking
            val bytesList = uris.mapNotNull { uri ->      // Convert each Uri → ByteArray or skip if fails
                context.contentResolver.openInputStream(uri)  // Open stream from content provider
                    ?.use { it.readBytes() }              // Read all bytes & auto-close stream
            }
            onImagesSelected(bytesList)                   // Send loaded images to callback
        }
    )

    // ────────────────────────────────────────────────────────────────
    // Single-select launcher (used when allowMultiple = false)
    // ────────────────────────────────────────────────────────────────
    val singleLauncher = rememberLauncherForActivityResult(     // Remember launcher across recompositions
        contract = ActivityResultContracts.PickVisualMedia(),   // Contract for picking one item
        onResult = { uri: Uri? ->                         // Called when user finishes (null = cancel)
            val bytes = uri?.let { u ->                   // Only proceed if we have a Uri
                context.contentResolver.openInputStream(u)    // Open input stream
                    ?.use { it.readBytes() }              // Read bytes & auto-close
            }
            onImagesSelected(if (bytes != null) listOf(bytes) else emptyList())  // Always return List
        }
    )

    // ────────────────────────────────────────────────────────────────
    // Trigger the picker when 'show' becomes true
    // ────────────────────────────────────────────────────────────────
    LaunchedEffect(show) {                                // Run only when 'show' changes
        if (show) {                                       // Only launch when flag is true
            val request = PickVisualMediaRequest(         // Build picker request
                ActivityResultContracts.PickVisualMedia.ImageOnly   // Restrict to images only
                // Tip: change to .ImageAndVideo if you also want videos
            )
            if (allowMultiple) {                          // Choose correct launcher
                multipleLauncher.launch(request)          // Open multi-select picker
            } else {
                singleLauncher.launch(request)            // Open single-select picker
            }
        }
    }
}