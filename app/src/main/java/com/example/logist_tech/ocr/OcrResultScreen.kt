package com.example.logist_tech.ocr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logist_tech.anomalias.AnomaliaManager
import com.example.logist_tech.anomalias.AnomaliaType
import com.example.logist_tech.history.HistorialManager
import com.example.logist_tech.history.TipoEvento
import com.example.logist_tech.inventory.InventarioManager
import com.example.logist_tech.models.Producto
import java.time.LocalDate

@Composable
fun OcrResultScreen(
    textoOcr: String,
    textoQr: String,
    imagenCapturada: Bitmap? = null,
    onRegistrarEnInventario: (Producto) -> Unit = {}
) {
    val esEscaneoQr = textoOcr.isBlank() && textoQr.isNotBlank()

    val ocrData  = remember(textoOcr) { OcrProcessor.parsearTextoOcr(textoOcr) }
    val qrData   = remember(textoQr)  { OcrProcessor.parsearQr(textoQr) }

    val resultado = remember(ocrData, qrData, esEscaneoQr) {
        if (esEscaneoQr) validarQr(qrData) else validarOcr(ocrData, qrData)
    }

    // Campos a mostrar
    val nombreMostrar = (if (esEscaneoQr) qrData?.nombre else ocrData.nombre)
        .orEmpty().ifBlank { "No detectado" }
    val cantidadMostrar = run {
        val n = if (esEscaneoQr) qrData?.cantidad ?: 0 else ocrData.cantidad
        if (n > 0) "$n" else "No detectado"
    }
    val pesoMostrar = run {
        val p = if (esEscaneoQr) qrData?.pesoKg ?: 0.0 else ocrData.pesoKg
        if (p > 0.0) "$p kg" else "No especificado"
    }
    val destinoMostrar = (if (esEscaneoQr) qrData?.destino else ocrData.destino)
        .orEmpty().ifBlank { "No detectado" }

    // El peso es OPCIONAL — nunca se marca en rojo como campo crítico
    val pesoCritico = false

    // Campos críticos faltantes (para colorear en rojo)
    val camposCriticosFaltantes = remember(resultado) {
        if (resultado.hayAnomalia && resultado.tipo == AnomaliaType.DATOS_INCOMPLETOS) {
            resultado.descripcion
                .substringAfter("Faltan: ", "")
                .substringBefore(".", "")
                .split(", ")
                .map { it.trim().lowercase() }
        } else emptyList()
    }

    val nombreCritico   = "producto" in camposCriticosFaltantes
    val cantidadCritica = "cantidad" in camposCriticosFaltantes
    val destinoCritico  = "destino" in camposCriticosFaltantes

    // Valores para registro
    val idCajaRegistro   = qrData?.idCaja.orEmpty().ifBlank { nombreMostrar }
    val nombreRegistro   = (if (esEscaneoQr) qrData?.nombre else ocrData.nombre)
        .orEmpty().ifBlank { "Desconocido" }
    val cantidadRegistro = if (esEscaneoQr) qrData?.cantidad ?: 0 else ocrData.cantidad
    val destinoRegistro  = (if (esEscaneoQr) qrData?.destino else ocrData.destino).orEmpty()
    val pesoRegistro     = if (esEscaneoQr) qrData?.pesoKg ?: 0.0 else ocrData.pesoKg

    // FIX: flag para evitar registrar la anomalía múltiples veces
    var yaRegistroAnomalia by remember { mutableStateOf(false) }

    LaunchedEffect(resultado) {
        if (!yaRegistroAnomalia &&
            resultado.hayAnomalia &&
            resultado.tipo != AnomaliaType.SIN_ANOMALIA) {
            yaRegistroAnomalia = true
            AnomaliaManager.registrarDesdeResultado(resultado)
            HistorialManager.agregar(
                idCaja      = idCajaRegistro,
                producto    = nombreRegistro.ifBlank { "Desconocido" },
                cantidad    = cantidadRegistro,
                tipoEvento  = TipoEvento.ANOMALIA,
                descripcion = resultado.descripcion
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Resultado del Escaneo",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF123B6D)
        )
        Text(
            text = if (esEscaneoQr) "Modo: Escaneo QR" else "Modo: OCR / Foto",
            fontSize = 13.sp,
            color = Color.Gray
        )

        // F-01: Preview de la foto (solo modo OCR, nunca en modo QR)
        if (!esEscaneoQr && imagenCapturada != null) {
            Image(
                bitmap = imagenCapturada.asImageBitmap(),
                contentDescription = "Foto OCR capturada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE7EAF0))
            )
        }

        // Campos detectados
        CampoCard("Producto",    nombreMostrar,    esCritico = nombreCritico)
        CampoCard("Cantidad",    cantidadMostrar,  esCritico = cantidadCritica)
        // Peso: nunca crítico, color neutro si no se detectó
        CampoCard("Peso (kg)",   pesoMostrar,      esCritico = pesoCritico, esOpcional = true)
        CampoCard("Destino",     destinoMostrar,   esCritico = destinoCritico)

        // Resultado de validación
        AnomaliaCard(resultado = resultado)

        // Botón registrar — si no hay anomalía, o si la anomalía es BAJA (solo falta peso)
        if (!resultado.hayAnomalia || resultado.prioridad == "BAJA") {
            Button(
                onClick = {
                    val producto = Producto(
                        id             = System.currentTimeMillis().toString(),
                        nombre         = nombreRegistro,
                        cantidad       = cantidadRegistro,
                        pesoKg         = pesoRegistro,
                        categoria      = "General",
                        destino        = destinoRegistro.ifBlank { "Sin destino" },
                        estado         = "ok",
                        tipoMovimiento = "ENTRADA",
                        fecha          = LocalDate.now().toString()
                    )
                    InventarioManager.agregar(producto)
                    HistorialManager.agregar(
                        idCaja      = idCajaRegistro,
                        producto    = nombreRegistro,
                        cantidad    = cantidadRegistro,
                        tipoEvento  = TipoEvento.INGRESO,
                        descripcion = "Caja registrada correctamente en inventario"
                    )
                    onRegistrarEnInventario(producto)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Registrar en Inventario", color = Color.White)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// VALIDACIÓN MODO QR
// ══════════════════════════════════════════════════════════════
private fun validarQr(qrData: QrData?): ResultadoComparacion {
    val ocrVacio = OcrData(
        nombre = "", cantidad = 0, pesoKg = 0.0, destino = "",
        textoOriginal = "QR_MODE", camposFaltantes = emptyList()
    )

    if (qrData == null) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.DATOS_INCOMPLETOS,
            descripcion = "No se pudo leer el QR. Verifica que el código sea válido y esté bien iluminado.",
            prioridad   = "ALTA",
            ocrData     = ocrVacio,
            qrData      = null
        )
    }

    // ALTA — faltan campos críticos: id o producto
    val camposCriticos = mutableListOf<String>()
    if (qrData.idCaja.isBlank()) camposCriticos.add("id")
    if (qrData.nombre.isBlank()) camposCriticos.add("producto")

    if (camposCriticos.isNotEmpty()) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.DATOS_INCOMPLETOS,
            descripcion = "El QR tiene campos críticos vacíos: ${camposCriticos.joinToString(", ")}. " +
                    "Formato requerido: {\"id\":\"CJ-001\",\"producto\":\"...\",\"cantidad\":10,\"destino\":\"...\"}",
            prioridad   = "ALTA",
            ocrData     = ocrVacio,
            qrData      = qrData
        )
    }

    // ALTA — caja no registrada en API
    if (!OcrProcessor.esCajaRegistradaEnApi(qrData.idCaja)) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.CAJA_NO_REGISTRADA_EN_API,
            descripcion = "La caja '${qrData.idCaja}' no está registrada en el sistema (Error 404). " +
                    "Solo se aceptan IDs que comiencen con 'CJ-' o estén en la lista de la API.",
            prioridad   = "ALTA",
            ocrData     = ocrVacio,
            qrData      = qrData
        )
    }

    // MEDIA — cantidad inválida o cero (id y producto ya existen, solo falta cantidad)
    if (qrData.cantidad <= 0) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.CANTIDAD_ERRONEA,
            descripcion = "La cantidad del QR es inválida o está en cero. Debe ser un número mayor a 0.",
            prioridad   = "MEDIA",
            ocrData     = ocrVacio,
            qrData      = qrData
        )
    }

    // MEDIA — falta destino (id, producto y cantidad OK, pero destino vacío)
    if (qrData.destino.isBlank()) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.DATOS_INCOMPLETOS,
            descripcion = "El QR no tiene el campo 'destino'. El producto quedará sin destino asignado.",
            prioridad   = "MEDIA",
            ocrData     = ocrVacio,
            qrData      = qrData
        )
    }

    // BAJA — todos los campos obligatorios OK pero falta el peso (opcional)
    if (qrData.pesoKg <= 0.0) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.DATOS_INCOMPLETOS,
            descripcion = "Campos obligatorios completos, pero falta el peso (opcional).",
            prioridad   = "BAJA",
            ocrData     = ocrVacio,
            qrData      = qrData
        )
    }

    // SIN ANOMALÍA — todo presente y válido incluyendo peso
    return ResultadoComparacion(
        hayAnomalia = false,
        tipo        = AnomaliaType.SIN_ANOMALIA,
        descripcion = "QR leído correctamente. Todos los campos están presentes y son válidos.",
        prioridad   = "",
        ocrData     = ocrVacio,
        qrData      = qrData
    )
}

