package org.example.project

import androidx.compose.runtime.Composable

@Composable
internal expect fun ImagePicker(
    show: Boolean,
    allowMultiple:Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
)