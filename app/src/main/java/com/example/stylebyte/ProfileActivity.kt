package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.UpdateProfileRequestDto
import com.example.stylebyte.network.extractErrorMessage
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 * ProfileActivity: pantalla de Perfil del usuario.
 *
 * Todo lo que se muestra sale de la sesión real (que a su vez viene de SQL
 * Server al hacer login) y de OrderRepository (tabla Pedidos real). "Editar
 * perfil" guarda los cambios con PUT /users/me. Wishlist sigue siendo un
 * número simulado porque tu base de datos no tiene una tabla de favoritos
 * todavía (avísame si quieres que agreguemos una).
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnEditProfile: View

    private lateinit var tvStatOrders: TextView
    private lateinit var tvStatWishlist: TextView
    private lateinit var tvStatReviews: TextView

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

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para ver tu perfil", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bindViews()
        loadUserData()
        setupEditProfile()
        setupMenuRowClicks()
        setupSignOut()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun bindViews() {
        tvAvatarInitial = findViewById(R.id.tv_avatar_initial)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        btnEditProfile = findViewById(R.id.btn_edit_profile)

        tvStatOrders = findViewById(R.id.tv_stat_orders_count)
        tvStatWishlist = findViewById(R.id.tv_stat_wishlist_count)
        tvStatReviews = findViewById(R.id.tv_stat_reviews_count)

        rowMyOrders = findViewById(R.id.row_my_orders)
        rowSavedAddresses = findViewById(R.id.row_saved_addresses)
        rowWishlist = findViewById(R.id.row_wishlist)
        rowNotifications = findViewById(R.id.row_notifications)
        rowPaymentMethods = findViewById(R.id.row_payment_methods)
        btnAddNewAddress = findViewById(R.id.btn_add_new_address)
        btnSignOut = findViewById(R.id.btn_sign_out)

        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun loadUserData() {
        val name = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()

        tvUserName.text = name
        tvUserEmail.text = email
        tvAvatarInitial.text = name.trim().firstOrNull()?.uppercase() ?: "?"
    }

    /** Trae los pedidos reales del usuario (la API ya filtra "solo los míos") para los contadores. */
    private fun loadStats() {
        lifecycleScope.launch {
            val result = OrderRepository.refreshFromApi()
            result.onFailure {
                // No interrumpimos con un Toast acá: si falla la carga de stats no es crítico.
            }

            val orders = OrderRepository.getAll()
            tvStatOrders.text = orders.size.toString()
            tvStatReviews.text = orders.count { it.status == OrderStatus.DELIVERED }.toString()
            tvStatWishlist.text = WishlistSampleData.countFor(sessionManager.getUserName()).toString()
        }
    }

    /** Abre un diálogo simple para editar Nombre/Apellido/Teléfono/Dirección (PUT /users/me). */
    private fun setupEditProfile() {
        btnEditProfile.setOnClickListener {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 24, 48, 0)
            }

            val currentName = sessionManager.getUserName()
            val partes = currentName.split(" ", limit = 2)

            val etNombre = EditText(this).apply { hint = "Nombre"; setText(partes.getOrElse(0) { "" }) }
            val etApellido = EditText(this).apply { hint = "Apellido"; setText(partes.getOrElse(1) { "" }) }
            val etTelefono = EditText(this).apply { hint = "Teléfono (opcional)" }
            val etDireccion = EditText(this).apply { hint = "Dirección (opcional)" }

            container.addView(etNombre)
            container.addView(etApellido)
            container.addView(etTelefono)
            container.addView(etDireccion)

            AlertDialog.Builder(this)
                .setTitle("Editar perfil")
                .setView(container)
                .setPositiveButton("Guardar") { _, _ ->
                    val nombre = etNombre.text.toString().trim()
                    val apellido = etApellido.text.toString().trim()
                    if (nombre.isEmpty() || apellido.isEmpty()) {
                        Toast.makeText(this, "Nombre y apellido son obligatorios", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.userApi.updateMyProfile(
                                UpdateProfileRequestDto(
                                    nombre = nombre,
                                    apellido = apellido,
                                    telefono = etTelefono.text.toString().trim().ifEmpty { null },
                                    direccion = etDireccion.text.toString().trim().ifEmpty { null }
                                )
                            )

                            if (response.isSuccessful && response.body() != null) {
                                val usuario = response.body()!!
                                sessionManager.saveSession(
                                    userId = usuario.IdUsuario,
                                    name = "${usuario.Nombre} ${usuario.Apellido}".trim(),
                                    email = usuario.Correo,
                                    role = usuario.Rol,
                                    token = sessionManager.getToken() ?: ""
                                )
                                loadUserData()
                                Toast.makeText(this@ProfileActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ProfileActivity, response.extractErrorMessage("No se pudo actualizar el perfil"), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@ProfileActivity, "No se pudo conectar con la API", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupMenuRowClicks() {
        rowMyOrders.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        rowSavedAddresses.setOnClickListener {
            Toast.makeText(this, "Saved Addresses: próximamente", Toast.LENGTH_SHORT).show()
        }

        rowWishlist.setOnClickListener {
            Toast.makeText(this, "Wishlist: próximamente", Toast.LENGTH_SHORT).show()
        }

        rowNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications: próximamente", Toast.LENGTH_SHORT).show()
        }

        rowPaymentMethods.setOnClickListener {
            Toast.makeText(this, "Payment Methods: próximamente", Toast.LENGTH_SHORT).show()
        }

        btnAddNewAddress.setOnClickListener {
            Toast.makeText(this, "Add New Address: próximamente", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    false
                }

                R.id.nav_orders -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_profile -> true

                else -> false
            }
        }
    }
}
