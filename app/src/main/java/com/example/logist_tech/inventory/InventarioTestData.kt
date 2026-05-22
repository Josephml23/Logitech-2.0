package com.example.logist_tech.inventory

import com.example.logist_tech.models.Producto

object InventarioTestData {

    fun getDatosPrueba(): List<Producto> {
        return listOf(
            Producto("1", "Caja Roja",    10, 2.5, "Frágil", "Lima",     "ok", "ENTRADA", "2025-05-15"),
            Producto("2", "Caja Roja",     3, 2.5, "Frágil", "Lima",     "ok", "SALIDA",  "2025-05-16"),
            Producto("3", "Caja Azul",     8, 1.2, "Normal", "Cusco",    "ok", "ENTRADA", "2025-05-15"),
            Producto("4", "Caja Azul",     7, 1.2, "Normal", "Cusco",    "ok", "SALIDA",  "2025-05-17"),
            Producto("5", "Pallet Metal",  2, 5.0, "Pesado", "Arequipa", "ok", "ENTRADA", "2025-05-16")
        )
    }
}
/// InventarioTestData.kt es solo para pruebas :v
///Cuando la app esté completa y conectada a Firebase estos datos no se
// usarán, solo sirven ahora para desarrollar.
// :D