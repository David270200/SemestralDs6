package com.example.stylebyte.network.dto

/** Un item del carrito, tal como lo arma routes/cart.js (join Productos + DetalleCarrito). */
data class CartItemDto(
    val IdProducto: Int,
    val Nombre: String,
    val Precio: Double,
    val Stock: Int,
    val RutaImagen: String?,
    val Cantidad: Int
)

/** Respuesta de todos los endpoints de /cart: la API ya calcula subtotal y la regla de negocio. */
data class CartResponseDto(
    val items: List<CartItemDto>,
    val subtotal: Double,
    val homeDeliveryAvailable: Boolean
)

/** Body de POST /cart/:idUsuario/items */
data class AddCartItemRequestDto(
    val idProducto: Int,
    val cantidad: Int
)

/** Body de PUT /cart/:idUsuario/items/:idProducto */
data class UpdateCartItemRequestDto(
    val cantidad: Int
)
