package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
internal actual fun ImagePicker(
    show: Boolean,
    allowMultiple: Boolean,
    onImagesSelected: (images: List<ByteArray>) -> Unit
) {
    LaunchedEffect(show){
        if (show){
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"
            input.multiple = allowMultiple

            input.onchange = { event ->
                val files = (event.target as? HTMLInputElement)?.files
                if(files != null && files.length > 0){
                    val byteArrays = mutableListOf<ByteArray>()
                    var processed = 0

                    for (i in 0 until files.length){
                        val reader = FileReader()

                        reader.onload = { _ ->
                            val result = reader.result
                            if(result is ArrayBuffer){
                                val int8Array = Int8Array(result)
                                val byteArray = ByteArray(int8Array.length){idx -> int8Array[idx] }
                                byteArrays.add(byteArray)
                            }
                            processed ++

                            if (processed == files.length){
                                onImagesSelected(byteArrays)
                            }
                        }
                        reader.readAsArrayBuffer(files[i]!!)
                    }
                }else {
                    onImagesSelected(emptyList())
                }
            }
            input.click()
        }

    }
}