package com.example.logist_tech.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.logist_tech.network.HistorialMovimiento
import com.example.logist_tech.ui.viewmodels.LogistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogistViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadHistorialGlobal()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Línea de Tiempo Global", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2F4F7)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (viewModel.historialGlobal.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay movimientos registrados", color = Color.Gray)
                    }
                }
            }
            items(viewModel.historialGlobal) { mov ->
                TimelineItem(mov)
            }
        }
    }
}

@Composable
fun TimelineItem(mov: HistorialMovimiento) {
    val stateColor = when(mov.estado_nuevo) {
        "REGISTRADO" -> Color(0xFF1976D2)
        "RECEPCION_EN_ALMACEN" -> Color(0xFFF57C00)
        "EN_ESTANTE" -> Color(0xFF388E3C)
        "SALIDA_DE_ESTANTE" -> Color(0xFF7B1FA2)
        "SALIENDO_DE_ALMACEN" -> Color(0xFFE91E63)
        "ENTREGADO" -> Color(0xFF455A64)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(12.dp).background(stateColor, CircleShape))
                Box(modifier = Modifier.width(2.dp).height(60.dp).background(stateColor.copy(alpha = 0.2f)))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mov.estado_nuevo.replace("_", " "),
                        color = stateColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(text = if(mov.fecha_cambio.length > 10) mov.fecha_cambio.takeLast(8) else "", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = mov.producto.ifBlank { "Caja: ${mov.id_caja}" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(text = "${mov.id_operador} • ${mov.tipo_operador}", fontSize = 13.sp, color = Color.DarkGray)
                }
                
                Text(text = mov.fecha_cambio.take(10), fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}
