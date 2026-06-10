package com.example.logist_tech.ocr

import org.json.JSONObject
import com.example.logist_tech.anomalias.AnomaliaType

/**
 * T-03 — OcrProcessor
 * Lógica principal del módulo OCR:
 *  1. Parsea el texto crudo del OCR en campos logísticos (nombre, cantidad, peso, destino)
 *  2. Parsea el JSON del QR en un QrData
 *  3. Compara ambos y genera un ResultadoComparacion para el módulo de Anomalías (T-04)
 */
object OcrProcessor {

    // ─────────────────────────────────────────────────────────────────
    // 1. PARSEAR TEXTO OCR → OcrData
    // ─────────────────────────────────────────────────────────────────

    /**
     * Extrae campos logísticos del texto crudo detectado por ML Kit OCR.
     *
     * Formatos de texto aceptados (insensible a mayúsculas):
     *   Producto: Coca Cola
     *   Cantidad: 24
     *   Peso: 1.5        ← opcional, no genera campoFaltante si no aparece
     *   Destino: Lima
     *
     * Si un campo obligatorio no se encuentra, se registra en [OcrData.camposFaltantes].
     * El peso NO es obligatorio — su ausencia no se penaliza.
     */
    fun parsearTextoOcr(textoOcr: String): OcrData {
        val camposFaltantes = mutableListOf<String>()

        val lineas = textoOcr.lines().map { it.trim() }

        val nombre   = extraerCampoTexto(lineas, listOf("producto", "nombre", "item"))
        val cantidad = extraerCampoNumeroEntero(lineas, listOf("cantidad", "qty", "unidades"))
        val peso     = extraerCampoNumeroDecimal(lineas, listOf("peso", "kg", "weight"))
        val destino  = extraerCampoTexto(lineas, listOf("destino", "destination", "para"))

        // El peso es opcional — no se agrega a camposFaltantes
        if (nombre.isBlank())  camposFaltantes.add("nombre")
        if (cantidad == 0)     camposFaltantes.add("cantidad")
        if (destino.isBlank()) camposFaltantes.add("destino")

        return OcrData(
            nombre          = nombre,
            cantidad        = cantidad,
            pesoKg          = peso,
            destino         = destino,
            textoOriginal   = textoOcr,
            camposFaltantes = camposFaltantes
        )
    }

    /** Busca la primera línea que empiece con alguna de las claves y extrae el valor como texto. */
    private fun extraerCampoTexto(lineas: List<String>, claves: List<String>): String {
        for (linea in lineas) {
            val lineaLower = linea.lowercase()
            for (clave in claves) {
                if (lineaLower.startsWith(clave)) {
                    val valor = linea.substringAfter(":", "").trim()
                    if (valor.isNotBlank()) return valor
                }
            }
        }
        return ""
    }

    /** Busca un valor numérico entero (ej: cantidad). */
    private fun extraerCampoNumeroEntero(lineas: List<String>, claves: List<String>): Int {
        val texto = extraerCampoTexto(lineas, claves)
        return Regex("\\d+").find(texto)?.value?.toIntOrNull() ?: 0
    }

