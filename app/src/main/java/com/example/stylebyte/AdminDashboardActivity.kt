package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Dashboard del Panel de Administración. Da acceso a los 4 módulos pedidos:
 * Productos, Categorías, Pedidos y Usuarios. Acceso solo mediante las
 * credenciales quemadas validadas en LoginActivity.
 */
class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val sessionManager = SessionManager(this)
        findViewById<TextView>(R.id.tv_admin_welcome).text =
            "Bienvenido, ${sessionManager.getUserEmail()}"

        setupCard(R.id.card_admin_products, R.drawable.ic_orders_admin, getString(R.string.admin_products)) {
            startActivity(Intent(this, AdminProductsActivity::class.java))
        }
        setupCard(R.id.card_admin_categories, R.drawable.ic_category, getString(R.string.admin_categories)) {
            startActivity(Intent(this, AdminCategoriesActivity::class.java))
        }
        setupCard(R.id.card_admin_orders, R.drawable.ic_orders_admin, getString(R.string.admin_orders)) {
            startActivity(Intent(this, AdminOrdersActivity::class.java))
        }
        setupCard(R.id.card_admin_users, R.drawable.ic_users_admin, getString(R.string.admin_users)) {
            startActivity(Intent(this, AdminUsersActivity::class.java))
        }

        findViewById<ImageView>(R.id.btn_admin_logout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión de administrador")
                .setMessage("¿Seguro que deseas salir del panel de administración?")
                .setPositiveButton("Salir") { _, _ ->
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

    private fun setupCard(containerId: Int, iconRes: Int, title: String, onClick: () -> Unit) {
        val cardRoot: View = findViewById(containerId)
        cardRoot.findViewById<ImageView>(R.id.iv_card_icon).setImageResource(iconRes)
        cardRoot.findViewById<TextView>(R.id.tv_card_title).text = title
        cardRoot.setOnClickListener { onClick() }
    }
}
