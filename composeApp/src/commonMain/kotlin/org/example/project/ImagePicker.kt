
package org.example.project

import androidx.compose.runtime.Composable

/**
 * An expect composable that launches a platform-specific image picker.
 *
 * @param show A boolean to control the visibility of the picker.
 * @param allowMultiple If true, the user can select multiple images.
 * @param onImagesSelected A lambda invoked with a list of ByteArrays. Returns empty list if cancelled.
 */
@Composable
internal expect fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit,
)
