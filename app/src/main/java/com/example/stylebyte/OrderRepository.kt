package com.example.stylebyte

import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.CreateOrderRequestDto
import com.example.stylebyte.network.dto.UpdateOrderStatusRequestDto
import com.example.stylebyte.network.dto.toOrder
import com.example.stylebyte.network.extractErrorMessage

/**
 * Repositorio de pedidos — YA NO usa datos de ejemplo en memoria. Todo viene
 * de la tabla Pedidos (y DetallePedidos) vía StyleStoreAPI.
 *
 * El backend ya filtra: un Cliente logueado solo ve SUS pedidos; un
 * Administrador ve todos. Por eso getAll() ya no necesita filtrar por nombre
 * del lado de Android.
 */
object OrderRepository {

    private var cache: List<Order> = emptyList()

    /** Trae el listado (resumen) desde la API. */
    suspend fun refreshFromApi(): Result<List<Order>> {
        return try {
            val response = RetrofitClient.orderApi.getOrders()
            if (response.isSuccessful && response.body() != null) {
                cache = response.body()!!.map { it.toOrder() }
                Result.success(cache)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudieron cargar los pedidos")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAll(): List<Order> {
        if (cache.isEmpty()) refreshFromApi()
        return cache
    }

    /** Trae el detalle COMPLETO (con todos los items) de un pedido, para la Factura. */
    suspend fun fetchDetail(pedidoId: Int): Result<Order> {
        return try {
            val response = RetrofitClient.orderApi.getOrderDetail(pedidoId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toOrder())
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo cargar el pedido")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea el pedido a partir del carrito actual del usuario logueado.
     * El servidor hace TODO en una transacción: crea el Pedido, sus
     * DetallePedidos, descuenta Stock y vacía el carrito.
     */
    suspend fun createOrder(deliveryMethod: String): Result<Order> {
        return try {
            val response = RetrofitClient.orderApi.createOrder(CreateOrderRequestDto(deliveryMethod))
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!.toOrder()
                cache = listOf(order) + cache
                Result.success(order)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo confirmar el pedido")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Solo Administrador. */
    suspend fun updateStatus(pedidoId: Int, newStatus: OrderStatus): Result<Order> {
        return try {
            val response = RetrofitClient.orderApi.updateStatus(pedidoId, UpdateOrderStatusRequestDto(newStatus.toEstadoApi()))
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!.toOrder()
                cache = cache.map { if (it.pedidoId == pedidoId) order else it }
                Result.success(order)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo actualizar el estado")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
