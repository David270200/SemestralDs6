package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * ProfileActivity: pantalla de Perfil del usuario.
 *
 * Responsabilidades de esta pantalla:
 *  1. Mostrar los datos del usuario (nombre, correo, estadísticas).
 *  2. Reaccionar a los clics de cada fila del menú (My Orders, Wishlist, etc.).
 *  3. Manejar la Bottom Navigation Bar (Home, Browse, Cart, Orders, Profile).
 *  4. Cerrar la sesión ("Sign Out") y volver a la pantalla de Login.
 *
 * Nota para quien esté aprendiendo: esta Activity NO usa View Binding ni Jetpack Compose
 * porque el resto del proyecto tampoco los usa (ver LoginActivity.kt, OrderHistoryActivity.kt).
 * Se mantiene el mismo estilo: findViewById() manual dentro de onCreate().
 */
class ProfileActivity : AppCompatActivity() {

    // lateinit = "prometo inicializar esta variable antes de usarla" (en onCreate).
    // Se usa en vez de nullable (?) porque sabemos con certeza que existen en el layout.
    private lateinit var sessionManager: SessionManager

    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    private lateinit var rowMyOrders: LinearLayout
    private lateinit var rowSavedAddresses: LinearLayout
    private lateinit var rowWishlist: LinearLayout
    private lateinit var rowNotifications: LinearLayout
    private lateinit var rowPaymentMethods: LinearLayout
    private lateinit var btnAddNewAddress: TextView
    private lateinit var btnSignOut: LinearLayout

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // SessionManager nos permite leer si hay un usuario logueado y sus datos.
        sessionManager = SessionManager(this)

