package com.example.stylebyte.network.dto

import com.example.stylebyte.Category

/** Representa exactamente el JSON que devuelve GET /categories. */
data class CategoryDto(
    val IdCategoria: Int,
    val Nombre: String,
    val Descripcion: String?
)

fun CategoryDto.toCategory(): Category = Category(
    id = IdCategoria.toString(),
    name = Nombre
)
