package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class  ImageFile(
    val name: String,
    val data: ByteArray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme (
        colorScheme = darkColorScheme()
    ){
        val pickImages = remember { mutableStateListOf<ImageFile>() }
        var showPicker by remember { mutableStateOf(false) }
        val allowMultiple by remember { mutableStateOf(true) }

        ImagePicker(
            show = showPicker,
            allowMultiple = allowMultiple,
            onImagesSelected = {images ->
                showPicker = false
                images.forEach { data ->
                    val fileName = "IMG_${pickImages.size + 1}"
                    pickImages.add(ImageFile(fileName,data))
                }
            }
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {Text("Gallery Multiplatform", fontWeight = FontWeight.Bold)},
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {showPicker = true},
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = {Text("Pick Image")}
                )
            }
        ){ paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ){
                if (pickImages.isEmpty()){
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }else{
                    ImageGrid(
                        images = pickImages,
                        onRemove = {pickImages.remove(it)}
                    )
                }
            }

        }
    }

}

@Composable
fun EmptyState(
    modifier: Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ){
        Icon(
            imageVector = Icons.Default.Face,
            contentDescription = null,
            modifier = modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Text(
            text = "Your gallery is Empty",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ImageGrid(
    images: List<ImageFile>,
    onRemove: (ImageFile) -> Unit
){
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        items(images, key = {it.name}){imageFile ->
            ImageCard(imageFile = imageFile, onRemove = {onRemove(imageFile)})
        }
    }
}

@Composable
fun ImageCard(imageFile: ImageFile, onRemove: ()-> Unit){
    Card(
        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Box {
            val bitmap = remember(imageFile.data){
                try {
                    imageFile.data.decodeToImageBitmap()
                } catch (e: Exception){
                    null
                }
            }
            if (bitmap != null){
                Image(
                    bitmap = bitmap,
                    contentDescription = imageFile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }else {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        Icons.Default.Face,null, tint = Color.Gray
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.4f),RoundedCornerShape(50))
                    .size(24.dp)
            ){
                Icon(
                    Icons.Default.Clear,
                    "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

        }
    }
}