        bindViews()
        loadUserData()
        setupMenuRowClicks()
        setupSignOut()
        setupBottomNavigation()
    }

    /** Conecta cada variable de Kotlin con su vista del XML (findViewById). */
    private fun bindViews() {
        tvAvatarInitial = findViewById(R.id.tv_avatar_initial)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)

        rowMyOrders = findViewById(R.id.row_my_orders)
        rowSavedAddresses = findViewById(R.id.row_saved_addresses)
        rowWishlist = findViewById(R.id.row_wishlist)
        rowNotifications = findViewById(R.id.row_notifications)
        rowPaymentMethods = findViewById(R.id.row_payment_methods)
        btnAddNewAddress = findViewById(R.id.btn_add_new_address)
        btnSignOut = findViewById(R.id.btn_sign_out)

        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    /**
     * Pinta el nombre/correo del usuario.
     *
     * IMPORTANTE (te lo explico porque eres nuevo en esto): ahora mismo LoginActivity.kt
     * NO guarda ninguna sesión real cuando el usuario inicia sesión (solo muestra un Toast).
     * Por eso, si no hay sesión guardada, aquí mostramos "Sarah Chen" como dato de ejemplo
     * (igual al diseño de referencia) en vez de dejar campos vacíos. Cuando tu proyecto
     * tenga login real, sessionManager.isLoggedIn() será true y se mostrarán los datos
     * reales automáticamente, sin tocar este archivo.
     */
    private fun loadUserData() {
        val name = if (sessionManager.isLoggedIn()) sessionManager.getUserName() else "Sarah Chen"
        val email = if (sessionManager.isLoggedIn()) sessionManager.getUserEmail() else "sarah.chen@email.com"

        tvUserName.text = name
        tvUserEmail.text = email
        // Tomamos la primera letra del nombre para el avatar circular (ej: "Sarah" -> "S")
        tvAvatarInitial.text = name.trim().firstOrNull()?.uppercase() ?: "?"
    }

    /**
     * Asigna un OnClickListener a cada fila del menú.
     *
     * Solo "My Orders" navega a una pantalla real (OrderHistoryActivity, que ya existía
     * en tu proyecto). Las demás (Saved Addresses, Wishlist, Notifications, Payment Methods)
     * todavía no tienen pantalla propia, así que muestran un Toast de aviso -- exactamente
     * el mismo patrón que ya usa tu LoginActivity.kt para "¿Olvidaste tu contraseña?"
     * (Toast + comentario TODO), para mantener consistencia con el resto del proyecto.
     */
    private fun setupMenuRowClicks() {
        rowMyOrders.setOnClickListener {
            // OrderHistoryActivity ya existe en el proyecto (la creó tu compañero/a).
            // Solo necesitamos abrirla con un Intent explícito.
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        rowSavedAddresses.setOnClickListener {
            // TODO: crear SavedAddressesActivity y navegar aquí cuando exista.
            Toast.makeText(this, "Saved Addresses: próximamente", Toast.LENGTH_SHORT).show()
        }

        rowWishlist.setOnClickListener {
            // TODO: crear WishlistActivity y navegar aquí cuando exista.
            Toast.makeText(this, "Wishlist: próximamente", Toast.LENGTH_SHORT).show()
        }

        rowNotifications.setOnClickListener {
            // TODO: crear NotificationsActivity y navegar aquí cuando exista.
            Toast.makeText(this, "Notifications: próximamente", Toast.LENGTH_SHORT).show()
        }

        rowPaymentMethods.setOnClickListener {
            // TODO: crear PaymentMethodsActivity y navegar aquí cuando exista.
            Toast.makeText(this, "Payment Methods: próximamente", Toast.LENGTH_SHORT).show()
        }

        btnAddNewAddress.setOnClickListener {
            // TODO: crear pantalla/formulario para agregar una nueva dirección.
            Toast.makeText(this, "Add New Address: próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura el botón "Sign Out".
     *
     * Flujo:
     *  1. Se muestra un diálogo de confirmación (buena práctica de UX: una acción
     *     destructiva/importante como cerrar sesión no debería ejecutarse con un solo toque
     *     accidental).
     *  2. Si el usuario confirma, se borra la sesión (SessionManager.clearSession()).
     *  3. Se navega a LoginActivity, limpiando TODO el historial de pantallas anteriores
     *     con las flags FLAG_ACTIVITY_NEW_TASK y FLAG_ACTIVITY_CLEAR_TASK. Esto es importante:
     *     sin esas flags, si el usuario presiona "atrás" en la pantalla de Login, podría
     *     volver a Profile aunque ya cerró sesión. Con las flags, Login queda como la única
     *     pantalla en la pila de navegación.
     */
    private fun setupSignOut() {
        btnSignOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que deseas cerrar sesión?")
                .setPositiveButton("Sign Out") { _, _ ->
                    sessionManager.clearSession()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    /**
     * Configura la Bottom Navigation Bar.
     *
     * bottom_nav_menu.xml (creado en el paso anterior) define 5 items: nav_home, nav_browse,
     * nav_cart, nav_orders, nav_profile. Aquí escuchamos cuál fue presionado y navegamos.
     *
     * Detalle importante del orden del código:
     *  1. Primero marcamos "Profile" como seleccionado (bottomNavigation.selectedItemId).
     *  2. RECIÉN DESPUÉS asignamos el listener (setOnItemSelectedListener).
     * ¿Por qué en ese orden? Si asignamos el listener ANTES de marcar el item seleccionado,
     * ese mismo cambio dispara el listener inmediatamente y ProfileActivity se re-abriría
     * a sí misma en un bucle apenas se crea la pantalla. Haciéndolo en este orden evitamos
     * ese bug.
     */
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // TODO: cuando exista HomeActivity (la está haciendo tu compañero/a),
                    // reemplazar esta línea por:
                    // startActivity(Intent(this, HomeActivity::class.java))
                    // finish()
                    Toast.makeText(this, "Home: pantalla en construcción", Toast.LENGTH_SHORT).show()
                    false // false = no marcar este item como seleccionado (aún no existe la pantalla)
                }

                R.id.nav_cart -> {
                    // TODO: navegar a CartActivity cuando exista.
                    Toast.makeText(this, "Cart: pantalla en construcción", Toast.LENGTH_SHORT).show()
                    false
                }

                R.id.nav_orders -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    finish() // cerramos Profile para que "atrás" no regrese a una copia duplicada
                    true
                }

                R.id.nav_profile -> {
                    // Ya estamos en Profile: no hacemos nada, solo confirmamos la selección.
                    true
                }

                else -> false
            }
        }
    }
}
