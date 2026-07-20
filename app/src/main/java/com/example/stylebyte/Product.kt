package com.example.stylebyte

/**
 * Representa un producto del catálogo de StyleStore.
 *
 * "imageRes" es el recurso local (placeholder) que se usa mientras no tengamos
 * imágenes reales servidas por la API. "imageUrl" es el campo que viene de la
 * base de datos (columna RutaImagen); hoy no se usa todavía para mostrar la
 * imagen real (eso se conecta en una fase futura con Glide/Coil), pero ya
 * viaja en el modelo para no tener que tocar esta clase de nuevo después.
 */
data class Product(
    val id: String,
    var name: String,
    var description: String,
    var price: Double,
    var categoryId: String,
    var stock: Int,
    var imageRes: Int = R.drawable.bg_product_placeholder,
    var imageUrl: String? = null
)
