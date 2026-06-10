package com.example.logist_tech.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logist_tech.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val AzulLogis  = Color(0xFF2980B9)
private val AzulOscuro = Color(0xFF123B6D)
private val FondoBlanco = Color(0xFFFFFFFF)

@Composable
fun HistoryScreen(onNavigateBack: () -> Unit = {}) {

    // Filtro de tipo: null = todos
    var selectedFilter    by remember { mutableStateOf<String?>(null) }
    var searchQuery       by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf<String?>(null) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    // Refrescar la lista cada vez que cambia el historial
    var historial by remember { mutableStateOf(HistorialManager.historial) }

    // Mapeamos TipoEvento → String de filtro
    fun tipoTexto(t: TipoEvento) = when (t) {
        TipoEvento.INGRESO  -> "Entrada"
        TipoEvento.DESPACHO -> "Salida"
        TipoEvento.ANOMALIA -> "Anomalia"
    }

    val filteredList = historial.filter { entrada ->
        val tipo = tipoTexto(entrada.tipoEvento)
        // Filtro tipo
        (selectedFilter == null || tipo == selectedFilter) &&
                // Filtro fecha: fechaHora = "dd/MM/yyyy HH:mm", comparar solo la parte de fecha
                (fechaSeleccionada == null || entrada.fechaHora.startsWith(fechaSeleccionada!!)) &&
                // Filtro búsqueda
                entrada.producto.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize().background(FondoBlanco).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding  = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.height(10.dp))

                // Botón atrás
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint               = AzulLogis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter            = painterResource(id = R.drawable.ic_logo_logis),
                    contentDescription = "Logo",
                    modifier           = Modifier.fillMaxWidth().height(130.dp),
                    contentScale       = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text       = "Historial",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AzulOscuro
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Búsqueda
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Buscar producto...", color = Color.Gray) },
                    trailingIcon  = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = AzulLogis)
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(50.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Color(0xFFE7EAF0),
                        unfocusedContainerColor = Color(0xFFE7EAF0),
                        focusedBorderColor      = AzulLogis,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedTextColor        = Color.Black,
                        unfocusedTextColor      = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filtro fecha
                OutlinedTextField(
                    value         = fechaSeleccionada ?: "Todas las fechas",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Fecha", color = AzulOscuro) },
                    trailingIcon  = {
                        IconButton(onClick = { mostrarCalendario = true }) {
                            Icon(
                                imageVector        = Icons.Default.CalendarToday,
                                contentDescription = "Calendario",
                                tint               = AzulLogis
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = OutlinedTextFieldDefaults.colors(
                        focusedTextColor    = Color.Black,
                        unfocusedTextColor  = Color.Black,
                        focusedBorderColor  = AzulLogis,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                if (mostrarCalendario) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { mostrarCalendario = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { ms ->
                                    // Guardar en formato "dd/MM/yyyy" igual que HistorialManager
                                    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .apply { timeZone = TimeZone.getTimeZone("UTC") }
                                    fechaSeleccionada = fmt.format(java.util.Date(ms))
                                }
                                mostrarCalendario = false
                            }) { Text("Aceptar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
                        }
                    ) { DatePicker(state = datePickerState) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Chips de filtro: Entrada | Salida | Anomalia ──────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    items(listOf("Entrada", "Salida", "Anomalia")) { tipo ->
                        val colorChip = when (tipo) {
                            "Entrada"  -> Color(0xFF2E7D32)
                            "Salida"   -> Color(0xFFC62828)
                            else       -> Color(0xFFE65100)
                        }
                        FilterButton(
                            text     = tipo,
                            selected = selectedFilter == tipo,
                            color    = colorChip,
                            onClick  = {
                                selectedFilter = if (selectedFilter == tipo) null else tipo
                            }
                        )
                    }

                    // Botón limpiar — aparece cuando hay algún filtro activo
                    if (selectedFilter != null || fechaSeleccionada != null || searchQuery.isNotEmpty()) {
                        item {
                            TextButton(onClick = {
                                selectedFilter    = null
                                fechaSeleccionada = null
                                searchQuery       = ""
                                historial         = HistorialManager.historial
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint               = Color.Gray,
                                    modifier           = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpiar", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier        = Modifier.fillMaxWidth().padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = if (historial.isEmpty()) "Sin eventos registrados aún"
                        else "No se encontraron registros con ese filtro",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        items(filteredList) { entrada -> HistoryCard(entrada) }
    }
}

// ── Botón de filtro con color personalizado ───────────────────────────────────
@Composable
fun FilterButton(
    text:     String,
    selected: Boolean,
    color:    Color    = AzulLogis,
    onClick:  () -> Unit
) {
    Button(
        onClick = onClick,
        colors  = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else Color.White
        ),
        border = BorderStroke(1.dp, if (selected) color else Color.LightGray),
        shape  = RoundedCornerShape(50.dp)
    ) {
        Text(text = text, color = if (selected) Color.White else Color.Black, fontSize = 13.sp)
    }
}

// ── Card de cada entrada del historial ───────────────────────────────────────
@Composable
fun HistoryCard(entrada: EntradaHistorial) {
    val cardColor = when (entrada.tipoEvento) {
        TipoEvento.ANOMALIA -> Color(0xFFFFE0E0)
        TipoEvento.DESPACHO -> Color(0xFFFFF8E1)
        else                -> Color(0xFFDCE6EF)
    }
    val tipoTexto = when (entrada.tipoEvento) {
        TipoEvento.INGRESO  -> "Entrada"
        TipoEvento.DESPACHO -> "Salida"
        TipoEvento.ANOMALIA -> "Anomalía"
    }
    val tipoColor = when (entrada.tipoEvento) {
        TipoEvento.INGRESO  -> Color(0xFF2E7D32)
        TipoEvento.DESPACHO -> Color(0xFFC62828)
        TipoEvento.ANOMALIA -> Color(0xFFE65100)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape     = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint               = Color.DarkGray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = entrada.producto,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.Black
                )
                if (entrada.idCaja.isNotBlank() && entrada.idCaja != "SIN_ID") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text     = "ID: ${entrada.idCaja}",
                        fontSize = 12.sp,
                        color    = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Fecha: ${entrada.fechaHora}", fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = tipoTexto, fontSize = 13.sp, color = tipoColor)
                if (entrada.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = entrada.descripcion, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(
                imageVector        = Icons.Default.MoreVert,
                contentDescription = null,
                tint               = Color.DarkGray
            )
        }
    }
}