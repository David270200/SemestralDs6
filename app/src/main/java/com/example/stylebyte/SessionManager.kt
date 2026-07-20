package com.example.stylebyte

import android.content.Context

/**
 * SessionManager: guarda la sesión real del usuario (token JWT + datos básicos)
 * en SharedPreferences, para que sobreviva aunque el usuario cierre la app.
 *
 * Desde esta entrega, LoginActivity SÍ llama a saveSession(...) con los datos
 * reales que devuelve la API (POST /auth/login), incluyendo el token JWT que
 * se necesita para las llamadas protegidas (como el carrito).
 */
class SessionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "stylebyte_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_TOKEN = "auth_token"
    }

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Guarda la sesión completa después de un login/registro exitoso contra la API.
     * @param userId IdUsuario de SQL Server (necesario para las llamadas al carrito).
     * @param role "Administrador" o "Cliente", tal como viene de la tabla Usuarios.
     */
    fun saveSession(userId: Int, name: String, email: String, role: String, token: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Usuario") ?: "Usuario"

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "Cliente") ?: "Cliente"

    fun isAdmin(): Boolean = getUserRole().equals("Administrador", ignoreCase = true)

    /** Token JWT a enviar en el header "Authorization: Bearer <token>". Null si no hay sesión. */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /** Borra todos los datos de sesión. Esto es lo que usa el botón "Sign Out". */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
