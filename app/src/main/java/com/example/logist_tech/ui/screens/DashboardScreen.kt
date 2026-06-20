package com.example.logist_tech.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.logist_tech.network.Caja
import com.example.logist_tech.ui.viewmodels.LogistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogistViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadTodasLasCajas()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC), // Gris azulado ultra claro (Minimalista)
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Operativo", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            
            // Sección de Estadísticas Rápidas (Estilo limpio)
            Row(
                modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip("Total", viewModel.todasLasCajas.size.toString(), Color(0xFF2980B9), Modifier.weight(1f))
                StatChip("Estante", viewModel.todasLasCajas.count { it.estado == "EN_ESTANTE" }.toString(), Color(0xFF10B981), Modifier.weight(1f))
            }

            Text(
                text = "FLUJO DE CAJAS EN TIEMPO REAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(viewModel.todasLasCajas) { caja ->
                    MinimalistCajaItem(caja)
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun MinimalistCajaItem(caja: Caja) {
    val stateColor = getEstadoColor(caja.estado)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de estado minimalista (lateral)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(stateColor)
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = caja.producto.ifBlank { "Producto sin nombre" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ID: ${caja.codigo_qr}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }

                // Estado al final con punto indicador
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(stateColor, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = caja.estado.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = stateColor
                        )
                    }
                }
            }
        }
    }
}

fun getEstadoColor(estado: String): Color {
    return when(estado) {
        "REGISTRADO" -> Color(0xFF3B82F6) // Azul
        "RECEPCION_EN_ALMACEN" -> Color(0xFFF59E0B) // Ambar
        "EN_ESTANTE" -> Color(0xFF10B981) // Esmeralda
        "SALIDA_DE_ESTANTE" -> Color(0xFF8B5CF6) // Violeta
        "SALIENDO_DE_ALMACEN" -> Color(0xFFEC4899) // Rosa
        "ENTREGADO" -> Color(0xFF64748B) // Pizarra
        else -> Color.Gray
    }
}