    /** Busca un valor numérico decimal (ej: peso). */
    private fun extraerCampoNumeroDecimal(lineas: List<String>, claves: List<String>): Double {
        val texto = extraerCampoTexto(lineas, claves)
        return Regex("\\d+([.,]\\d+)?").find(texto)?.value?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. PARSEAR JSON DEL QR → QrData
    // ─────────────────────────────────────────────────────────────────

    /**
     * Parsea el JSON del código QR en un [QrData].
     * Formato completo: {"idCaja":"CJ-001","producto":"Laptop","cantidad":10,"destino":"Lima","peso":1.5}
     * Retorna null si el QR no tiene el formato correcto o está vacío.
     */
    fun parsearQr(qrJson: String): QrData? {
        if (qrJson.isBlank() || qrJson == "Esperando código QR...") return null
        return try {
            val json = JSONObject(qrJson)
            val idCaja = when {
                json.has("idCaja") -> json.getString("idCaja")
                json.has("id")     -> json.getString("id")
                else               -> ""
            }.trim()
            val nombre   = json.optString("producto", "").trim()
            val cantidad = json.optInt("cantidad", 0)
            val destino  = json.optString("destino", "").trim()
            val pesoKg   = json.optDouble("peso", 0.0)
            QrData(idCaja = idCaja, nombre = nombre, cantidad = cantidad, destino = destino, pesoKg = pesoKg)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Valida si el identificador de la caja está registrado en la API.
     *
     * NOTA PARA EL ENCARGADO DEL BLOQUE E:
     * Reemplazar esta simulación por una llamada real a:
     *   GET http://38.250.116.214/api/v1/cajas/{id}
     * Si retorna 404 → false. Si retorna 200 → true.
     *
     * Por ahora se acepta:
     *  - IDs que comiencen con "CJ-" (ej. CJ-001, CJ-002)
     *  - Lista de prueba predefinida
     */
    fun esCajaRegistradaEnApi(idCaja: String): Boolean {
        if (idCaja.isBlank()) return false
        val listaPrueba = setOf("1", "2", "3", "4", "BOX-001", "BOX-002")
        return idCaja.startsWith("CJ-", ignoreCase = true) || idCaja in listaPrueba
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. COMPARAR OCR vs QR → ResultadoComparacion (para T-04)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Compara los datos extraídos del OCR con los del QR y detecta anomalías.
     *
     * Tipos de anomalía posibles (ver AnomaliaType):
     *  - TEXTO_BORROSO           → OCR vacío Y sin QR válido
     *  - DATOS_INCOMPLETOS       → QR presente pero faltan campos obligatorios
     *  - CAJA_NO_REGISTRADA_EN_API → ID de caja no existe en la API
     *  - PRODUCTO_INEXISTENTE    → OCR no detectó nombre de producto
     *  - CANTIDAD_ERRONEA        → Cantidad inválida o diferente entre QR y OCR
     *  - QR_OCR_DIFERENTE        → Nombre de producto no coincide
     *  - SIN_ANOMALIA            → Todo OK
     */
    fun compararOcrConQr(ocrData: OcrData, qrData: QrData?): ResultadoComparacion {

        // ── Caso 1: OCR vacío → modo QR puro ─────────────────────────
        if (ocrData.textoOriginal.isBlank()) {
            if (qrData != null) {
                // Validar campos obligatorios: id, producto, cantidad y destino.
                // Sin destino no sabemos adónde va la caja → no se puede registrar.
                val faltantes = mutableListOf<String>()
                if (qrData.idCaja.isBlank())  faltantes.add("id")
                if (qrData.nombre.isBlank())  faltantes.add("producto")
                if (qrData.cantidad <= 0)     faltantes.add("cantidad")
                if (qrData.destino.isBlank()) faltantes.add("destino")

                if (faltantes.isNotEmpty()) {
                    return ResultadoComparacion(
                        hayAnomalia = true,
                        tipo        = AnomaliaType.DATOS_INCOMPLETOS,
                        descripcion = "El QR tiene campos obligatorios vacíos: ${faltantes.joinToString(", ")}.",
                        prioridad   = "ALTA",
                        ocrData     = ocrData,
                        qrData      = qrData
                    )
                }

                // Verificar registro en API
                if (!esCajaRegistradaEnApi(qrData.idCaja)) {
                    return ResultadoComparacion(
                        hayAnomalia = true,
                        tipo        = AnomaliaType.CAJA_NO_REGISTRADA_EN_API,
                        descripcion = "La caja '${qrData.idCaja}' no está registrada en la API (Error 404).",
                        prioridad   = "ALTA",
                        ocrData     = ocrData,
                        qrData      = qrData
                    )
                }

                return ResultadoComparacion(
                    hayAnomalia = false,
                    tipo        = AnomaliaType.SIN_ANOMALIA,
                    descripcion = "✅ Código QR leído correctamente. Todos los campos presentes.",
                    prioridad   = "BAJA",
                    ocrData     = ocrData,
                    qrData      = qrData
                )
            }
            // Sin QR válido y OCR vacío → documento ilegible
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.TEXTO_BORROSO,
                descripcion = "El OCR no pudo leer el documento. Intente mejorar el enfoque o la iluminación.",
                prioridad   = "ALTA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Caso 2: QR con campos obligatorios faltantes ──────────────
        val qrIncompleto = qrData != null &&
                (qrData.idCaja.isBlank() || qrData.nombre.isBlank() || qrData.cantidad <= 0)
        if (qrIncompleto) {
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.DATOS_INCOMPLETOS,
                descripcion = "Los datos del código QR están incompletos (requiere ID, producto y cantidad).",
                prioridad   = "ALTA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Caso 3: ID de caja no registrado en la API (simul. 404) ───
        if (qrData != null && !esCajaRegistradaEnApi(qrData.idCaja)) {
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.CAJA_NO_REGISTRADA_EN_API,
                descripcion = "La caja con ID '${qrData.idCaja}' no está registrada en la API (Error 404).",
                prioridad   = "ALTA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Caso 4: No se detectó nombre de producto en el OCR ────────
        if (ocrData.nombre.isBlank()) {
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.PRODUCTO_INEXISTENTE,
                descripcion = "No se detectó nombre de producto en el documento OCR.",
                prioridad   = "ALTA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Caso 5: Cantidad inválida en el OCR ───────────────────────
        if (ocrData.cantidad <= 0) {
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.CANTIDAD_ERRONEA,
                descripcion = "No se detectó cantidad o el valor es inválido en el documento OCR.",
                prioridad   = "MEDIA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Sin QR para comparar → OCR solo, sin anomalía ─────────────
        if (qrData == null) {
            return ResultadoComparacion(
                hayAnomalia = false,
                tipo        = AnomaliaType.SIN_ANOMALIA,
                descripcion = "OCR leído correctamente. Sin QR para comparar.",
                prioridad   = "BAJA",
                ocrData     = ocrData,
                qrData      = null
            )
        }

        // ── Caso 6: Nombre no coincide entre QR y OCR ─────────────────
        val nombreOcrNorm = ocrData.nombre.trim().lowercase()
        val nombreQrNorm  = qrData.nombre.trim().lowercase()
        if (!nombreOcrNorm.contains(nombreQrNorm) && !nombreQrNorm.contains(nombreOcrNorm)) {
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.QR_OCR_DIFERENTE,
                descripcion = "El producto del QR (\"${qrData.nombre}\") no coincide con el OCR (\"${ocrData.nombre}\").",
                prioridad   = "ALTA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Caso 7: Cantidad diferente entre QR y OCR ─────────────────
        if (qrData.cantidad != ocrData.cantidad) {
            return ResultadoComparacion(
                hayAnomalia = true,
                tipo        = AnomaliaType.CANTIDAD_ERRONEA,
                descripcion = "Cantidad QR: ${qrData.cantidad}  ≠  Cantidad OCR: ${ocrData.cantidad}.",
                prioridad   = "MEDIA",
                ocrData     = ocrData,
                qrData      = qrData
            )
        }

        // ── Todo OK ────────────────────────────────────────────────────
        return ResultadoComparacion(
            hayAnomalia = false,
            tipo        = AnomaliaType.SIN_ANOMALIA,
            descripcion = "✅ QR y OCR coinciden correctamente.",
            prioridad   = "BAJA",
            ocrData     = ocrData,
            qrData      = qrData
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. CONVERTIR OcrData → Producto (para StockManager / InventarioManager)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Convierte un [OcrData] en un [com.example.logist_tech.models.Producto]
     * listo para ser registrado en el inventario.
     * Si el OCR no trajo datos pero hay QR, usa los datos del QR como respaldo.
     */
    fun ocrDataToProducto(
        ocrData: OcrData,
        qrData: QrData? = null,
        tipoMovimiento: String = "ENTRADA"
    ): com.example.logist_tech.models.Producto {
        val nombre  = ocrData.nombre.ifBlank  { qrData?.nombre  ?: "Desconocido" }
        val cantidad = if (ocrData.cantidad > 0) ocrData.cantidad else (qrData?.cantidad ?: 0)
        val peso    = if (ocrData.pesoKg > 0.0)  ocrData.pesoKg  else (qrData?.pesoKg  ?: 0.0)
        val destino = ocrData.destino.ifBlank { qrData?.destino ?: "Sin destino" }

        return com.example.logist_tech.models.Producto(
            id             = System.currentTimeMillis().toString(),
            nombre         = nombre,
            cantidad       = cantidad,
            pesoKg         = peso,
            categoria      = "General",
            destino        = destino,
            estado         = if (ocrData.camposFaltantes.isEmpty()) "ok" else "incompleto",
            tipoMovimiento = tipoMovimiento,
            fecha          = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
        )
    }
}