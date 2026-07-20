package com.example.stylebyte

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/** Gestión de Pedidos del Admin: ve TODOS los pedidos (la API lo decide por el Rol) y cambia su estado. */
class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var adapter: AdminOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_orders)

        findViewById<Toolbar>(R.id.toolbar_admin_orders).setNavigationOnClickListener { finish() }

        rvOrders = findViewById(R.id.rv_admin_orders)
        rvOrders.layoutManager = LinearLayoutManager(this)
        adapter = AdminOrderAdapter(emptyList()) { order, newStatus -> changeStatus(order, newStatus) }
        rvOrders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            val result = OrderRepository.refreshFromApi()
            result.onFailure {
                Toast.makeText(this@AdminOrdersActivity, it.message ?: "No se pudieron cargar los pedidos", Toast.LENGTH_LONG).show()
            }
            adapter.updateData(OrderRepository.getAll())
        }
    }

    private fun changeStatus(order: Order, newStatus: OrderStatus) {
        lifecycleScope.launch {
            val result = OrderRepository.updateStatus(order.pedidoId, newStatus)
            result.onSuccess {
                Toast.makeText(this@AdminOrdersActivity, "Estado actualizado", Toast.LENGTH_SHORT).show()
                adapter.updateData(OrderRepository.getAll())
            }.onFailure {
                Toast.makeText(this@AdminOrdersActivity, it.message ?: "No se pudo actualizar el estado", Toast.LENGTH_LONG).show()
            }
        }
    }
}
