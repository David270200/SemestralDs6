package com.example.stylebyte.network

import com.example.stylebyte.SessionManager
import com.example.stylebyte.StyleByteApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente único de Retrofit para hablar con StyleStoreAPI (Node.js).
 *
 * IMPORTANTE — BASE_URL según dónde estés probando la app:
 *  - Emulador de Android Studio: "http://10.0.2.2:3000/"
 *    (10.0.2.2 es una dirección especial que, DESDE el emulador, apunta al
 *    "localhost" de tu PC. No cambies esto si usas el emulador.)
 *  - Dispositivo físico conectado por USB o en la misma red WiFi que tu PC:
 *    tienes que usar la IP local de tu PC (ej: "http://192.168.1.50:3000/").
 *    La ves en Windows con: abre CMD -> escribe "ipconfig" -> busca
 *    "Dirección IPv4" de tu adaptador de red activo. "localhost" NO funciona
 *    desde un dispositivo físico porque ahí "localhost" sería el propio celular.
 *
 * Cambia SOLO la constante BASE_URL de abajo según el caso; el resto del
 * archivo no necesita tocarse.
 */
object RetrofitClient {

    // Cambia SOLO esta constante según dónde pruebes la app (ver explicación abajo).
    // No es privada porque también se usa para armar la URL completa de las
    // imágenes de productos (ver buildImageUrl más abajo).
    const val BASE_URL = "http://10.0.2.2:3000/"

    // Muestra en Logcat (filtra por "OkHttp") cada request y response completos.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Agrega automáticamente "Authorization: Bearer <token>" a CADA request,
    // si hay una sesión guardada. Las rutas que no lo necesitan (productos,
    // categorías, login, registro) simplemente lo ignoran del lado del servidor.
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val token = SessionManager(StyleByteApp.instance).getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val productApi: ProductApiService by lazy { retrofit.create(ProductApiService::class.java) }
    val categoryApi: CategoryApiService by lazy { retrofit.create(CategoryApiService::class.java) }
    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val cartApi: CartApiService by lazy { retrofit.create(CartApiService::class.java) }
    val orderApi: OrderApiService by lazy { retrofit.create(OrderApiService::class.java) }
    val userApi: UserApiService by lazy { retrofit.create(UserApiService::class.java) }

    /**
     * Convierte el valor de Productos.RutaImagen (ej: "images/products/camisa1.jpg",
     * tal como está guardado en SQL Server) en la URL completa donde la API
     * realmente sirve ese archivo (index.js expone /images como carpeta estática).
     * Devuelve null si el producto no tiene imagen asignada, para que Glide
     * pueda mostrar directamente el placeholder.
     */
    fun buildImageUrl(rutaImagen: String?): String? {
        if (rutaImagen.isNullOrBlank()) return null
        return if (rutaImagen.startsWith("http")) rutaImagen else BASE_URL + rutaImagen
    }
}
