package com.example.stylebyte

import android.app.Application

/**
 * Application propia de StyleByte.
 *
 * ¿Para qué se necesita? RetrofitClient es un "object" (singleton) que vive
 * fuera de cualquier Activity, pero necesita leer el token guardado en
 * SessionManager (que a su vez necesita un Context) para poder mandarlo en
 * cada request al carrito. Guardando el Context de la aplicación aquí, en un
 * solo lugar, evitamos tener que pasar el Context manualmente por todos lados.
 */
class StyleByteApp : Application() {

    companion object {
        lateinit var instance: StyleByteApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
