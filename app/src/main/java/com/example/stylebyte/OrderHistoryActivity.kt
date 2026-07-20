package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/** Historial de pedidos — ahora lee la tabla Pedidos real vía la API. */
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvOrderHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        toolbar = findViewById(R.id.toolbar_order_history)
        rvOrderHistory = findViewById(R.id.rv_order_history)

        toolbar.setNavigationOnClickListener { finish() }
        rvOrderHistory.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()

        if (!SessionManager(this).isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para ver tu historial de pedidos", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            val result = OrderRepository.refreshFromApi()
            result.onFailure {
                Toast.makeText(this@OrderHistoryActivity, it.message ?: "No se pudieron cargar tus pedidos", Toast.LENGTH_LONG).show()
            }

            rvOrderHistory.adapter = OrderHistoryAdapter(OrderRepository.getAll()) { order ->
                val intent = Intent(this@OrderHistoryActivity, InvoiceActivity::class.java)
                intent.putExtra(InvoiceActivity.EXTRA_ORDER_ID, order.pedidoId)
                startActivity(intent)
            }
        }
    }
}
