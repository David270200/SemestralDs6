package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class AdminOrderAdapter(
    private var orders: List<Order>,
    private val onStatusChanged: (Order, OrderStatus) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tv_admin_order_id)
        val chipStatus: Chip = view.findViewById(R.id.chip_admin_order_status)
        val tvCustomer: TextView = view.findViewById(R.id.tv_admin_order_customer)
        val tvDate: TextView = view.findViewById(R.id.tv_admin_order_date)
        val tvTotal: TextView = view.findViewById(R.id.tv_admin_order_total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_order, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.tvId.text = order.id
        holder.tvCustomer.text = "Cliente: ${order.customerName}"
        holder.tvDate.text = "Fecha: ${order.date} · Entrega: ${order.deliveryMethod}"
        holder.tvTotal.text = "Total: ${order.totalPrice}"

        holder.chipStatus.text = context.getString(order.status.labelRes)
        holder.chipStatus.chipBackgroundColor = ContextCompat.getColorStateList(context, order.status.backgroundColorRes)
        holder.chipStatus.setTextColor(ContextCompat.getColor(context, order.status.textColorRes))

        // Tocar el chip abre un menú para cambiar el estado del pedido (Admin).
        holder.chipStatus.setOnClickListener {
            val popup = PopupMenu(context, holder.chipStatus)
            OrderStatus.values().forEach { status ->
                popup.menu.add(context.getString(status.labelRes)).setOnMenuItemClickListener {
                    onStatusChanged(order, status)
                    true
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
