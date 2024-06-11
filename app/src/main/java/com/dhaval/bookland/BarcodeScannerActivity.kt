package com.dhaval.bookland

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dhaval.bookland.databinding.ActivityBarcodeScannerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider

class BarcodeScannerActivity : AppCompatActivity() {
    // Executor service
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: ActivityBarcodeScannerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Complete activity setup...

    }

    override fun onDestroy() {
        super.onDestroy()

        cameraExecutor.shutdown()
    }

    // Permission request
// 1. This function is responsible to request the required CAMERA permission
    private fun checkCameraPermission() {
        try {
            val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this,requiredPermissions,0)
        } catch (e: IllegalArgumentException) {
            checkIfCameraPermissionIsGranted()
        }
    }

    // 2. This function will check if the CAMERA permission has been granted.
// If so, it will call the function responsible to initialize the camera preview.
// Otherwise it will raise an alert.
    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted: start the preview
            startCamera()
        }else{
            // Permission denied
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission required")
                .setMessage("This app needs to access the camera to read barcodes.")
                .setPositiveButton("Ok") {_, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission() // Maybe remove this and only ask if trying to use the scanner.
                }
                .setCancelable(false) // WHY?!
                .create()
                .apply {
                    setCanceledOnTouchOutside(false) // Again, WHY?!
                    show()
                }
        }
    }

    // 3. This function is executed once the user has granted or denied the missing permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        checkIfCameraPermissionIsGranted()
    }

    // This function sets up the camera preview and the image analyzer.
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Image analyzer
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor,BarcodeAnalyzer())
                }

            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,cameraSelector,preview,imageAnalyzer
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
