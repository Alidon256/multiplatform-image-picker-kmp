package org.example.project                           // Shared package for multiplatform code

import androidx.compose.animation.*                     // Animations (not used yet, but good to have)
import androidx.compose.foundation.Image                // Displays bitmap images
import androidx.compose.foundation.background           // Background color modifier
import androidx.compose.foundation.layout.*             // All basic layout composables
import androidx.compose.foundation.lazy.grid.*          // Grid layout for images
import androidx.compose.foundation.shape.RoundedCornerShape // Rounded corners
import androidx.compose.material.icons.Icons            // Material icons collection
import androidx.compose.material.icons.filled.*         // Filled style icons (Add, Clear, Face)
import org.jetbrains.compose.resources.decodeToImageBitmap // Multiplatform image decoding
import androidx.compose.material3.*                     // Material 3 components (Card, Scaffold, etc.)
import androidx.compose.runtime.*                        // Core Compose state & remember
import androidx.compose.ui.Alignment                     // Center, top-end, etc.
import androidx.compose.ui.Modifier                      // Modifier chain builder
import androidx.compose.ui.draw.clip                     // Clip shapes
import androidx.compose.ui.graphics.Color               // Colors
import androidx.compose.ui.graphics.ImageBitmap         // Decoded image type
import androidx.compose.ui.layout.ContentScale          // Crop, Fit, etc. image scaling
import androidx.compose.ui.text.font.FontWeight         // Bold, etc.
import androidx.compose.ui.unit.dp                      // Density-independent pixels
import androidx.compose.ui.unit.sp                       // Scalable pixels for text

/**
 * Data class that holds one picked image.
 * Used to store both the name (for display/key) and raw bytes.
 */
data class ImageFile(                                 // Simple holder for picked image
    val name: String,                                 // Display name or generated ID
    val data: ByteArray                               // Raw image bytes from picker
)

/**
 * Root composable – main screen of the multiplatform gallery app.
 * Uses Material 3 dark theme, Scaffold layout, and manages picked images state.
 */
@OptIn(ExperimentalMaterial3Api::class)              // Allows usage of newer Material 3 APIs
@Composable
fun App() {                                           // Main entry point composable
    MaterialTheme(colorScheme = darkColorScheme()) {  // Apply dark theme to whole app
        val pickedImages = remember { mutableStateListOf<ImageFile>() }  // Observable list of images
        var showPicker by remember { mutableStateOf(false) }  // Controls when picker appears
        var allowMultiple by remember { mutableStateOf(true) }  // Toggle single/multi mode

        ImagePicker(                                  // Cross-platform picker (expect/actual)
            show = showPicker,                        // Open when this is true
            allowMultiple = allowMultiple,            // Allow picking several images
            onImagesSelected = { images ->            // Called when user finishes picking
                showPicker = false                    // Close picker after selection
                images.forEach { data ->              // Process each picked ByteArray
                    val fileName = "IMG_${pickedImages.size + 1}"  // Simple incremental name
                    pickedImages.add(ImageFile(fileName, data))    // Add to displayed list
                }
            }
        )

        Scaffold(                                     // Main screen structure: top bar + FAB + content
            topBar = {                                // Top app bar
                CenterAlignedTopAppBar(               // Centered title style
                    title = { Text("Gallery Multiplatform", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(  // Semi-transparent
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            floatingActionButton = {                  // Floating button to trigger picker
                ExtendedFloatingActionButton(         // Button with icon + text
                    onClick = { showPicker = true },  // Show picker when clicked
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Pick Image") }
                )
            }
        ) { paddingValues ->                          // Content area with safe padding
            Box(                                      // Full-screen container
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)           // Respect top bar & FAB padding
                    .background(MaterialTheme.colorScheme.background)  // Dark background
            ) {
                if (pickedImages.isEmpty()) {         // Show empty state if no images
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                } else {                              // Otherwise show the image grid
                    ImageGrid(
                        images = pickedImages,
                        onRemove = { pickedImages.remove(it) }  // Remove clicked image
                    )
                }
            }
        }
    }
}

/**
 * Shown when no images have been picked yet.
 */
@Composable
fun EmptyState(modifier: Modifier = Modifier) {       // Centered placeholder UI
    Column(                                           // Stack icon + text vertically
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(                                         // Subtle placeholder icon
            imageVector = Icons.Default.Face,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Text(                                         // Message
            text = "Your gallery is empty",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Displays all picked images in a responsive grid.
 */
@Composable
fun ImageGrid(images: List<ImageFile>, onRemove: (ImageFile) -> Unit) {
    LazyVerticalGrid(                                 // Efficient vertical grid
        columns = GridCells.Adaptive(minSize = 128.dp),  // Responsive columns
        contentPadding = PaddingValues(16.dp),        // Outer spacing
        horizontalArrangement = Arrangement.spacedBy(12.dp),  // Gap between columns
        verticalArrangement = Arrangement.spacedBy(12.dp)     // Gap between rows
    ) {
        items(images, key = { it.name }) { imageFile ->  // Each item keyed by name (stable)
            ImageCard(imageFile = imageFile, onRemove = { onRemove(imageFile) })
        }
    }
}

/**
 * Single image card with remove button overlay.
 */
@Composable
fun ImageCard(imageFile: ImageFile, onRemove: () -> Unit) {
    Card(                                             // Material 3 card with elevation
        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)),  // Square + rounded
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {                                         // Stack image + remove button
            // Decode bytes to ImageBitmap only once (remember key = data)
            val bitmap = remember(imageFile.data) {   // Cache decoded bitmap
                try {
                    imageFile.data.decodeToImageBitmap()  // Multiplatform decoding
                } catch (e: Exception) {              // Handle corrupt/invalid images
                    null
                }
            }

            if (bitmap != null) {                     // Show image if decoding succeeded
                Image(
                    bitmap = bitmap,
                    contentDescription = imageFile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop      // Fill square + crop edges
                )
            } else {                                  // Fallback placeholder on decode failure
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Face, null, tint = Color.Gray)
                }
            }

            IconButton(                               // Small remove button (top-right)
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .size(24.dp)
            ) {
                Icon(                                 // Clear (X) icon
                    Icons.Default.Clear,
                    "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}