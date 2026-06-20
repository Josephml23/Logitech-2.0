package com.example.logist_tech.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.logist_tech.network.RetrofitClient
import com.example.logist_tech.ui.viewmodels.LogistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroCajaScreen(
    codigoQr: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: LogistViewModel = viewModel()
) {
    var producto by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("1") }
    var peso by remember { mutableStateOf("0.5") }
    var prioridad by remember { mutableStateOf("NORMAL") }
    var categoria by remember { mutableStateOf("GENERAL") }
    var esFragil by remember { mutableStateOf(false) }
    var idProveedor by remember { mutableStateOf("PROV-001") }
    var selectedTipoId by remember { mutableStateOf("") }
    var expandedTipos by remember { mutableStateOf(false) }

    // Validación de existencia
    var cajaYaExiste by remember { mutableStateOf<Boolean?>(null) }

    // Estado para nuevo tipo de caja
    var showAddTipoDialog by remember { mutableStateOf(false) }
    var nuevoNombreTipo by remember { mutableStateOf("") }
    var nuevoLargo by remember { mutableStateOf("") }
    var nuevoAncho by remember { mutableStateOf("") }
    var nuevoAlto by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getTodasLasCajas()
            if (response.isSuccessful) {
                cajaYaExiste = response.body()?.any { it.codigo_qr == codigoQr } ?: false
            } else { cajaYaExiste = false }
        } catch (e: Exception) { cajaYaExiste = false }

        viewModel.loadInitialData()
    }

    if (showAddTipoDialog) {
        AlertDialog(
            onDismissRequest = { showAddTipoDialog = false },
            title = { Text("Nuevo Tipo de Caja") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nuevoNombreTipo, onValueChange = { nuevoNombreTipo = it }, label = { Text("Nombre") })
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(value = nuevoLargo, onValueChange = { nuevoLargo = it }, label = { Text("Largo") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = nuevoAncho, onValueChange = { nuevoAncho = it }, label = { Text("Ancho") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = nuevoAlto, onValueChange = { nuevoAlto = it }, label = { Text("Alto") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.registrarTipoCaja(nuevoNombreTipo, nuevoLargo.toDoubleOrNull() ?: 0.0, nuevoAncho.toDoubleOrNull() ?: 0.0, nuevoAlto.toDoubleOrNull() ?: 0.0) {
                        showAddTipoDialog = false
                    }
                }) { Text("GUARDAR") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registro de Paquete") }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (cajaYaExiste) {
                true -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFF57F17), modifier = Modifier.size(100.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("REGISTRO DUPLICADO", color = Color(0xFFF57F17), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Esta caja con ID \"$codigoQr\" ya se encuentra registrada.", textAlign = TextAlign.Center, color = Color.DarkGray)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)), modifier = Modifier.fillMaxWidth()) {
                            Text("VOLVER AL MENÚ")
                        }
                    }
                }
                false -> {
                    Column(
                        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Código QR: $codigoQr", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(value = producto, onValueChange = { producto = it }, label = { Text("Producto") }, modifier = Modifier.fillMaxWidth())

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f))
                        }

                        ExposedDropdownMenuBox(expanded = expandedTipos, onExpandedChange = { expandedTipos = !expandedTipos }) {
                            OutlinedTextField(
                                value = viewModel.tiposCaja.find { it.id.toString() == selectedTipoId }?.nombre ?: "Seleccionar Tipo",
                                onValueChange = {}, readOnly = true, label = { Text("Tipo de Caja") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipos) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedTipos, onDismissRequest = { expandedTipos = false }) {
                                viewModel.tiposCaja.forEach { tipo ->
                                    DropdownMenuItem(text = { Text("${tipo.nombre} (${tipo.largo}x${tipo.ancho}x${tipo.alto})") }, onClick = { selectedTipoId = tipo.id.toString(); expandedTipos = false })
                                }
                                HorizontalDivider()
                                DropdownMenuItem(text = { Text("+ Agregar nuevo tipo...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }, onClick = { expandedTipos = false; showAddTipoDialog = true })
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = esFragil, onCheckedChange = { esFragil = it })
                            Text("Es frágil")
                        }

                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            Button(
                                onClick = { viewModel.registrarCaja(codigoQr, producto, cantidad.toIntOrNull() ?: 1, peso.toDoubleOrNull() ?: 0.0, prioridad, categoria, esFragil, idProveedor, selectedTipoId, onSuccess) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = producto.isNotBlank() && selectedTipoId.isNotBlank()
                            ) { Text("REGISTRAR CAJA") }
                        }
                    }
                }
                null -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                        Text("Validando código...")
                    }
                }
            }
        }
    }
}
