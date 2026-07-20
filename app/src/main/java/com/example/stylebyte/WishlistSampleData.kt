package com.example.stylebyte

/**
 * Wishlist simulada (sin base de datos): cada usuario tiene una cantidad de
 * favoritos "de ejemplo". Se usa únicamente para poblar el contador de
 * Wishlist en el Perfil, tal como pide el enunciado ("aunque sean simulados").
 */
object WishlistSampleData {

    private val simulatedCounts = mapOf(
        "Sarah Chen" to 5,
        "Marco Díaz" to 2,
        "Valentina Ríos" to 3,
        "Luis Fernández" to 0,
        "Camila Torres" to 4
    )

    fun countFor(name: String): Int =
        simulatedCounts[name] ?: ((name.trim().length % 4) + 1)
}
