package com.example.logist_tech.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.logist_tech.models.Producto
import com.example.logist_tech.ui.theme.Logist_TechTheme

// ─────────────────────────────────────────────
// PANTALLA PRINCIPAL DE INVENTARIO  (T05)
// Usa StockManager de Katherine para calcular stock
// ─────────────────────────────────────────────

@Composable
fun InventarioScreen() {

    // Estado de la lista de movimientos (por ahora con datos de prueba de Katherine)
    var movimientos by remember { mutableStateOf(InventarioTestData.getDatosPrueba()) }

    // Filtro seleccionado: null = sin filtro
    var filtroActivo by remember { mutableStateOf<String?>(null) }

    // Controla si se muestra el diálogo de nuevo movimiento
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Stock calculado con la lógica de Katherine
    val stockActual = StockManager.calcularStockTotal(movimientos)
    val productosBajos = StockManager.productosBajoStock(movimientos)

    // Categorías únicas disponibles para filtrar
    val categorias = movimientos.map { it.categoria }.distinct().sorted()

    // Destinos únicos disponibles para filtrar
    val destinos = movimientos.map { it.destino }.distinct().sorted()

    // Lista de items filtrados para mostrar
    val itemsFiltrados = if (filtroActivo == null) {
        stockActual.entries.toList()
    } else {
        // Filtra por categoría O por destino según el filtro activo
        val nombresFiltrados = movimientos
            .filter { it.categoria == filtroActivo || it.destino == filtroActivo }
            .map { it.nombre }
            .distinct()
        stockActual.entries.filter { it.key in nombresFiltrados }
    }

    Scaffold(
        topBar = { InventarioTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar movimiento")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // ── Chips de filtro ──
            FiltroChips(
                categorias = categorias,
                destinos = destinos,
                filtroActivo = filtroActivo,
                onFiltroSeleccionado = { nuevo ->
                    filtroActivo = if (filtroActivo == nuevo) null else nuevo
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Resumen total ──
            ResumenStock(totalProductos = stockActual.size, bajoStock = productosBajos.size)

            Spacer(modifier = Modifier.height(12.dp))

            // ── Lista de productos ──
            if (itemsFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin productos para este filtro", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(itemsFiltrados) { entry ->
                        val esBajo = entry.key in productosBajos
                        // Buscar datos extra del último movimiento de este producto
                        val detalle = movimientos.lastOrNull { it.nombre == entry.key }
                        ProductoCard(
                            nombre = entry.key,
                            stock = entry.value,
                            categoria = detalle?.categoria ?: "—",
                            destino = detalle?.destino ?: "—",
                            bajoBstock = esBajo
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // espacio para FAB
                }
            }
        }
    }

    // ── Diálogo para registrar entrada/salida manual ──
    if (mostrarDialogo) {
        RegistroMovimientoDialog(
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { nuevoProducto ->
                movimientos = movimientos + nuevoProducto
                mostrarDialogo = false
            }
        )
    }
}

// ─────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "📦 Inventario",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

// ─────────────────────────────────────────────
// CHIPS DE FILTRO (categoría o destino)
// ─────────────────────────────────────────────

@Composable
fun FiltroChips(
    categorias: List<String>,
    destinos: List<String>,
    filtroActivo: String?,
    onFiltroSeleccionado: (String) -> Unit
) {
    Column {
        Text("Filtrar por categoría:", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categorias.forEach { cat ->
                FilterChip(
                    selected = filtroActivo == cat,
                    onClick = { onFiltroSeleccionado(cat) },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Filtrar por destino:", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            destinos.forEach { dest ->
                FilterChip(
                    selected = filtroActivo == dest,
                    onClick = { onFiltroSeleccionado(dest) },
                    label = { Text(dest, fontSize = 12.sp) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// CARD DE RESUMEN
// ─────────────────────────────────────────────

@Composable
fun ResumenStock(totalProductos: Int, bajoStock: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ResumenChip(
            modifier = Modifier.weight(1f),
            label = "Productos",
            valor = "$totalProductos",
            color = MaterialTheme.colorScheme.primaryContainer
        )
        ResumenChip(
            modifier = Modifier.weight(1f),
            label = "Bajo stock",
            valor = "$bajoStock",
            color = if (bajoStock > 0) Color(0xFFFFE0E0) else Color(0xFFE0FFE8)
        )
    }
}

@Composable
fun ResumenChip(modifier: Modifier = Modifier, label: String, valor: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// ─────────────────────────────────────────────
// CARD DE PRODUCTO
// ─────────────────────────────────────────────

@Composable
fun ProductoCard(
    nombre: String,
    stock: Int,
    categoria: String,
    destino: String,
    bajoBstock: Boolean
) {
    val borderColor = if (bajoBstock) Color(0xFFE53935) else Color.Transparent
    val stockColor  = if (bajoBstock) Color(0xFFE53935) else Color(0xFF2E7D32)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bajoBstock) Color(0xFFFFF3F3) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color lateral
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(50.dp)
                    .background(
                        color = stockColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("📁 $categoria  |  📍 $destino", fontSize = 12.sp, color = Color.Gray)
                if (bajoBstock) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ Stock bajo",
                        fontSize = 11.sp,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Stock en grande
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$stock",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = stockColor
                )
                Text("unidades", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
fun RegistroMovimientoDialog(
    onDismiss: () -> Unit,
    onConfirmar: (Producto) -> Unit
) {
    var nombre     by remember { mutableStateOf("") }
    var cantidad   by remember { mutableStateOf("") }
    var categoria  by remember { mutableStateOf("") }
    var destino    by remember { mutableStateOf("") }
    var tipoMovimiento by remember { mutableStateOf("ENTRADA") }  // ENTRADA o SALIDA

    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Registrar movimiento",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // Selector ENTRADA / SALIDA
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ENTRADA", "SALIDA").forEach { tipo ->
                        FilterChip(
                            selected = tipoMovimiento == tipo,
                            onClick = { tipoMovimiento = tipo },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (tipo == "ENTRADA") Icons.Default.Add else Icons.Default.Remove,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(tipo)
                                }
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría (ej: Frágil, Normal)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = destino,
                    onValueChange = { destino = it },
                    label = { Text("Destino (ej: Lima, Cusco)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFE53935), fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        // Validación básica
                        val cantInt = cantidad.toIntOrNull()
                        when {
                            nombre.isBlank()    -> errorMsg = "El nombre es obligatorio"
                            cantInt == null || cantInt <= 0 -> errorMsg = "Cantidad inválida"
                            categoria.isBlank() -> errorMsg = "La categoría es obligatoria"
                            destino.isBlank()   -> errorMsg = "El destino es obligatorio"
                            else -> {
                                val nuevo = Producto(
                                    id             = System.currentTimeMillis().toString(),
                                    nombre         = nombre.trim(),
                                    cantidad       = cantInt,
                                    pesoKg         = 0.0,   // no requerido aquí
                                    categoria      = categoria.trim(),
                                    destino        = destino.trim(),
                                    estado         = "ok",
                                    tipoMovimiento = tipoMovimiento,
                                    fecha          = java.time.LocalDate.now().toString()
                                )
                                onConfirmar(nuevo)
                            }
                        }
                    }) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun InventarioScreenPreview() {
    Logist_TechTheme {
        InventarioScreen()
    }
}