package com.example.stylebyte.network

import com.example.stylebyte.network.dto.ApiErrorDto
import com.google.gson.Gson
import retrofit2.Response

/**
 * Cuando la API responde con un error (400, 401, 409, 500...), Retrofit NO lanza
 * una excepción: hay que leer response.errorBody() manualmente. Esta función
 * centraliza eso para no repetir el mismo try/catch en cada Activity.
 */
fun <T> Response<T>.extractErrorMessage(defaultMessage: String = "Ocurrió un error inesperado"): String {
    return try {
        val errorJson = errorBody()?.string()
        if (errorJson.isNullOrBlank()) return defaultMessage
        val parsed = Gson().fromJson(errorJson, ApiErrorDto::class.java)
        parsed?.error ?: defaultMessage
    } catch (e: Exception) {
        defaultMessage
    }
}
