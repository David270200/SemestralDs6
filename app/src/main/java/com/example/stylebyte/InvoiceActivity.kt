package com.example.stylebyte

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Factura completa de un pedido — el detalle (con todos los productos) se
 * trae de GET /orders/:id.
 *
 * El botón "Imprimir Factura" ahora sí hace algo real: abre el diálogo nativo
 * de impresión de Android, que permite imprimir en una impresora de la red/USB
 * O exportar a PDF (Android trae "Guardar como PDF" como opción de fábrica).
 */
class InvoiceActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id" // ahora es el IdPedido (Int)
    }

    private var loadedOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val pedidoId = intent.getIntExtra(EXTRA_ORDER_ID, -1)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_invoice)
        toolbar.setNavigationOnClickListener { finish() }

        if (pedidoId == -1) {
            Toast.makeText(this, "No se encontró el pedido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<MaterialButton>(R.id.btn_print_invoice).setOnClickListener {
            val order = loadedOrder
            if (order == null) {
                Toast.makeText(this, "Espera a que cargue la factura", Toast.LENGTH_SHORT).show()
            } else {
                printInvoice(order)
            }
        }

        loadOrder(pedidoId)
    }

    private fun loadOrder(pedidoId: Int) {
        lifecycleScope.launch {
            val result = OrderRepository.fetchDetail(pedidoId)
            result.onSuccess { order ->
                loadedOrder = order
                renderInvoice(order)
            }.onFailure {
                Toast.makeText(this@InvoiceActivity, it.message ?: "No se pudo cargar la factura", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun renderInvoice(order: Order) {
        findViewById<TextView>(R.id.tv_invoice_order_number).text =
            "${getString(R.string.invoice_order_number)}: ${order.id}"
        findViewById<TextView>(R.id.tv_invoice_date).text =
            "${getString(R.string.invoice_date)}: ${order.date}"
        findViewById<TextView>(R.id.tv_invoice_customer).text =
            "${getString(R.string.invoice_customer)}: ${order.customerName}"
        findViewById<TextView>(R.id.tv_invoice_delivery_method).text =
            "${getString(R.string.invoice_delivery_method)}: ${order.deliveryMethod}"
        findViewById<TextView>(R.id.tv_invoice_status).text =
            "${getString(R.string.invoice_status)}: ${getString(order.status.labelRes)}"

        val llProducts = findViewById<LinearLayout>(R.id.ll_invoice_products)
        llProducts.removeAllViews()
        for (item in order.items) {
            val row = TextView(this).apply {
                text = "${item.quantity}x ${item.productName}\n" +
                        "  ${Order.formatPrice(item.unitPrice)} c/u  →  ${Order.formatPrice(item.subtotal)}"
                textSize = 13f
                setTextColor(getColor(R.color.black))
                setPadding(0, 6, 0, 6)
                gravity = Gravity.START
            }
            llProducts.addView(row)
        }

        findViewById<TextView>(R.id.tv_invoice_subtotal).text = Order.formatPrice(order.subtotal)
        findViewById<TextView>(R.id.tv_invoice_delivery_fee).text = Order.formatPrice(order.deliveryFee)
        findViewById<TextView>(R.id.tv_invoice_total).text = Order.formatPrice(order.total)
    }

    private fun printInvoice(order: Order) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Factura_${order.id}"
        printManager.print(jobName, InvoicePrintAdapter(this, order), PrintAttributes.Builder().build())
    }
}
