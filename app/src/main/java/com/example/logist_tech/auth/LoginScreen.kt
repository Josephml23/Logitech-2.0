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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logist_tech.R
import com.example.logist_tech.auth.SessionManager.Rol

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol.DESPACHO) }
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
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Identificacion",
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
            label = { Text("Nombre de usuario", color = Color(0xFF2980B9)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = Color(0xFF111111),
                unfocusedTextColor      = Color(0xFF111111),
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor      = Color(0xFF2980B9),
                unfocusedBorderColor    = Color(0xFFBBBBBB),
                cursorColor             = Color(0xFF2980B9)
            )
        )

        if (errorVisible) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ingresa tu nombre para continuar",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Selecciona tu rol",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RolButton(
                label = "Despacho",
                selected = rolSeleccionado == Rol.DESPACHO,
                modifier = Modifier.weight(1f),
                onClick = { rolSeleccionado = Rol.DESPACHO }
            )
            RolButton(
                label = "Banda",
                selected = rolSeleccionado == Rol.BANDA,
                modifier = Modifier.weight(1f),
                onClick = { rolSeleccionado = Rol.BANDA }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                if (nombre.isBlank()) {
                    errorVisible = true
                } else {
                    SessionManager.login(
                        id = nombre.trim().lowercase().replace(" ", "_"),
                        nombre = nombre.trim(),
                        rolSeleccionado = rolSeleccionado
                    )
                    onLoginSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9))
        ) {
            Text(
                text = "Ingresar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF2980B9) else Color(0xFFE0E0E0),
            contentColor = if (selected) Color.White else Color(0xFF444444)
        )
    ) {
        Text(text = label, fontWeight = FontWeight.Medium)
    }
}