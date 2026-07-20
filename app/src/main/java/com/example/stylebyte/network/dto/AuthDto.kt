package com.example.stylebyte.network.dto

/** Body de POST /auth/login */
data class LoginRequestDto(
    val correo: String,
    val password: String
)

/** Body de POST /auth/register (el Rol no se manda: la API siempre asigna "Cliente") */
data class RegisterRequestDto(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null
)

/** Refleja la tabla Usuarios (sin PasswordHash, la API nunca lo devuelve). */
data class UsuarioDto(
    val IdUsuario: Int,
    val Nombre: String,
    val Apellido: String,
    val Correo: String,
    val Telefono: String?,
    val Direccion: String?,
    val Rol: String,
    val Activo: Boolean,
    val FechaRegistro: String?
)

/** Respuesta de /auth/login y /auth/register */
data class AuthResponseDto(
    val token: String,
    val usuario: UsuarioDto
)

/** Respuesta de error genérica de la API (para leer el mensaje real en Android). */
data class ApiErrorDto(
    val error: String?,
    val detalle: String?
)
