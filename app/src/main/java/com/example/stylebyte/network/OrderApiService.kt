package com.example.stylebyte.network

import com.example.stylebyte.network.dto.CreateOrderRequestDto
import com.example.stylebyte.network.dto.OrderDto
import com.example.stylebyte.network.dto.UpdateOrderStatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Todas requieren sesión iniciada. */
interface OrderApiService {

    /** Clientes: solo sus pedidos. Administradores: todos. */
    @GET("orders")
    suspend fun getOrders(): Response<List<OrderDto>>

    @GET("orders/{idPedido}")
    suspend fun getOrderDetail(@Path("idPedido") idPedido: Int): Response<OrderDto>

    /** Crea el pedido a partir del carrito actual del usuario logueado (transacción en el servidor). */
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequestDto): Response<OrderDto>

    /** Solo Administrador. */
    @PATCH("orders/{idPedido}/status")
    suspend fun updateStatus(@Path("idPedido") idPedido: Int, @Body body: UpdateOrderStatusRequestDto): Response<OrderDto>
}
