package com.example.logist_tech.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logist_tech.R
import com.example.logist_tech.auth.SessionManager.Rol

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol.CLIENTE) }
    var errorVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_logis),
            contentDescription = "LogisTech Logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Logist Tech System",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2980B9)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorVisible = false
            },
            label = { Text("Nombre / ID Usuario", color = Color(0xFF2980B9)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = Color(0xFF111111),
                unfocusedTextColor      = Color(0xFF111111),
                focusedBorderColor      = Color(0xFF2980B9)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Selecciona tu Rol de Operación", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        // Grid de Roles
        val roles = Rol.values()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            roles.toList().chunked(2).forEach { rowRoles ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowRoles.forEach { rol ->
                        RolButton(
                            label = rol.name.replace("_", " ").lowercase().capitalize(),
                            selected = rolSeleccionado == rol,
                            modifier = Modifier.weight(1f),
                            onClick = { rolSeleccionado = rol }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (nombre.isBlank()) {
                    errorVisible = true
                } else {
                    SessionManager.login(
                        id = nombre.trim(),
                        nombre = nombre.trim(),
                        rolSeleccionado = rolSeleccionado
                    )
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9))
        ) {
            Text("INGRESAR AL SISTEMA", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RolButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(65.dp), // Altura Senior
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF2980B9) else Color(0xFFE0E0E0),
            contentColor = if (selected) Color.White else Color(0xFF444444)
        )
    ) {
        Text(
            text = label, 
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
