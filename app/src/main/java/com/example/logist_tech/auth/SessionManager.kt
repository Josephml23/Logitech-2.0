package com.example.logist_tech.auth

object SessionManager {

    enum class Rol { 
        CLIENTE, 
        RECEPTOR_ENTRADA, 
        ORGANIZADOR_ENTRADA, 
        ORGANIZADOR_SALIDA, 
        RECEPTOR_SALIDA 
    }

    var usuarioId: String = ""
        private set

    var nombreUsuario: String = ""
        private set

    var rol: Rol = Rol.CLIENTE
        private set

    fun login(id: String, nombre: String, rolSeleccionado: Rol) {
        usuarioId = id.trim().lowercase() // Normalización a minúsculas
        nombreUsuario = nombre.trim()
        rol = rolSeleccionado
    }

    fun logout() {
        usuarioId = ""
        nombreUsuario = ""
        rol = Rol.CLIENTE
    }

    fun estaLogueado(): Boolean = usuarioId.isNotEmpty()
}
