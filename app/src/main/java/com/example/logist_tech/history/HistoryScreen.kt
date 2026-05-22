package com.example.logist_tech.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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
import com.example.logist_tech.models.Producto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


@Composable
fun HistoryScreen() {

    var selectedFilter by remember { mutableStateOf("Entrada") }
    var searchQuery by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf("21/05/2026") }
    var mostrarCalendario by remember { mutableStateOf(false) }


    // DATOS TEMPORALES PARA DEMO
    val historial = remember {
        listOf(
            Producto(
                id = "1",
                nombre = "Laptop Lenovo",
                cantidad = 5,
                pesoKg = 2.5,
                categoria = "Tecnología",
                destino = "Lima",
                estado = "Correcto",
                tipoMovimiento = "Entrada",
                fecha = "21/05/2026"
            ),
            Producto(
                id = "2",
                nombre = "Mouse Logitech",
                cantidad = 3,
                pesoKg = 0.5,
                categoria = "Accesorios",
                destino = "Arequipa",
                estado = "Correcto",
                tipoMovimiento = "Salida",
                fecha = "20/05/2026"
            ),
            Producto(
                id = "3",
                nombre = "Monitor Samsung",
                cantidad = 2,
                pesoKg = 4.5,
                categoria = "Pantallas",
                destino = "Cusco",
                estado = "Anomalía",
                tipoMovimiento = "Entrada",
                fecha = "19/05/2026"
            )
        )
    }

    // FILTRO
    val filteredList = historial.filter { producto ->
        producto.tipoMovimiento == selectedFilter &&
                producto.nombre.contains(searchQuery, ignoreCase = true)
                producto.fecha == fechaSeleccionada
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {

        item {

            Column {

                Spacer(modifier = Modifier.height(10.dp))

                // TOP BAR
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { }) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF7A00)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LOGO
                Image(
                    painter = painterResource(id = R.drawable.logo_logistech),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(20.dp))

                // TITULO
                Text(
                    text = "Historial",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF123B6D)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    placeholder = {
                        Text(
                            text = "Buscar producto...",
                            color = Color.Gray
                        )
                    },
                    trailingIcon = {

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE7EAF0),
                        unfocusedContainerColor = Color(0xFFE7EAF0),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // DATE FIELD
                OutlinedTextField(
                    value = fechaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Date")
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarCalendario = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // DIÁLOGO DEL CALENDARIO (Añadido justo debajo del espacio)
                if (mostrarCalendario) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { mostrarCalendario = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { milisegundos ->
                                    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                        timeZone = TimeZone.getTimeZone("UTC")
                                    }
                                    fechaSeleccionada = formato.format(java.util.Date(milisegundos))
                                }
                                mostrarCalendario = false
                            }) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarCalendario = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // BOTONES FILTRO
                Row {

                    FilterButton(
                        text = "Entrada",
                        selected = selectedFilter == "Entrada"
                    ) {
                        selectedFilter = "Entrada"
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    FilterButton(
                        text = "Salida",
                        selected = selectedFilter == "Salida"
                    ) {
                        selectedFilter = "Salida"
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // ESTADO VACÍO
        if (filteredList.isEmpty()) {

            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "No se encontraron registros",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // LISTA
        items(filteredList) { producto ->

            HistoryCard(producto)
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (selected)
                    Color(0xFF1565C0)
                else
                    Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (selected)
                Color(0xFF1565C0)
            else
                Color.LightGray
        ),
        shape = RoundedCornerShape(50.dp)
    ) {

        Text(
            text = text,
            color =
                if (selected)
                    Color.White
                else
                    Color.Black
        )
    }
}

@Composable
fun HistoryCard(producto: Producto) {

    val cardColor =
        when (producto.estado) {

            "Anomalía" -> Color(0xFFFFE0E0)

            else -> Color(0xFFDCE6EF)
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = Color.DarkGray
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = producto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Fecha: ${producto.fecha}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = producto.tipoMovimiento,
                    fontSize = 13.sp,
                    color =
                        if (producto.tipoMovimiento == "Entrada")
                            Color(0xFF2E7D32)
                        else
                            Color(0xFFC62828)
                )
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = Color.DarkGray
            )
        }
    }
}