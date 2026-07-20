package com.example.stylebyte.network.dto

import com.example.stylebyte.Order
import com.example.stylebyte.OrderItem
import com.example.stylebyte.OrderStatus

/** Una línea de DetallePedidos, tal como la arma la API (join con Productos). */
data class OrderItemDto(
    val IdProducto: Int,
    val Nombre: String,
    val Cantidad: Int,
    val PrecioUnitario: Double,
    val Subtotal: Double
)

/**
 * Refleja Pedidos + datos del cliente (join con Usuarios). "items" solo viene
 * lleno en GET /orders/:id y en la respuesta de POST /orders — en el listado
 * (GET /orders) puede venir null/vacío, ya que ahí solo se necesita el resumen.
 */
data class OrderDto(
    val IdPedido: Int,
    val NumeroPedido: String,
    val IdUsuario: Int,
    val FechaPedido: String?,
    val FechaEntrega: String?,
    val MetodoEntrega: String,
    val Estado: String,
    val Total: Double,
    val ClienteNombre: String?,
    val ClienteApellido: String?,
    val ClienteCorreo: String?,
    val items: List<OrderItemDto>? = null
)

/** Body de POST /orders */
data class CreateOrderRequestDto(
    val metodoEntrega: String
)

/** Body de PATCH /orders/:id/status */
data class UpdateOrderStatusRequestDto(
    val estado: String
)

fun OrderDto.toOrder(): Order {
    val deliveryFee = if (MetodoEntrega == "Envío a domicilio") 5.0 else 0.0

    // Si "items" viene null (listado resumido), armamos una sola línea genérica
    // con el Total para no dejar la lista vacía en pantallas que solo muestran resumen.
    val orderItems = items?.map { OrderItem(it.Nombre, it.Cantidad, it.PrecioUnitario) }
        ?: listOf(OrderItem("Pedido $NumeroPedido", 1, Total - deliveryFee))

    return Order(
        pedidoId = IdPedido,
        id = NumeroPedido,
        date = FechaPedido ?: "",
        status = OrderStatus.fromEstadoApi(Estado),
        customerName = "${ClienteNombre ?: ""} ${ClienteApellido ?: ""}".trim(),
        customerEmail = ClienteCorreo ?: "",
        items = orderItems,
        deliveryMethod = MetodoEntrega,
        deliveryFee = deliveryFee
    )
}
