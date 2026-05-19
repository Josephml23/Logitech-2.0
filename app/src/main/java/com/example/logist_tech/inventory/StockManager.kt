package com.example.logist_tech.inventory

import com.example.logist_tech.models.Producto

object StockManager {

    fun calcularStockTotal(movimientos: List<Producto>): Map<String, Int> {
        val stockMap = mutableMapOf<String, Int>()
        for (producto in movimientos) {
            val actual = stockMap.getOrDefault(producto.nombre, 0)
            stockMap[producto.nombre] = when (producto.tipoMovimiento.uppercase()) {
                "ENTRADA" -> actual + producto.cantidad
                "SALIDA"  -> actual - producto.cantidad
                else      -> actual
            }
        }
        return stockMap
    }

    fun productosBajoStock(movimientos: List<Producto>, minimo: Int = 5): List<String> {
        return calcularStockTotal(movimientos)
            .filter { it.value < minimo }
            .keys.toList()
    }

    fun validarSalida(nombreProducto: String, cantidadSolicitada: Int, movimientos: List<Producto>): String {
        val stockActual = calcularStockTotal(movimientos)[nombreProducto] ?: 0
        return if (cantidadSolicitada > stockActual) {
            "Stock insuficiente"
        } else {
            "OK"
        }
    }
}