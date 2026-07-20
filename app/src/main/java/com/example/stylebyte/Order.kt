package com.example.stylebyte

import java.util.Locale

/** Una línea de producto dentro de un pedido (para la factura y el detalle). */
data class OrderItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val subtotal: Double get() = unitPrice * quantity
}

/**
 * Representa un pedido. Desde esta entrega, TODOS los pedidos vienen de la
 * tabla Pedidos en SQL Server (vía StyleStoreAPI) — ya no hay datos de ejemplo
 * en memoria.
 *
 * "pedidoId" es el IdPedido real (se usa para pedir el detalle o cambiar el
 * estado en la API). "id" es el NumeroPedido (ej: "ST-20260001"), el que se
 * le muestra al usuario.
 */
data class Order(
    val pedidoId: Int,
    val id: String,
    val date: String,
    var status: OrderStatus,
    val customerName: String,
    val customerEmail: String,
    val items: List<OrderItem>,
    val deliveryMethod: String,
    val deliveryFee: Double = 0.0
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val total: Double get() = subtotal + deliveryFee

    // Campos "de compatibilidad" para pantallas que muestran un resumen de una línea.
    val productName: String
        get() = if (items.isEmpty()) "" else if (items.size == 1) items[0].productName
                else "${items[0].productName} +${items.size - 1} más"

    val quantity: Int get() = items.sumOf { it.quantity }

    val totalPrice: String get() = formatPrice(total)

    companion object {
        fun formatPrice(value: Double): String = "$" + String.format(Locale.US, "%.2f", value)
    }
}
