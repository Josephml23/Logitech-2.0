package com.example.logist_tech.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.*
import com.example.logist_tech.R
import com.example.logist_tech.auth.SessionManager
import com.example.logist_tech.auth.SessionManager.Rol

@Composable
fun HomeScreen(
    onNavigateScanner: () -> Unit = {},
    onNavigateInventory: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateMisCajas: () -> Unit = {},
    onNavigateDashboard: () -> Unit = {},
    onNavigatePerfil: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val rol = SessionManager.rol

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera con saludo y logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Bienvenido,", color = Color.Gray, fontSize = 14.sp)
                Text(SessionManager.nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2980B9))
            }
            IconButton(onClick = { onLogout() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesion", tint = Color(0xFF2980B9))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid de Opciones
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (rol == Rol.CLIENTE) {
                // MENÚ CLIENTE
                item { MenuCard("Registrar Caja", Icons.Filled.AddBox, onNavigateScanner) }
                item { MenuCard("Mis Cajas", Icons.Filled.Inventory2, onNavigateMisCajas) }
                item { MenuCard("Alertas", Icons.Filled.Notifications, onNavigateNotifications) }
                item { MenuCard("Editar Perfil", Icons.Filled.Person, onNavigatePerfil) }
            } else {
                // MENÚ OPERARIO
                item { MenuCard("Escanear / Cambiar", Icons.Filled.QrCodeScanner, onNavigateScanner) }
                item { MenuCard("Dashboard Vivo", Icons.Filled.Dashboard, onNavigateDashboard) }
                item { MenuCard("Mi Historial", Icons.Filled.History, onNavigateHistory) }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2980B9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (badgeCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                containerColor = Color(0xFFE53935)
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}