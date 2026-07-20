package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class OrderHistoryAdapter(
    private val orders: List<Order>,
    private val onViewDetails: (Order) -> Unit = {}
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val chipStatus: com.google.android.material.chip.Chip = view.findViewById(R.id.chipOrderStatus)
        val tvDate: android.widget.TextView = view.findViewById(R.id.tvOrderDate)
        val tvId: android.widget.TextView = view.findViewById(R.id.tvOrderId)
        val tvName: android.widget.TextView = view.findViewById(R.id.tvProductName)
        val tvQuantity: android.widget.TextView = view.findViewById(R.id.tvQuantity)
        val tvDelivery: android.widget.TextView = view.findViewById(R.id.tvDeliveryMethod)
        val tvTotal: android.widget.TextView = view.findViewById(R.id.tvOrderTotalPrice)
        val btnViewDetails: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.tvDate.text = order.date
        holder.tvId.text = order.id
        holder.tvName.text = order.productName
        holder.tvQuantity.text = "Cantidad: ${order.quantity}"
        holder.tvDelivery.text = "Entrega: ${order.deliveryMethod}"
        holder.tvTotal.text = order.totalPrice

        holder.chipStatus.text = context.getString(order.status.labelRes)
        holder.chipStatus.chipBackgroundColor =
            ContextCompat.getColorStateList(context, order.status.backgroundColorRes)
        holder.chipStatus.setTextColor(
            ContextCompat.getColor(context, order.status.textColorRes)
        )

        holder.btnViewDetails.setOnClickListener { onViewDetails(order) }
    }

    override fun getItemCount(): Int = orders.size
}