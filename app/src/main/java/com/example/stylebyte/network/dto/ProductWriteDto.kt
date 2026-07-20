package com.example.stylebyte.network.dto

/** Body de POST /products y PUT /products/:id (misma forma para ambos). */
data class ProductWriteRequestDto(
    val idCategoria: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val rutaImagen: String? = null
)

/** Body de POST /categories y PUT /categories/:id. */
data class CategoryWriteRequestDto(
    val nombre: String,
    val descripcion: String? = null
)