// ══════════════════════════════════════════════════════════════
// VALIDACIÓN MODO OCR
// ══════════════════════════════════════════════════════════════
private fun validarOcr(ocrData: OcrData, qrData: QrData?): ResultadoComparacion {
    if (ocrData.textoOriginal.isBlank()) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.TEXTO_BORROSO,
            descripcion = "No se pudo leer texto del documento. " +
                    "Asegúrate de tener buena iluminación y el documento bien enfocado.",
            prioridad   = "ALTA",
            ocrData     = ocrData,
            qrData      = qrData
        )
    }

    // ALTA — falta producto (sin nombre no se puede identificar qué es la caja)
    if (ocrData.nombre.isBlank()) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.PRODUCTO_INEXISTENTE,
            descripcion = "No se detectó el nombre del producto. " +
                    "El documento debe incluir la etiqueta 'Producto:'.",
            prioridad   = "ALTA",
            ocrData     = ocrData,
            qrData      = qrData
        )
    }

    // MEDIA — falta cantidad (producto identificado pero sin cantidad)
    if (ocrData.cantidad <= 0) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.CANTIDAD_ERRONEA,
            descripcion = "No se detectó la cantidad o su valor es inválido. " +
                    "El documento debe incluir la etiqueta 'Cantidad:' con un número mayor a 0.",
            prioridad   = "MEDIA",
            ocrData     = ocrData,
            qrData      = qrData
        )
    }

    // MEDIA — falta destino (producto y cantidad OK, pero sin destino)
    if (ocrData.destino.isBlank()) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.DATOS_INCOMPLETOS,
            descripcion = "No se detectó el destino. " +
                    "El documento debe incluir la etiqueta 'Destino:'.",
            prioridad   = "MEDIA",
            ocrData     = ocrData,
            qrData      = qrData
        )
    }

    // Si hay QR, comparar producto y cantidad entre OCR y QR
    if (qrData != null) {
        return OcrProcessor.compararOcrConQr(ocrData, qrData)
    }

    // BAJA — obligatorios OK pero falta el peso (opcional) — se guarda en anomalías pero permite registrar
    if (ocrData.pesoKg <= 0.0) {
        return ResultadoComparacion(
            hayAnomalia = true,
            tipo        = AnomaliaType.DATOS_INCOMPLETOS,
            descripcion = "Campos obligatorios completos, pero falta el peso (opcional).",
            prioridad   = "BAJA",
            ocrData     = ocrData,
            qrData      = null
        )
    }

    // SIN ANOMALÍA — todo completo incluyendo peso
    return ResultadoComparacion(
        hayAnomalia = false,
        tipo        = AnomaliaType.SIN_ANOMALIA,
        descripcion = "Documento leído correctamente. Todos los campos están presentes.",
        prioridad   = "",
        ocrData     = ocrData,
        qrData      = null
    )
}

