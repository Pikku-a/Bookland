package com.dhaval.bookland.ui.components.main

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.dhaval.bookland.BarcodeAnalyzer

// Currently this doesn't work properly.
// Camera preview opens and closes properly.
// Camera permission is not asked and it had to be enabled manually for it to work.
// So fix this. Use BarcodeScannerActivity for reference.

@Composable
fun BarcodeScannerScreen(navController: NavHostController) {
    // Should this be what is in BarcodeScannerActivity?
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannedCode by remember { mutableStateOf("") }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                // Set up CameraX preview
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                BarcodeAnalyzer()
                            )
                        }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        // Handle exceptions
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        },
        modifier = Modifier.fillMaxSize() // Or adjust the size
    )

    // Display scanned barcode
    println("Scanned Code: $scannedCode")
}