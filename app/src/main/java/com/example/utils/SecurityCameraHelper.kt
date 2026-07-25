package com.example.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SecurityCameraHelper {

    suspend fun captureSecurityPhotos(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ): List<String> {
        val capturedUris = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("SecurityCameraHelper", "Camera permission not granted")
            return emptyList()
        }

        try {
            val cameraProvider = getCameraProvider(context) ?: return emptyList()

            // Capture Front Camera
            val frontUri = captureImageFromCamera(
                context,
                lifecycleOwner,
                cameraProvider,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                "front"
            )
            if (frontUri != null) {
                capturedUris.add(frontUri)
            }

            // Capture Back Camera
            val backUri = captureImageFromCamera(
                context,
                lifecycleOwner,
                cameraProvider,
                CameraSelector.DEFAULT_BACK_CAMERA,
                "back"
            )
            if (backUri != null) {
                capturedUris.add(backUri)
            }
        } catch (e: Exception) {
            Log.e("SecurityCameraHelper", "Error capturing security photos", e)
        }

        return capturedUris
    }

    private suspend fun getCameraProvider(context: Context): ProcessCameraProvider? =
        suspendCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    continuation.resume(cameraProviderFuture.get())
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }, ContextCompat.getMainExecutor(context))
        }

    private suspend fun captureImageFromCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector,
        prefix: String
    ): String? = suspendCoroutine { continuation ->
        try {
            if (!cameraProvider.hasCamera(cameraSelector)) {
                continuation.resume(null)
                return@suspendCoroutine
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val storageDir = File(context.filesDir, "security_captures")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, "SECURITY_${prefix.uppercase()}_$timeStamp.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        try { cameraProvider.unbindAll() } catch (_: Exception) {}
                        continuation.resume(Uri.fromFile(photoFile).toString())
                    }

                    override fun onError(exc: ImageCaptureException) {
                        Log.e("SecurityCameraHelper", "Photo capture failed ($prefix)", exc)
                        try { cameraProvider.unbindAll() } catch (_: Exception) {}
                        continuation.resume(null)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("SecurityCameraHelper", "Camera bind failed ($prefix)", e)
            try { cameraProvider.unbindAll() } catch (_: Exception) {}
            continuation.resume(null)
        }
    }
}
