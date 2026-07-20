package com.example.stylebyte

import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.UpdateUserStatusRequestDto
import com.example.stylebyte.network.extractErrorMessage

/** Un usuario real de la tabla Usuarios, para la sección "Gestión de Usuarios" del Admin. */
data class AdminUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    var active: Boolean,
    val ordersCount: Int
)

/**
 * Repositorio de usuarios para el Admin — ya NO es una lista simulada:
 * lee/escribe la tabla Usuarios real vía GET/PUT /users (solo Administrador).
 */
object AdminUserRepository {

    private var cache: List<AdminUser> = emptyList()

    suspend fun refreshFromApi(): Result<List<AdminUser>> {
        return try {
            val response = RetrofitClient.userApi.getUsers()
            if (response.isSuccessful && response.body() != null) {
                cache = response.body()!!.map {
                    AdminUser(
                        id = it.IdUsuario,
                        name = "${it.Nombre} ${it.Apellido}".trim(),
                        email = it.Correo,
                        role = it.Rol,
                        active = it.Activo,
                        ordersCount = it.CantidadPedidos
                    )
                }
                Result.success(cache)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudieron cargar los usuarios")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAll(): List<AdminUser> = cache

    suspend fun setActive(userId: Int, active: Boolean): Result<Unit> {
        return try {
            val response = RetrofitClient.userApi.updateStatus(userId, UpdateUserStatusRequestDto(active))
            if (response.isSuccessful) {
                cache = cache.map { if (it.id == userId) it.copy(active = active) else it }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo actualizar el usuario")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
