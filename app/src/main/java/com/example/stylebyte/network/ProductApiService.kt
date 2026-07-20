package com.example.stylebyte.network

import com.example.stylebyte.network.dto.ProductDto
import com.example.stylebyte.network.dto.ProductWriteRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de productos expuestos por StyleStoreAPI (routes/products.js). */
interface ProductApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products")
    suspend fun getProductsByCategory(@Query("categoria") idCategoria: Int): List<ProductDto>

    /** Solo Administrador. */
    @POST("products")
    suspend fun createProduct(@Body body: ProductWriteRequestDto): Response<ProductDto>

    /** Solo Administrador. */
    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body body: ProductWriteRequestDto): Response<ProductDto>

    /** Solo Administrador. Borrado lógico (Activo = 0) del lado del servidor. */
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>
}
