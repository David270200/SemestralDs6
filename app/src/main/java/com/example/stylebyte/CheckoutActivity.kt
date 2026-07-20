package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Checkout: aplica la regla de negocio del carrito/entrega.
 *
 *  - Subtotal < $50  -> solo "Retiro en tienda" (sin costo de envío).
 *  - Subtotal >= $50 -> se habilita también "Envío a domicilio" (costo fijo $5.00).
 *
 * "homeDeliveryAvailable" ya viene calculado por la API (misma regla validada
 * de los dos lados, como se pidió); Android solo lee ese valor para armar la UI.
 *
 * NOTA: crear el Pedido real en SQL Server (Pedidos + DetallePedidos +
 * descuento de Stock, con transacción) es la próxima entrega. Por ahora,
 * al confirmar, se sigue guardando en el OrderRepository en memoria (como
 * ya funcionaba), pero el CARRITO que se lee aquí ya es 100% real.
 */
class CheckoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rgDeliveryMethod: RadioGroup
    private lateinit var rbPickup: RadioButton
    private lateinit var rbHomeDelivery: RadioButton
    private lateinit var tvLockedHint: TextView
    private lateinit var llCheckoutItems: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnConfirm: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        if (!SessionManager(this).isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para continuar con tu pedido", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bindViews()
        toolbar.setNavigationOnClickListener { finish() }

        rgDeliveryMethod.setOnCheckedChangeListener { _, _ -> updateTotals() }
        btnConfirm.setOnClickListener { confirmOrder() }

        loadCart()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar_checkout)
        rgDeliveryMethod = findViewById(R.id.rg_delivery_method)
        rbPickup = findViewById(R.id.rb_pickup)
        rbHomeDelivery = findViewById(R.id.rb_home_delivery)
        tvLockedHint = findViewById(R.id.tv_delivery_locked_hint)
        llCheckoutItems = findViewById(R.id.ll_checkout_items)
        tvSubtotal = findViewById(R.id.tv_checkout_subtotal)
        tvDeliveryFee = findViewById(R.id.tv_checkout_delivery_fee)
        tvTotal = findViewById(R.id.tv_checkout_total)
        btnConfirm = findViewById(R.id.btn_confirm_order)
    }

    private fun loadCart() {
        lifecycleScope.launch {
            val result = CartManager.refresh(this@CheckoutActivity)
            result.onFailure {
                Toast.makeText(this@CheckoutActivity, it.message ?: "No se pudo cargar tu carrito", Toast.LENGTH_LONG).show()
            }

            if (CartManager.getItems().isEmpty()) {
                Toast.makeText(this@CheckoutActivity, "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            renderOrderSummary()
            setupDeliveryRules()
            updateTotals()
        }
    }

    private fun renderOrderSummary() {
        llCheckoutItems.removeAllViews()
        for (item in CartManager.getItems()) {
            val row = TextView(this).apply {
                text = "${item.Cantidad}x ${item.Nombre}  —  ${Order.formatPrice(item.Precio * item.Cantidad)}"
                textSize = 13f
                setTextColor(getColor(R.color.black))
                setPadding(0, 8, 0, 8)
                gravity = Gravity.START
            }
            llCheckoutItems.addView(row)
        }
    }

    /** Aplica la regla de negocio de $50 (ya calculada por la API) sobre las opciones de entrega. */
    private fun setupDeliveryRules() {
        val homeDeliveryAvailable = CartManager.isHomeDeliveryAvailable()

        rbHomeDelivery.isEnabled = homeDeliveryAvailable
        rbHomeDelivery.alpha = if (homeDeliveryAvailable) 1f else 0.4f
        tvLockedHint.visibility = if (homeDeliveryAvailable) View.GONE else View.VISIBLE

        if (!homeDeliveryAvailable && rbHomeDelivery.isChecked) {
            rbPickup.isChecked = true
        }
    }

    private fun selectedDeliveryFee(): Double =
        if (rgDeliveryMethod.checkedRadioButtonId == R.id.rb_home_delivery) CartManager.HOME_DELIVERY_FEE else 0.0

    private fun selectedDeliveryMethodLabel(): String =
        if (rgDeliveryMethod.checkedRadioButtonId == R.id.rb_home_delivery) getString(R.string.home_delivery_option)
        else getString(R.string.pickup_option)

    private fun updateTotals() {
        val subtotal = CartManager.subtotal()
        val fee = selectedDeliveryFee()
        tvSubtotal.text = Order.formatPrice(subtotal)
        tvDeliveryFee.text = Order.formatPrice(fee)
        tvTotal.text = Order.formatPrice(subtotal + fee)
    }

    private fun confirmOrder() {
        if (CartManager.getItems().isEmpty()) {
            Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Doble chequeo de la regla de negocio justo antes de confirmar, por si
        // el usuario dejó la pantalla abierta un rato (defensa extra; la API
        // también la valida del lado del servidor, es la que manda de verdad).
        if (rgDeliveryMethod.checkedRadioButtonId == R.id.rb_home_delivery && !CartManager.isHomeDeliveryAvailable()) {
            Toast.makeText(this, "El envío a domicilio ya no está disponible para este subtotal", Toast.LENGTH_LONG).show()
            setupDeliveryRules()
            updateTotals()
            return
        }

        btnConfirm.isEnabled = false

        lifecycleScope.launch {
            val result = OrderRepository.createOrder(selectedDeliveryMethodLabel())

            result.onSuccess { order ->
                // La API ya vació el carrito como parte de la misma transacción;
                // sincronizamos la copia local para que Cart/Home no muestren datos viejos.
                CartManager.refresh(this@CheckoutActivity)

                Toast.makeText(this@CheckoutActivity, getString(R.string.order_confirmed), Toast.LENGTH_SHORT).show()

                val intent = Intent(this@CheckoutActivity, InvoiceActivity::class.java)
                intent.putExtra(InvoiceActivity.EXTRA_ORDER_ID, order.pedidoId)
                startActivity(intent)
                finish()
            }.onFailure {
                btnConfirm.isEnabled = true
                Toast.makeText(this@CheckoutActivity, it.message ?: "No se pudo confirmar el pedido", Toast.LENGTH_LONG).show()
                // Si fue un problema de stock, refrescamos el carrito para reflejar la realidad.
                loadCart()
            }
        }
    }
}
