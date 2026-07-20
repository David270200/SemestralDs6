package com.example.stylebyte.network

import com.example.stylebyte.network.dto.AddCartItemRequestDto
import com.example.stylebyte.network.dto.CartResponseDto
import com.example.stylebyte.network.dto.UpdateCartItemRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Todas requieren sesión iniciada (el token se agrega solo, ver RetrofitClient). */
interface CartApiService {

    @GET("cart/{idUsuario}")
    suspend fun getCart(@Path("idUsuario") idUsuario: Int): Response<CartResponseDto>

    @POST("cart/{idUsuario}/items")
    suspend fun addItem(@Path("idUsuario") idUsuario: Int, @Body body: AddCartItemRequestDto): Response<CartResponseDto>

    @PUT("cart/{idUsuario}/items/{idProducto}")
    suspend fun updateItem(
        @Path("idUsuario") idUsuario: Int,
        @Path("idProducto") idProducto: Int,
        @Body body: UpdateCartItemRequestDto
    ): Response<CartResponseDto>

    @DELETE("cart/{idUsuario}/items/{idProducto}")
    suspend fun removeItem(@Path("idUsuario") idUsuario: Int, @Path("idProducto") idProducto: Int): Response<CartResponseDto>

    @DELETE("cart/{idUsuario}")
    suspend fun clearCart(@Path("idUsuario") idUsuario: Int): Response<CartResponseDto>
}
