package com.example.logist_tech.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.graphics.BitmapFactory
import java.io.File
import java.util.concurrent.Executors

enum class ScanMode { QR, OCR }

@Composable
fun ScannerScreen(
    onNavigarResultado: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanMode by remember { mutableStateOf(ScanMode.QR) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    var procesandoFoto by remember { mutableStateOf(false) }
    var yaNavego by remember { mutableStateOf(false) }
    var qrDetectado by remember { mutableStateOf("") }

    // FIX: limpiar el holder completo al entrar al scanner
    // Así nunca se muestra una foto de un escaneo anterior
    LaunchedEffect(Unit) {
        ScannerResultHolder.textoOcr = ""
        ScannerResultHolder.textoQr = ""
        ScannerResultHolder.imagenBitmap = null
        yaNavego = false
        qrDetectado = ""
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(qrDetectado) {
        if (qrDetectado.isNotBlank() && !yaNavego) {
            yaNavego = true
            ScannerResultHolder.textoQr = qrDetectado
            ScannerResultHolder.textoOcr = ""
            ScannerResultHolder.imagenBitmap = null  // FIX: nunca pasar foto al modo QR
            onNavigarResultado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageCapture = ImageCapture.Builder().build()
                        imageCaptureUseCase = imageCapture

                        val barcodeScanner = BarcodeScanning.getClient()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (scanMode == ScanMode.QR && !yaNavego) {
                                procesarQR(
                                    imageProxy     = imageProxy,
                                    barcodeScanner = barcodeScanner,
                                    onQrDetected   = { qrText -> qrDetectado = qrText }
                                )
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("CameraX", "Error al iniciar camara", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Selector QR / OCR arriba
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        scanMode = ScanMode.QR
                        yaNavego = false
                        qrDetectado = ""
                        // FIX: limpiar bitmap al cambiar a QR
                        ScannerResultHolder.imagenBitmap = null
                        ScannerResultHolder.textoOcr = ""
                        ScannerResultHolder.textoQr = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (scanMode == ScanMode.QR) Color(0xFF2980B9) else Color.Gray
                    ),
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("QR", color = Color.White)
                }
                Button(
                    onClick = {
                        scanMode = ScanMode.OCR
                        // FIX: limpiar QR al cambiar a OCR
                        qrDetectado = ""
                        yaNavego = false
                        ScannerResultHolder.textoQr = ""
                        ScannerResultHolder.textoOcr = ""
                        ScannerResultHolder.imagenBitmap = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (scanMode == ScanMode.OCR) Color(0xFF2980B9) else Color.Gray
                    ),
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("OCR", color = Color.White)
                }
            }

            // Recuadro de enfoque QR
            if (scanMode == ScanMode.QR) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .align(Alignment.Center)
                ) {
                    val cornerColor = if (qrDetectado.isNotBlank()) Color(0xFF4CAF50) else Color.White
                    val cornerSize  = 24.dp
                    val cornerWidth = 4.dp

                    Box(modifier = Modifier.size(cornerSize).align(Alignment.TopStart)
                        .border(width = cornerWidth, color = cornerColor,
                            shape = RoundedCornerShape(topStart = 8.dp)))
                    Box(modifier = Modifier.size(cornerSize).align(Alignment.TopEnd)
                        .border(width = cornerWidth, color = cornerColor,
                            shape = RoundedCornerShape(topEnd = 8.dp)))
                    Box(modifier = Modifier.size(cornerSize).align(Alignment.BottomStart)
                        .border(width = cornerWidth, color = cornerColor,
                            shape = RoundedCornerShape(bottomStart = 8.dp)))
                    Box(modifier = Modifier.size(cornerSize).align(Alignment.BottomEnd)
                        .border(width = cornerWidth, color = cornerColor,
                            shape = RoundedCornerShape(bottomEnd = 8.dp)))
                }
            }

            // HUD inferior
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (scanMode == ScanMode.QR) {
                    Text(
                        text = if (qrDetectado.isNotBlank()) "QR detectado, procesando..."
                        else "Centra el codigo QR en el recuadro",
                        color = if (qrDetectado.isNotBlank()) Color(0xFF4CAF50) else Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Apunta la camara al documento y toma la foto",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            procesandoFoto = true
                            tomarFotoYProcesar(
                                imageCapture = imageCaptureUseCase,
                                context      = context,
                                onResultado  = { textoOcr ->
                                    procesandoFoto = false
                                    ScannerResultHolder.textoOcr = textoOcr
                                    ScannerResultHolder.textoQr  = ""
                                    onNavigarResultado()
                                },
                                onError = {
                                    procesandoFoto = false
                                    Log.e("OCR_FOTO", "Error al procesar foto", it)
                                }
                            )
                        },
                        enabled = !procesandoFoto,
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9))
                    ) {
                        if (procesandoFoto) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Tomar Foto", color = Color.White)
                        }
                    }
                }
            }

        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Se requiere permiso de la camara para escanear.")
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun procesarQR(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { onQrDetected(it) }
                }
            }
            .addOnFailureListener { Log.e("MLKit_QR", "Error QR", it) }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

private fun tomarFotoYProcesar(
    imageCapture: ImageCapture?,
    context: android.content.Context,
    onResultado: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    if (imageCapture == null) {
        onError(Exception("ImageCapture no disponible"))
        return
    }

    val archivoTemp = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(archivoTemp).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                try {
                    val bitmap = BitmapFactory.decodeFile(archivoTemp.absolutePath)
                    if (bitmap == null) {
                        onError(Exception("No se pudo leer la imagen guardada"))
                        return
                    }
                    ScannerResultHolder.imagenBitmap = bitmap

                    val inputImage = InputImage.fromFilePath(
                        context,
                        android.net.Uri.fromFile(archivoTemp)
                    )
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        .process(inputImage)
                        .addOnSuccessListener { visionText -> onResultado(visionText.text) }
                        .addOnFailureListener { onError(it) }
                        .addOnCompleteListener { archivoTemp.delete() }
                } catch (e: Exception) {
                    onError(e)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}