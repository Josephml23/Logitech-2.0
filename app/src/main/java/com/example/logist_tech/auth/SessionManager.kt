package com.example.logist_tech.auth

object SessionManager {

    enum class Rol { DESPACHO, BANDA }

    var usuarioId: String = ""
        private set

    var nombreUsuario: String = ""
        private set

    var rol: Rol = Rol.DESPACHO
        private set

    fun login(id: String, nombre: String, rolSeleccionado: Rol) {
        usuarioId = id
        nombreUsuario = nombre
        rol = rolSeleccionado
    }

    fun logout() {
        usuarioId = ""
        nombreUsuario = ""
        rol = Rol.DESPACHO
    }

    fun estaLogueado(): Boolean = usuarioId.isNotEmpty()
}
