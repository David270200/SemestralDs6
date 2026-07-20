package com.example.stylebyte

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 * Gestión de Usuarios del Admin — YA NO es una lista simulada: lee la tabla
 * Usuarios real (GET /users) y permite activar/inactivar cuentas.
 */
class AdminUsersActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        findViewById<Toolbar>(R.id.toolbar_admin_users).setNavigationOnClickListener { finish() }

        rvUsers = findViewById(R.id.rv_admin_users)
        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = AdminUserAdapter(emptyList()) { user -> confirmToggleActive(user) }
        rvUsers.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            val result = AdminUserRepository.refreshFromApi()
            result.onFailure {
                Toast.makeText(this@AdminUsersActivity, it.message ?: "No se pudieron cargar los usuarios", Toast.LENGTH_LONG).show()
            }
            adapter.updateData(AdminUserRepository.getAll())
        }
    }

    private fun confirmToggleActive(user: AdminUser) {
        val accion = if (user.active) "inactivar" else "activar"
        AlertDialog.Builder(this)
            .setTitle("${accion.replaceFirstChar { it.uppercase() }} cuenta")
            .setMessage("¿Seguro que deseas $accion la cuenta de \"${user.name}\"?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    val result = AdminUserRepository.setActive(user.id, !user.active)
                    result.onSuccess {
                        adapter.updateData(AdminUserRepository.getAll())
                    }.onFailure {
                        Toast.makeText(this@AdminUsersActivity, it.message ?: "No se pudo actualizar el usuario", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
