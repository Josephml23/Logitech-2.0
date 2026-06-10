package com.example.logist_tech.anomalias

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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

private val AzulLogis   = Color(0xFF2980B9)
private val AzulOscuro  = Color(0xFF123B6D)
private val FondoBlanco = Color(0xFFFFFFFF)

@Composable
fun AnomaliasScreen(onNavigateBack: () -> Unit = {}) {
    var filtroActivo by remember { mutableStateOf<String?>(null) }
    var anomalias    by remember { mutableStateOf(AnomaliaManager.filtrarPorPrioridad(null)) }

    LaunchedEffect(filtroActivo) {
        anomalias = AnomaliaManager.filtrarPorPrioridad(filtroActivo)
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AzulLogis,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Anomalías (${AnomaliaManager.totalAnomalias()})",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chips de filtro
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALTA", "MEDIA", "BAJA").forEach { prioridad ->
                        val color = colorPorPrioridad(prioridad)
                        FilterChip(
                            selected = filtroActivo == prioridad,
                            onClick = {
                                filtroActivo = if (filtroActivo == prioridad) null else prioridad
                                anomalias = AnomaliaManager.filtrarPorPrioridad(filtroActivo)
                            },
                            label = { Text(prioridad, fontSize = 12.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, shape = RoundedCornerShape(50))
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen — cards de conteo sin borde gris
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALTA", "MEDIA", "BAJA").forEach { prioridad ->
                        val cantidad = AnomaliaManager.filtrarPorPrioridad(prioridad).size
                        val color    = colorPorPrioridad(prioridad)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, color.copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.08f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$cantidad",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = color
                                )
                                Text(text = prioridad, fontSize = 10.sp, color = color.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (anomalias.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filtroActivo == null) "Sin anomalías registradas"
                        else "Sin anomalías de prioridad $filtroActivo",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(anomalias) { anomalia -> AnomaliaCard(anomalia) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AnomaliaCard(anomalia: Anomalia) {
    val color     = colorPorPrioridad(anomalia.prioridad)
    val fondoCard = fondoPorPrioridad(anomalia.prioridad)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        // SIN borde gris — solo borde del color de prioridad
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = fondoCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = anomalia.tipo.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = color
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = color
                ) {
                    Text(
                        text = anomalia.prioridad,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = color.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Producto: ${anomalia.productoNombre}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A2E)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = anomalia.descripcion,
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = anomalia.fecha,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (anomalia.evidenciaUri.isNotBlank()) {
                    Text(
                        text = "Con evidencia",
                        fontSize = 11.sp,
                        color = AzulLogis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun colorPorPrioridad(prioridad: String): Color = when (prioridad) {
    "ALTA"  -> Color(0xFFE53935)
    "MEDIA" -> Color(0xFFF57F17)
    else    -> Color(0xFF2E7D32)
}

fun fondoPorPrioridad(prioridad: String): Color = when (prioridad) {
    "ALTA"  -> Color(0xFFFFF0F0)
    "MEDIA" -> Color(0xFFFFF8EE)
    else    -> Color(0xFFF0FFF4)
}