// ══════════════════════════════════════════════════════════════
// COMPOSABLES
// ══════════════════════════════════════════════════════════════

@Composable
private fun CampoCard(
    titulo: String,
    valor: String,
    esCritico: Boolean = false,
    esOpcional: Boolean = false
) {
    // Rojo solo si el campo es crítico Y falta
    // Gris suave si es opcional y no está (peso)
    // Normal en cualquier otro caso
    val noDetectado = valor == "No detectado" || valor == "No especificado"
    val containerColor = when {
        esCritico && noDetectado -> Color(0xFFFFF3F3)
        esOpcional && noDetectado -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        esCritico && noDetectado -> Color(0xFFE53935)
        esOpcional && noDetectado -> Color(0xFF888888)
        else -> Color.Unspecified
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(titulo, fontSize = 13.sp, color = Color.Gray)
            Text(
                text       = valor,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = textColor
            )
        }
    }
}

@Composable
private fun AnomaliaCard(resultado: ResultadoComparacion) {
    val bgColor = when {
        !resultado.hayAnomalia || resultado.prioridad == "BAJA" -> Color(0xFFE8F5E9)
        resultado.prioridad == "ALTA"                           -> Color(0xFFFFEBEE)
        else                                                    -> Color(0xFFFFF8E1)
    }
    val textColor = when {
        !resultado.hayAnomalia || resultado.prioridad == "BAJA" -> Color(0xFF2E7D32)
        resultado.prioridad == "ALTA"                           -> Color(0xFFB71C1C)
        else                                                    -> Color(0xFFF57F17)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text       = resultado.tipo.replace("_", " "),
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                color      = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(resultado.descripcion, fontSize = 13.sp, color = textColor)
            if (resultado.hayAnomalia) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Prioridad: ${resultado.prioridad}",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = textColor
                )
            }
        }
    }
}