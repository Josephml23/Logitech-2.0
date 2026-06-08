package com.example.logist_tech.history

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TipoEvento {
    INGRESO,
    ANOMALIA,
    DESPACHO
}

data class EntradaHistorial(
    val idCaja: String,
    val producto: String,
    val cantidad: Int,
    val fechaHora: String,
    val tipoEvento: TipoEvento,
    val descripcion: String
)

object HistorialManager {

    private val _historial = mutableListOf<EntradaHistorial>()

    init {
        agregar("CJ-001", "Laptop Lenovo", 5, TipoEvento.INGRESO, "Caja registrada correctamente")
        agregar("CJ-002", "Mouse Logitech", 3, TipoEvento.ANOMALIA, "Cantidad no coincide con QR")
    }

    val historial: List<EntradaHistorial> get() = _historial.toList()

    fun agregar(
        idCaja: String,
        producto: String,
        cantidad: Int,
        tipoEvento: TipoEvento,
        descripcion: String
    ) {
        val entrada = EntradaHistorial(
            idCaja      = idCaja,
            producto    = producto,
            cantidad    = cantidad,
            fechaHora   = obtenerFechaActual(),
            tipoEvento  = tipoEvento,
            descripcion = descripcion
        )
        _historial.add(0, entrada)
    }

    fun totalEventos(): Int = _historial.size

    fun limpiar() = _historial.clear()

    private fun obtenerFechaActual(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
}