package com.example.logist_tech

import com.example.logist_tech.inventory.InventarioTestData
import com.example.logist_tech.inventory.StockManager
import org.junit.Test
import org.junit.Assert.*

class StockManagerTest {

    private val movimientos = InventarioTestData.getDatosPrueba()

    @Test
    fun `stock de Caja Roja debe ser 7`() {
        val stock = StockManager.calcularStockTotal(movimientos)
        assertEquals(7, stock["Caja Roja"])
    }

    @Test
    fun `stock de Caja Azul debe ser 1`() {
        val stock = StockManager.calcularStockTotal(movimientos)
        assertEquals(1, stock["Caja Azul"])
    }

    @Test
    fun `Pallet Metal debe estar en bajo stock`() {
        val bajoStock = StockManager.productosBajoStock(movimientos, minimo = 5)
        assertTrue(bajoStock.contains("Pallet Metal"))
    }

    @Test
    fun `validar salida con stock insuficiente`() {
        val resultado = StockManager.validarSalida("Caja Azul", 10, movimientos)
        assertEquals("Stock insuficiente", resultado)
    }

    @Test
    fun `validar salida con stock suficiente`() {
        val resultado = StockManager.validarSalida("Caja Roja", 5, movimientos)
        assertEquals("OK", resultado)
    }
}