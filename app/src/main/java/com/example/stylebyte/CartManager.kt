package com.example.stylebyte

import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.AddCartItemRequestDto
import com.example.stylebyte.network.dto.CartItemDto
import com.example.stylebyte.network.dto.UpdateCartItemRequestDto
import com.example.stylebyte.network.extractErrorMessage

/**
 * Carrito de compras — YA NO vive en memoria. Cada operación llama a la API
 * (StyleStoreAPI /cart), que a su vez lee/escribe las tablas Carrito y
 * DetalleCarrito en SQL Server. Así el carrito sobrevive aunque el usuario
 * cierre la app o cambie de celular con la misma cuenta.
 *
 * Requiere sesión iniciada (el token se agrega solo a cada request, ver
 * RetrofitClient). Si no hay sesión, todas las funciones devuelven un error
 * claro en vez de intentar la llamada.
 *
 * Regla de negocio del envío a domicilio ($50): la calcula la API
 * (campo "homeDeliveryAvailable" en cada respuesta) y Android solo la LEE,
 * para que quede validada en un único lugar y no se pueda desincronizar.
 */
object CartManager {

    const val HOME_DELIVERY_FEE = 5.0

    private var cachedItems: List<CartItemDto> = emptyList()
    private var cachedSubtotal: Double = 0.0
    private var cachedHomeDeliveryAvailable: Boolean = false

    private fun currentUserId(context: android.content.Context): Int? {
        val session = SessionManager(context)
        return if (session.isLoggedIn()) session.getUserId() else null
    }

    private fun updateCache(dto: com.example.stylebyte.network.dto.CartResponseDto) {
        cachedItems = dto.items
        cachedSubtotal = dto.subtotal
        cachedHomeDeliveryAvailable = dto.homeDeliveryAvailable
    }

    /** Trae el carrito actual desde la API y actualiza la copia local para lectura rápida. */
    suspend fun refresh(context: android.content.Context): Result<Unit> {
        val userId = currentUserId(context) ?: return Result.failure(IllegalStateException("Debes iniciar sesión para ver tu carrito"))
        return try {
            val response = RetrofitClient.cartApi.getCart(userId)
            if (response.isSuccessful && response.body() != null) {
                updateCache(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo cargar el carrito")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToCart(context: android.content.Context, product: Product, quantity: Int = 1): Result<Unit> {
        val userId = currentUserId(context) ?: return Result.failure(IllegalStateException("Debes iniciar sesión para agregar al carrito"))
        return try {
            val response = RetrofitClient.cartApi.addItem(userId, AddCartItemRequestDto(product.id.toInt(), quantity))
            if (response.isSuccessful && response.body() != null) {
                updateCache(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo agregar el producto")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(context: android.content.Context, idProducto: Int, quantity: Int): Result<Unit> {
        val userId = currentUserId(context) ?: return Result.failure(IllegalStateException("Debes iniciar sesión"))
        return try {
            val response = RetrofitClient.cartApi.updateItem(userId, idProducto, UpdateCartItemRequestDto(quantity))
            if (response.isSuccessful && response.body() != null) {
                updateCache(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo actualizar la cantidad")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(context: android.content.Context, idProducto: Int): Result<Unit> {
        val userId = currentUserId(context) ?: return Result.failure(IllegalStateException("Debes iniciar sesión"))
        return try {
            val response = RetrofitClient.cartApi.removeItem(userId, idProducto)
            if (response.isSuccessful && response.body() != null) {
                updateCache(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo quitar el producto")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Vacía el carrito completo (se usa después de confirmar un pedido). */
    suspend fun clear(context: android.content.Context): Result<Unit> {
        val userId = currentUserId(context) ?: return Result.failure(IllegalStateException("Debes iniciar sesión"))
        return try {
            val response = RetrofitClient.cartApi.clearCart(userId)
            if (response.isSuccessful && response.body() != null) {
                updateCache(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.extractErrorMessage("No se pudo vaciar el carrito")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Última copia conocida del carrito (ya sin llamar a la red). Úsala después de refresh(). */
    fun getItems(): List<CartItemDto> = cachedItems

    fun subtotal(): Double = cachedSubtotal

    fun isHomeDeliveryAvailable(): Boolean = cachedHomeDeliveryAvailable

    fun itemCount(): Int = cachedItems.sumOf { it.Cantidad }
}
