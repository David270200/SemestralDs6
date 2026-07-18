package com.example.stylebyte

/**
 * Representa un pedido dentro del historial.
 * Por ahora solo se usa con datos hardcodeados (OrderSampleData).
 */
data class Order(
    val id: String,
    val date: String,
    val status: OrderStatus,
    val productName: String,
    val quantity: Int,
    val deliveryMethod: String,
    val totalPrice: String
)