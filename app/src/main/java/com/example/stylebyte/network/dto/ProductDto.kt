package com.example.stylebyte.network.dto

import com.example.stylebyte.Product

/**
 * Representa exactamente el JSON que devuelve GET /products (mismos nombres
 * de columna que la tabla "Productos" en SQL Server, sin traducir).
 *
 * Los nombres de los campos deben coincidir EXACTO (mayúsculas incluidas) con
 * las claves del JSON, porque Gson los mapea por nombre automáticamente.
 */
data class ProductDto(
    val IdProducto: Int,
    val IdCategoria: Int,
    val Nombre: String,
    val Descripcion: String?,
    val Precio: Double,
    val Stock: Int,
    val RutaImagen: String?
)

/**
 * Convierte el DTO (con nombres/tipos "tal cual la base de datos") al modelo
 * Product que ya usa el resto de la app (con nombres en inglés e IDs en String).
 * Este es el ÚNICO lugar donde ocurre la traducción español -> inglés.
 */
fun ProductDto.toProduct(): Product = Product(
    id = IdProducto.toString(),
    name = Nombre,
    description = Descripcion ?: "",
    price = Precio,
    categoryId = IdCategoria.toString(),
    stock = Stock,
    imageUrl = RutaImagen
)
