# Multiplatform Image Picker

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.7.1-blue?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Desktop%20%7C%20Web-green.svg)](#)

A production-ready implementation of a native image picker for **Compose Multiplatform**. This project demonstrates a clean architecture approach to handling platform-specific media APIs while maintaining a unified UI state in shared code.

## Overview

Handling file systems and media pickers in a multiplatform environment requires bridging the gap between high-level UI declarations and low-level system APIs. This project utilizes the **expect/actual** pattern to provide a seamless "Pick and Display" experience across Android, JVM (Desktop), and Web (WASM/JS).

## Features

- **Multi-Platform Support**: Android, Desktop (JVM), and Web (WASM/JS).
- **Single & Multiple Selection**: Configurable selection modes.
- **Modern Android API**: Utilizes `ActivityResultContracts.PickVisualMedia` for a secure, permission-less experience on Android 13+.
- **Native Desktop Integration**: Uses AWT `FileDialog` for a native OS look and feel.
- **Web Integration**: Leverages HTML5 File API and `FileReader` for browser-based selection.
- **Memory Efficient**: Passes image data as `ByteArray` to ensure compatibility across all targets.

## Architecture

The project follows a modular architecture centered around the shared `commonMain` module:

- **Shared State**: Managed via Compose `runtime` states (`mutableStateListOf`).
- **Expect/Actual Pattern**: The `ImagePicker` composable is defined as an `expect` function in `commonMain` and implemented using platform-specific APIs.
- **Image Processing**: Image data is handled as `ByteArray` and decoded into `ImageBitmap` in the shared UI using `androidx.compose.ui.graphics.decodeToImageBitmap`.

## Project Structure

```text
composeApp/
├── commonMain/      # Shared UI logic and ImagePicker interface
├── androidMain/     # Android implementation (Photo Picker API)
├── jvmMain/         # Desktop implementation (AWT FileDialog)
├── jsMain/          # JavaScript implementation (HTML Input)
└── wasmJsMain/      # WASM implementation (TypedArray mapping)
```

## Screenshots

![Desktop and Mobile Preview](https://firebasestorage.googleapis.com/v0/b/mindset-reels.appspot.com/o/git_images%2Fmultiplatform-image-picker%2FDesktop%20%26%20Mobile.png?alt=media&token=30c483ac-b38c-4235-b478-9b6b36260c9d)

## Usage

### 1. Define the Picker Interface (commonMain)

```kotlin
@Composable
internal expect fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
)
```

### 2. Implementation Example

Integrating the picker into your shared Compose UI:

```kotlin
@Composable
fun App() {
    val selectedImages = remember { mutableStateListOf<ByteArray>() }
    var showPicker by remember { mutableStateOf(false) }

    ImagePicker(
        show = showPicker,
        allowMultiple = true,
        onImagesSelected = { images ->
            selectedImages.addAll(images)
            showPicker = false
        }
    )

    Button(onClick = { showPicker = true }) {
        Text("Select Images")
    }
}
```

## Platform Implementations (Actual)

### Android Implementation (`androidMain`)
Uses `rememberLauncherForActivityResult` with `PickVisualMedia` and `PickMultipleVisualMedia`.

```kotlin
@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    val context = LocalContext.current

    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            val bytesList = uris.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            onImagesSelected(bytesList)
        }
    )

    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val bytes = uri?.let { u ->
                context.contentResolver.openInputStream(u)?.use { it.readBytes() }
            }
            onImagesSelected(if (bytes != null) listOf(bytes) else emptyList())
        }
    )

    LaunchedEffect(show) {
        if (show) {
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (allowMultiple) multipleLauncher.launch(request) else singleLauncher.launch(request)
        }
    }
}
```

### Desktop Implementation (`jvmMain`)
Uses the AWT `FileDialog` for native file selection.

```kotlin
@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val dialog = FileDialog(null as Frame?, "Select Images", FileDialog.LOAD).apply {
                isMultipleMode = allowMultiple
                setFilenameFilter { _, name ->
                    val lowercase = name.lowercase()
                    lowercase.endsWith(".jpg") || lowercase.endsWith("jpeg") || lowercase.endsWith(".png")
                }
            }
            dialog.isVisible = true
            onImagesSelected(dialog.files.map { it.readBytes() })
        }
    }
}
```

### Web Implementation (`jsMain` & `wasmJsMain`)
Programmatically triggers a file input and reads the result as `ArrayBuffer`.

```kotlin
// Example for WasmJs implementation
@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"
            input.multiple = allowMultiple

            input.onchange = { event ->
                val files = (event.target as? HTMLInputElement)?.files
                if (files != null && files.length > 0) {
                    val byteArrays = mutableListOf<ByteArray>()
                    var processed = 0
                    for (i in 0 until files.length) {
                        val reader = FileReader()
                        reader.onload = { _ ->
                            val result = reader.result
                            if (result is ArrayBuffer) {
                                val int8Array = Int8Array(result)
                                val byteArray = ByteArray(int8Array.length) { idx -> int8Array[idx] }
                                byteArrays.add(byteArray)
                            }
                            processed++
                            if (processed == files.length) onImagesSelected(byteArrays)
                        }
                        reader.readAsArrayBuffer(files[i]!!)
                    }
                } else {
                    onImagesSelected(emptyList())
                }
            }
            input.click()
        }
    }
}
```

## Setup Instructions

### Prerequisites
- JDK 17 or higher
- Android Studio Ladybug or IntelliJ IDEA
- Kotlin 2.1.0+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Alidon256/multiplatform-image-picker-kmp.git
   ```
2. Open the project in your IDE.
3. Sync Gradle.

### Running the App
- **Android**: Run the `composeApp` configuration on an emulator or physical device.
- **Desktop**: Run `./gradlew :composeApp:run`
- **Web**: Run `./gradlew :composeApp:wasmJsBrowserRun`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Mugumya Ali**
- [LinkedIn](https://www.linkedin.com/in/mugumya-ali-937591367/)
- [GitHub](https://github.com/Alidon256)
