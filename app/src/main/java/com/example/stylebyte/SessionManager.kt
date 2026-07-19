package com.example.stylebyte

import android.content.Context

/**
 * SessionManager: pequeña clase de utilidad para guardar y borrar la sesión del usuario.
 *
 * ¿Por qué existe esta clase?
 * En Android, cuando necesitas guardar datos simples que sobrevivan aunque el usuario
 * cierre la app (como "¿hay alguien logueado?" o "¿cuál es su nombre?"), se usa
 * SharedPreferences: un almacenamiento tipo clave-valor que persiste en el dispositivo.
 *
 * Actualmente LoginActivity.kt NO guarda ninguna sesión al iniciar sesión (attemptSignIn()
 * solo valida el formulario y muestra un Toast). Por eso esta clase queda "preparada":
 * ProfileActivity ya puede usarla para cerrar sesión correctamente, pero para que el
 * flujo esté 100% completo, en el futuro habría que llamar a
 * SessionManager(this).saveSession(nombre, correo) dentro de LoginActivity.attemptSignIn()
 * cuando el login sea exitoso. Te explico esto más abajo en el chat, no lo hice porque
 * implicaría modificar un archivo existente y tú me pediste evitarlo salvo que sea
 * estrictamente necesario.
 */
class SessionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "stylebyte_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    // getSharedPreferences crea (o abre si ya existe) un archivo privado de la app
    // donde se guardan pares clave-valor. MODE_PRIVATE significa que solo esta app
    // puede leer/escribir ese archivo.
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Guarda que el usuario inició sesión, junto con su nombre y correo. */
    fun saveSession(name: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply() // apply() guarda en segundo plano (no bloquea la UI)
    }

    /** true si hay un usuario con sesión activa. */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    /** Devuelve el nombre guardado, o un valor por defecto si no hay sesión. */
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Usuario") ?: "Usuario"

    /** Devuelve el correo guardado, o un valor por defecto si no hay sesión. */
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    /** Borra todos los datos de sesión. Esto es lo que usa el botón "Sign Out". */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
