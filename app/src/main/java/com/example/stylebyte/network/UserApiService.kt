package com.example.stylebyte.network

import com.example.stylebyte.network.dto.AdminUserDto
import com.example.stylebyte.network.dto.UpdateProfileRequestDto
import com.example.stylebyte.network.dto.UpdateUserStatusRequestDto
import com.example.stylebyte.network.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {

    /** Solo Administrador. */
    @GET("users")
    suspend fun getUsers(): Response<List<AdminUserDto>>

    /** Solo Administrador. */
    @PUT("users/{id}/estado")
    suspend fun updateStatus(@Path("id") id: Int, @Body body: UpdateUserStatusRequestDto): Response<Unit>

    /** El propio usuario logueado edita su perfil. */
    @PUT("users/me")
    suspend fun updateMyProfile(@Body body: UpdateProfileRequestDto): Response<UsuarioDto>
}
