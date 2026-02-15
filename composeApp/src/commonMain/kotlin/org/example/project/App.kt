package org.example.project

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import org.jetbrains.compose.resources.decodeToImageBitmap
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face // Inbuilt icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
/**
 * Represents a picked image file.
 * @param name The display name or generated ID.
 * @param data The raw bytes of the image.
 */
data class ImageFile(
    val name: String,
    val data: ByteArray
)
/**
 * Main entry point for the Multiplatform Image Picker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val pickedImages = remember { mutableStateListOf<ImageFile>() }
        var showPicker by remember { mutableStateOf(false) }
        var allowMultiple by remember { mutableStateOf(true) }
        ImagePicker(
            show = showPicker,
            allowMultiple =allowMultiple,
            onImagesSelected = { images ->
                showPicker = false
                images.forEach{ data ->
                    val fileName = "IMG_${pickedImages.size + 1}"
                    pickedImages.add(ImageFile(fileName, data))
                }
            }
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gallery Multiplatform", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showPicker = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Pick Image") },
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (pickedImages.isEmpty()) {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    ImageGrid(
                        images = pickedImages,
                        onRemove = { pickedImages.remove(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Face,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Text(
            text = "Your gallery is empty",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ImageGrid(images: List<ImageFile>, onRemove: (ImageFile) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(images, key = { it.name }) { imageFile ->
            ImageCard(imageFile = imageFile, onRemove = { onRemove(imageFile) })
        }
    }
}

@Composable
fun ImageCard(imageFile: ImageFile, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // FIX: Call as an extension function on imageFile.data
            // We use 'remember' to avoid re-decoding the image on every frame
            val bitmap = remember(imageFile.data) {
                try {
                    imageFile.data.decodeToImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = imageFile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Optional: Show a placeholder if decoding fails
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Face, null, tint = Color.Gray)
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .size(24.dp)
            ) {
                Icon(Icons.Default.Clear, "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
