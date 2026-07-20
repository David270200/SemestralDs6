package com.example.stylebyte.network.dto

/** Refleja Usuarios + la cantidad de pedidos calculada por la API (para el Admin). */
data class AdminUserDto(
    val IdUsuario: Int,
    val Nombre: String,
    val Apellido: String,
    val Correo: String,
    val Telefono: String?,
    val Direccion: String?,
    val Rol: String,
    val Activo: Boolean,
    val FechaRegistro: String?,
    val CantidadPedidos: Int
)

/** Body de PUT /users/:id/estado */
data class UpdateUserStatusRequestDto(
    val activo: Boolean
)

/** Body de PUT /users/me */
data class UpdateProfileRequestDto(
    val nombre: String,
    val apellido: String,
    val telefono: String? = null,
    val direccion: String? = null
)
