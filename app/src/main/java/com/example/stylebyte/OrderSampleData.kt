package com.example.stylebyte

import com.example.stylebyte.Order
import com.example.stylebyte.OrderStatus

/**
 * Datos hardcodeados únicamente para visualizar la pantalla de historial de pedidos.
 * Se reemplazará por una fuente real (API/backend) más adelante.
 */
object OrderSampleData {

    val sampleOrders = listOf(
        Order(
            id = "ORD-2024-001",
            date = "28 de jun., 2024",
            status = OrderStatus.DELIVERED,
            productName = "Oversized Linen Blazer - Cream",
            quantity = 1,
            deliveryMethod = "Pickup",
            totalPrice = "$89.99"
        ),
        Order(
            id = "ORD-2024-002",
            date = "20 de jun., 2024",
            status = OrderStatus.PENDING,
            productName = "Zapatillas Running Pro",
            quantity = 2,
            deliveryMethod = "Envío a domicilio",
            totalPrice = "$134.50"
        ),
        Order(
            id = "ORD-2024-003",
            date = "10 de jun., 2024",
            status = OrderStatus.SHIPPED,
            productName = "Mochila Urbana Impermeable",
            quantity = 1,
            deliveryMethod = "Envío a domicilio",
            totalPrice = "$54.00"
        ),
        Order(
            id = "ORD-2024-004",
            date = "02 de jun., 2024",
            status = OrderStatus.CANCELLED,
            productName = "Reloj Analógico Clásico",
            quantity = 1,
            deliveryMethod = "Pickup",
            totalPrice = "$120.00"
        )
    )
}