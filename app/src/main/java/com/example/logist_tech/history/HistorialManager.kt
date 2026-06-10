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
    val idCaja:      String,
    val producto:    String,
    val cantidad:    Int,
    val fechaHora:   String,   // formato: "dd/MM/yyyy HH:mm"
    val tipoEvento:  TipoEvento,
    val descripcion: String
)

object HistorialManager {

    private val _historial = mutableListOf<EntradaHistorial>()

    val historial: List<EntradaHistorial> get() = _historial.toList()

    fun agregar(
        idCaja:      String,
        producto:    String,
        cantidad:    Int,
        tipoEvento:  TipoEvento,
        descripcion: String
    ) {
        _historial.add(
            0,
            EntradaHistorial(
                idCaja      = idCaja,
                producto    = producto,
                cantidad    = cantidad,
                fechaHora   = fechaActual(),
                tipoEvento  = tipoEvento,
                descripcion = descripcion
            )
        )
    }

    fun totalEventos(): Int = _historial.size

    fun limpiar() = _historial.clear()

    private fun fechaActual(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
}