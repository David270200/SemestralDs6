package com.example.stylebyte

import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.ProductWriteRequestDto
import com.example.stylebyte.network.dto.toProduct
import com.example.stylebyte.network.extractErrorMessage

/**
 * Repositorio de productos. Lectura (getAll/search) Y escritura (add/update/
 * delete) están conectadas a StyleStoreAPI — el Admin ya edita la base de
 * datos real, no una copia en memoria.
 */
object ProductRepository {

    private var cache: MutableList<Product> = mutableListOf()
    private var hasLoadedOnce = false

    suspend fun refreshFromApi(): Result<List<Product>> {
        return try {
            val dtos = RetrofitClient.productApi.getProducts()
            cache = dtos.map { it.toProduct() }.toMutableList()
            hasLoadedOnce = true
            Result.success(cache.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAll(): List<Product> {
        if (!hasLoadedOnce) refreshFromApi()
        return cache.toList()
    }

    suspend fun byCategory(categoryId: String?): List<Product> {
        val all = getAll()
        return if (categoryId == null) all else all.filter { it.categoryId == categoryId }
    }

    suspend fun search(query: String, categoryId: String? = null): List<Product> {
        val base = byCategory(categoryId)
        if (query.isBlank()) return base
        val q = query.trim().lowercase()
        return base.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
    }

    fun findByIdCached(id: String): Product? = cache.find { it.id == id }

    private fun Product.toWriteRequest() = ProductWriteRequestDto(
        idCategoria = categoryId.toInt(),
        nombre = name,
        descripcion = description,
        precio = price,
        stock = stock,
        rutaImagen = imageUrl
    )

    /** Solo Administrador. Crea el producto en SQL Server (POST /products). */
    suspend fun add(product: Product): Result<Product> {
        return try {
            val response = RetrofitClient.productApi.createProduct(product.toWriteRequest())
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toProduct()
                cache.add(created)
                Result.success(created)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo crear el producto")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Solo Administrador. Edita el producto en SQL Server (PUT /products/:id). */
    suspend fun update(updated: Product): Result<Product> {
        return try {
            val response = RetrofitClient.productApi.updateProduct(updated.id.toInt(), updated.toWriteRequest())
            if (response.isSuccessful && response.body() != null) {
                val saved = response.body()!!.toProduct()
                val index = cache.indexOfFirst { it.id == saved.id }
                if (index >= 0) cache[index] = saved else cache.add(saved)
                Result.success(saved)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo actualizar el producto")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Solo Administrador. Borrado lógico en SQL Server (DELETE /products/:id -> Activo = 0). */
    suspend fun delete(id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.productApi.deleteProduct(id.toInt())
            if (response.isSuccessful) {
                cache.removeAll { it.id == id }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo eliminar el producto")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
