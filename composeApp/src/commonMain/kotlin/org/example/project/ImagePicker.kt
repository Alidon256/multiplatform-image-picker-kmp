/*package org.example.project

import androidx.compose.runtime.Composable

/**
 * An expect composable that provides a mechanism to launch a platform-specific
 * image picker and returns the selected image as a ByteArray.
 *
 * @param show A boolean to control the visibility of the picker.
 * @param onImageSelected A lambda that will be invoked with the ByteArray of the selected image.
 *                        The ByteArray will be null if the user cancels the operation.
 */
@Composable
internal expect fun ImagePicker(
    show: Boolean,
    onImageSelected: (imageData: ByteArray?) -> Unit,
)*/
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
