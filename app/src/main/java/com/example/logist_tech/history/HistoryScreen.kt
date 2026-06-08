package com.example.logist_tech.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.logist_tech.history.HistorialManager
import com.example.logist_tech.history.EntradaHistorial
import com.example.logist_tech.history.TipoEvento
import androidx.compose.runtime.derivedStateOf

private val AzulLogis = Color(0xFF2980B9)
private val AzulOscuro = Color(0xFF123B6D)
private val FondoBlanco = Color(0xFFFFFFFF)

@Composable
fun HistoryScreen(onNavigateBack: () -> Unit = {}) {

    var selectedFilter by remember { mutableStateOf<String?>("Entrada") }
    var searchQuery by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf<String?>("21/05/2026") }
    var mostrarCalendario by remember { mutableStateOf(false) }

    val historial by remember { derivedStateOf { HistorialManager.historial } }

    val filteredList = historial.filter { entrada ->
        val tipoTexto = when (entrada.tipoEvento) {
            TipoEvento.INGRESO  -> "Entrada"
            TipoEvento.DESPACHO -> "Salida"
            TipoEvento.ANOMALIA -> "Anomalia"
        }
        (selectedFilter == null || tipoTexto == selectedFilter) &&
                (fechaSeleccionada == null || entrada.fechaHora.startsWith(
                    fechaSeleccionada!!.split("/").reversed().joinToString("-")
                )) &&
                entrada.producto.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoBlanco)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AzulLogis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_logo_logis),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Historial",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar producto...", color = Color.Gray) },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.Search,
                            contentDescription = "Search", tint = AzulLogis)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE7EAF0),
                        unfocusedContainerColor = Color(0xFFE7EAF0),
                        focusedBorderColor = AzulLogis,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fechaSeleccionada ?: "Todas las fechas",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha", color = AzulOscuro) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarCalendario = true }) {
                            Icon(imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar", tint = AzulLogis)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = AzulLogis,
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterButton("Entrada", selectedFilter == "Entrada") {
                        selectedFilter = if (selectedFilter == "Entrada") null else "Entrada"
                    }
                    FilterButton("Salida", selectedFilter == "Salida") {
                        selectedFilter = if (selectedFilter == "Salida") null else "Salida"
                    }
                    if (selectedFilter != null || fechaSeleccionada != null || searchQuery.isNotEmpty()) {
                        TextButton(onClick = {
                            selectedFilter = null
                            fechaSeleccionada = null
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null,
                                tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpiar", color = Color.Gray, fontSize = 13.sp)
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
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron registros", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }

        items(filteredList) { entrada -> HistoryCard(entrada) }
    }
}

@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AzulLogis else Color.White
        ),
        border = BorderStroke(1.dp, if (selected) AzulLogis else Color.LightGray),
        shape = RoundedCornerShape(50.dp)
    ) {
        Text(text = text, color = if (selected) Color.White else Color.Black)
    }
}

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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Inventory2,
                contentDescription = null, tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entrada.producto, fontSize = 16.sp,
                    fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Fecha: ${entrada.fechaHora}", fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = tipoTexto, fontSize = 13.sp, color = tipoColor)
                if (entrada.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = entrada.descripcion, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(imageVector = Icons.Default.MoreVert,
                contentDescription = null, tint = Color.DarkGray)
        }
    }
}