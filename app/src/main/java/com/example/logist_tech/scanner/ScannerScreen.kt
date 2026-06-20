package com.example.logist_tech.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class ScanMode { QR, OCR }

@Composable
fun ScannerScreen(
    onNavigarResultado: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Executor persistente para análisis de imagen
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Estados para entrada manual
    var showManualDialog by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        // Limpiar resultados anteriores
        ScannerResultHolder.textoQr = ""
        ScannerResultHolder.textoOcr = ""
        ScannerResultHolder.imagenBitmap = null
    }

    // Diálogo de entrada manual para pruebas de API
    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text("Simular Escaneo (Prueba API)") },
            text = {
                Column {
                    Text("Escribe el texto que debería tener el QR para probar la conexión.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { manualText = it },
                        label = { Text("Contenido del QR") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualText.isNotBlank()) {
                            showManualDialog = false
                            ScannerResultHolder.textoQr = manualText
                            onNavigarResultado()
                        }
                    }
                ) { Text("PROBAR API") }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    // Liberar recursos al salir
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                onQrDetected = { qrText ->
                    // Guardar y navegar en el hilo principal
                    ScannerResultHolder.textoQr = qrText
                    onNavigarResultado()
                }
            )
            
            // UI Overlay con opción de entrada manual
            ScannerOverlayUI(onManualClick = { showManualDialog = true })
            
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Se requiere permiso de cámara", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showManualDialog = true }) {
                        Text("ENTRADA MANUAL (MODO PRUEBA)")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var yaProcesado by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // 1. Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // 2. Analizador de QR
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    procesarFrameQR(imageProxy, barcodeScanner) { result ->
                        if (!yaProcesado) {
                            yaProcesado = true
                            // Navegar al hilo principal
                            ContextCompat.getMainExecutor(context).execute {
                                onQrDetected(result)
                            }
                        }
                    }
                }

                // 3. Selector de cámara
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("Scanner", "Error al vincular CameraX", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = modifier
    )
    
    // Cerrar scanner al destruir
    DisposableEffect(Unit) {
        onDispose { barcodeScanner.close() }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun procesarFrameQR(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onFound: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val qr = barcodes.firstOrNull()?.rawValue
                if (qr != null) {
                    onFound(qr)
                }
            }
            .addOnFailureListener { Log.e("MLKit", "Error", it) }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

@Composable
fun ScannerOverlayUI(onManualClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Botón de prueba manual en la esquina superior
        IconButton(
            onClick = onManualClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Entrada Manual",
                tint = Color.White
            )
        }

        // Marco de enfoque
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        )
        
        // Texto inferior
        Text(
            text = "Posiciona el código QR o usa el botón de teclado para modo texto",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        )
    }
}
