package com.example.logist_tech.inventory

import com.example.logist_tech.models.Producto
import java.time.LocalDate

/**
 * Singleton que mantiene la lista de movimientos de inventario en memoria.
 * Reemplaza InventarioTestData como fuente de verdad en tiempo de ejecución.
 */
object InventarioManager {

    private val _movimientos = mutableListOf<Producto>().apply {
        // Datos iniciales de ejemplo (igual que InventarioTestData)
        addAll(InventarioTestData.getDatosPrueba())
    }

    val movimientos: List<Producto> get() = _movimientos.toList()

    fun agregar(producto: Producto) {
        _movimientos.add(0, producto)
    }

    fun agregarMovimiento(nombre: String, cantidad: Int, tipo: String, categoria: String = "General", destino: String = "Sin destino") {
        _movimientos.add(
            0,
            Producto(
                id             = System.currentTimeMillis().toString(),
                nombre         = nombre,
                cantidad       = cantidad,
                pesoKg         = 0.0,
                categoria      = categoria,
                destino        = destino,
                estado         = "ok",
                tipoMovimiento = tipo,
                fecha          = LocalDate.now().toString()
            )
        )
    }

    fun limpiar() = _movimientos.clear()
}