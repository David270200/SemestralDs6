package com.example.stylebyte.network

import com.example.stylebyte.network.dto.CategoryDto
import com.example.stylebyte.network.dto.CategoryWriteRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints de categorías expuestos por StyleStoreAPI (routes/categories.js). */
interface CategoryApiService {

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    /** Solo Administrador. */
    @POST("categories")
    suspend fun createCategory(@Body body: CategoryWriteRequestDto): Response<CategoryDto>

    /** Solo Administrador. */
    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body body: CategoryWriteRequestDto): Response<CategoryDto>

    /** Solo Administrador. Borrado lógico (Activo = 0) del lado del servidor. */
    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>
}
