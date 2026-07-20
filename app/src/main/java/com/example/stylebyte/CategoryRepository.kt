package com.example.stylebyte

import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.CategoryWriteRequestDto
import com.example.stylebyte.network.dto.toCategory
import com.example.stylebyte.network.extractErrorMessage

/**
 * Repositorio de categorías. Lectura Y escritura conectadas a StyleStoreAPI.
 */
object CategoryRepository {

    private var cache: MutableList<Category> = mutableListOf()
    private var hasLoadedOnce = false

    suspend fun refreshFromApi(): Result<List<Category>> {
        return try {
            val dtos = RetrofitClient.categoryApi.getCategories()
            cache = dtos.map { it.toCategory() }.toMutableList()
            hasLoadedOnce = true
            Result.success(cache.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAll(): List<Category> {
        if (!hasLoadedOnce) refreshFromApi()
        return cache.toList()
    }

    fun findById(id: String): Category? = cache.find { it.id == id }

    /** Solo Administrador. */
    suspend fun add(name: String): Result<Category> {
        return try {
            val response = RetrofitClient.categoryApi.createCategory(CategoryWriteRequestDto(name))
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toCategory()
                cache.add(created)
                Result.success(created)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo crear la categoría")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Solo Administrador. */
    suspend fun update(id: String, newName: String): Result<Category> {
        return try {
            val response = RetrofitClient.categoryApi.updateCategory(id.toInt(), CategoryWriteRequestDto(newName))
            if (response.isSuccessful && response.body() != null) {
                val saved = response.body()!!.toCategory()
                val index = cache.indexOfFirst { it.id == id }
                if (index >= 0) cache[index] = saved else cache.add(saved)
                Result.success(saved)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo actualizar la categoría")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Solo Administrador. Borrado lógico (Activo = 0). */
    suspend fun delete(id: String): Result<Unit> {
        return try {
            val response = RetrofitClient.categoryApi.deleteCategory(id.toInt())
            if (response.isSuccessful) {
                cache.removeAll { it.id == id }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo eliminar la categoría")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
