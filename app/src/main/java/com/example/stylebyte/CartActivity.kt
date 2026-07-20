package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/** El carrito ahora vive en SQL Server (tablas Carrito/DetalleCarrito), vía la API. */
class CartActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvCart: RecyclerView
    private lateinit var llEmptyCart: View
    private lateinit var summaryContainer: View
    private lateinit var tvSubtotal: TextView
    private lateinit var btnGoCheckout: MaterialButton

    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        toolbar = findViewById(R.id.toolbar_cart)
        rvCart = findViewById(R.id.rv_cart)
        llEmptyCart = findViewById(R.id.ll_empty_cart)
        summaryContainer = findViewById(R.id.summary_container)
        tvSubtotal = findViewById(R.id.tv_cart_subtotal)
        btnGoCheckout = findViewById(R.id.btn_go_checkout)

        toolbar.setNavigationOnClickListener { finish() }

        rvCart.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            items = emptyList(),
            onQuantityChange = { idProducto, newQty -> updateQuantity(idProducto, newQty) },
            onRemove = { idProducto -> removeItem(idProducto) }
        )
        rvCart.adapter = cartAdapter

        btnGoCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        if (!SessionManager(this).isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para ver tu carrito", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            val result = CartManager.refresh(this@CartActivity)
            result.onFailure {
                Toast.makeText(this@CartActivity, it.message ?: "No se pudo cargar el carrito", Toast.LENGTH_LONG).show()
            }
            renderCart()
        }
    }

    private fun renderCart() {
        val items = CartManager.getItems()
        cartAdapter.updateData(items)

        val isEmpty = items.isEmpty()
        rvCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        summaryContainer.visibility = if (isEmpty) View.GONE else View.VISIBLE
        llEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE

        tvSubtotal.text = Order.formatPrice(CartManager.subtotal())
        btnGoCheckout.isEnabled = !isEmpty
    }

    private fun updateQuantity(idProducto: Int, newQuantity: Int) {
        lifecycleScope.launch {
            val result = CartManager.updateQuantity(this@CartActivity, idProducto, newQuantity)
            result.onFailure {
                Toast.makeText(this@CartActivity, it.message ?: "No se pudo actualizar la cantidad", Toast.LENGTH_SHORT).show()
            }
            renderCart()
        }
    }

    private fun removeItem(idProducto: Int) {
        lifecycleScope.launch {
            val result = CartManager.removeFromCart(this@CartActivity, idProducto)
            result.onFailure {
                Toast.makeText(this@CartActivity, it.message ?: "No se pudo quitar el producto", Toast.LENGTH_SHORT).show()
            }
            renderCart()
        }
    }
}
